package lyra.listeners;

import lyra.Compiler;
import lyra.JasminLauncher;
import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.symbols.*;
import lyra.tokens.NumberToken;
import lyra.tokens.StringToken;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class JasminListener extends ScopedBaseListener {
    private File outputDir;
    private JasminLauncher jasminLauncher = new JasminLauncher();
    private List<File> jasminFiles = new LinkedList<>();
    private List<File> classFiles = new LinkedList<>();

    private ClassSymbol classSymbol;
    private MethodSymbol methodSymbol;

    private File file;
    private ByteArrayOutputStream methodOutputStream;
    private PrintWriter writer;
    private PrintWriter classWriter;

    private int methodStackUsage;
    /** Tracks stack usage as the method childs are visited, when this grows larger than
     *  methodLocalsUsage, methodLocalsUsage is updated. */
    private int methodCurrentStackUsage;

    private HashMap<VariableSymbol, Integer> methodVars = new HashMap<>();
    private HashMap<ParserRuleContext, Integer> methodLabels = new HashMap<>();
    private HashMap<ParserRuleContext, Integer> methodLabelsAfter = new HashMap<>();

    public JasminListener(Compiler compiler, File outputDir) {
        super(compiler);
        jasminLauncher.setOutputDir(outputDir.toPath());
        this.outputDir = outputDir;
    }

    @Override
    protected void beginScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = table.getNodeScope(ctx);
    }

    @Override
    protected void endScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    private String mapVisibility(Visibility visibility) {
        switch (visibility) {
            case PRIVATE: return "private";
            case PUBLIC: return "public";
            case PROTECTED: return "protected";
            default: break;
        }
        return null;
    }

    private String typeSpec(TypeSymbol type) {
        String spec = "L";
        if (type instanceof InterfaceSymbol)
            spec = "I";
        spec += type.getBinaryName() + ";";
        return spec;
    }
    private String methodSpec(MethodSymbol method) {
        String returnSpec = (method.isConstructor()) ? "V" : typeSpec(method.getReturnType());

        return String.format("%1$s/%2$s(%3$s)%4$s",
                ((TypeSymbol)method.getEnclosingScope()).getBinaryName(),
                method.getBinaryName(),
                method.getArgumentTypes().stream().map(t -> typeSpec(t))
                    .reduce((a, b) -> a + b).orElse(""),
                returnSpec);
    }

    private void incStackUsage(int count) {
        methodCurrentStackUsage += count;
        if (methodCurrentStackUsage > methodStackUsage)
            methodStackUsage = methodCurrentStackUsage;
    }
    private void decStackUsage(int count) {
        methodCurrentStackUsage -= count;
    }

    private void declareVar(VariableSymbol var) {
        if (methodVars.get(var) != null)
            return;
        int idx = methodVars.size();
        methodVars.put(var, new Integer(idx));
        writer.printf(".var %1$d is %2$s %3$s\n", idx, var.getName(), typeSpec(var.getType()));
    }

    private VariableSymbol createTempVar(TypeSymbol type) {
        VariableSymbol var = new VariableSymbol("lyra_jasmin_temp_" + methodVars.size(), type);
        declareVar(var);
        return var;
    }

    private void loadVar(VariableSymbol var) {
        Integer idx = methodVars.get(var);
        incStackUsage(1);
        writer.printf("aload %1$d\n", idx.intValue());
    }
    private void storeVar(VariableSymbol var) {
        Integer idx = methodVars.get(var);
        writer.printf("astore %1$d\n", idx.intValue());
        decStackUsage(1);
    }
    private String generateLabel(ParserRuleContext node) {
        methodLabels.put(node, methodLabels.size());
        return getLabel(node);
    }
    private String getLabel(ParserRuleContext node) {
        Integer integer = methodLabels.get(node);
        if (integer == null)
            return null;
        return "LyraLabel" + integer.intValue();
    }
    private String generateLabelAfter(ParserRuleContext node) {
        methodLabelsAfter.put(node, methodLabelsAfter.size());
        return getLabelAfter(node);
    }
    private String getLabelAfter(ParserRuleContext node) {
        Integer integer = methodLabelsAfter.get(node);
        if (integer == null)
            return null;
        return "LyraLabelAfter" + integer.intValue();
    }

    private void createJasminFile(String className) {
        this.file = new File(outputDir, className + ".j");
        if (this.file.exists())
            this.file.delete();
        try {
            writer = new PrintWriter(this.file, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        jasminFiles.add(this.file);
    }

    private void endJasminFile() {
        writer.flush();
        writer.close();
        writer = null;
        compileJasmin();
    }

    private void compileJasmin() {
        if (!jasminLauncher.assemble(file.toPath())) {
            compiler.getErrorListener().semanticError(compiler.getParser(),
                    table.getSymbolNode(classSymbol), "Could not compile jasmin file.");
        }
    }

    private void closeJasminFile() {
        endJasminFile();
        file = null;
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        String label = getLabel(ctx);
        if (label != null)
            writer.printf("%1$s:\n", label);
        super.enterEveryRule(ctx);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        super.exitEveryRule(ctx);

        String label = getLabelAfter(ctx);
        if (label != null)
            writer.printf("%1$s:\n", label);
    }

    @Override
    public void enterClassBody(LyraParser.ClassBodyContext ctx) {
        LyraParser.ClassdeclContext parent = (LyraParser.ClassdeclContext) ctx.getParent();
        classSymbol = (ClassSymbol) table.getNodeSymbol(parent);
        createJasminFile(classSymbol.getName());

        writer.println(".source " + file.getName());
        writer.println(".class public " + classSymbol.getBinaryName());
        writer.println(".super " + classSymbol.getSuperClass().getBinaryName());
        writer.println();
        writer.println();
    }

    @Override
    public void enterMethodDecl(LyraParser.MethodDeclContext ctx) {
        super.enterMethodDecl(ctx);

        methodSymbol = (MethodSymbol) table.getNodeSymbol(ctx);
        List<TypeSymbol> args = methodSymbol.getArgumentTypes();

        methodStackUsage = 0;
        methodCurrentStackUsage = 0;
        methodVars.clear();
        methodLabels.clear();

        writer.printf(".method %1$s %2$s(", mapVisibility(methodSymbol.getVisibility()),
                methodSymbol.getBinaryName());

        for (TypeSymbol type : args) {
            writer.print(typeSpec(type));
        }
        writer.printf(")%1$s\n", typeSpec(methodSymbol.getReturnType()));

        methodOutputStream = new ByteArrayOutputStream();
        classWriter = writer;
        writer = new PrintWriter(methodOutputStream);

        declareVar((VariableSymbol) currentScope.resolve("this"));
        for (VariableSymbol arg : methodSymbol.getArguments())
            declareVar(arg);

    }

    @Override
    public void exitMemberFactor(LyraParser.MemberFactorContext ctx) {
        /* code for ctx.factor() and all expr() child of ctx.args() have already been visited
         * and their results are already stacked in the same left-to-right order they were
         * visited. The stack is magically ready for us, we just need to check if we are visiting
         * a method call or a field access. */
        Symbol memberSymbol = table.getNodeSymbol(ctx.IDENT());

        if (memberSymbol instanceof VariableSymbol) {
            VariableSymbol field = (VariableSymbol) memberSymbol;
            if (field.isClassField()) {
                incStackUsage(1); //we will push the field value but we have nothing stacked for us
                writer.printf("getstatic %1$s %2$s\n", field.getBinaryName(),
                                                       typeSpec(field.getType()));
            } else if (!isLeftOfAssignment(ctx)) {
                /* get the field of the stacked object, this will replace the currently
                 * stacked reference. If we are the left side of an assignment, we leave the object
                 * reference stacked. enterExpression only alows visiting this node for when our
                 * expression produces a named reference to an object field. */
                writer.printf("getfield %1$s %2$s\n", field.getBinaryName(),
                                                      typeSpec(field.getType()));
            }
        } else if (memberSymbol instanceof MethodSymbol) {
            MethodSymbol method = (MethodSymbol)memberSymbol;
            /* Object and method arguments are already stacked left-to-right (the rightmost
             * argument is at the top of the stack). The exitExpr method check to see if it is a
             * child of a memberFactor, and in that case, already emits the code to perform any
             * necessary implicit conversion, so we have the stack with the right types as well.
             */
            writer.printf("invokevirtual %1$s\n", methodSpec(method));
            /* pop object and arguments, leave a result */
            decStackUsage(1 + method.getArgumentTypes().size() - 1);
        }
    }

    private boolean isLeftOfAssignment(LyraParser.FactorContext ctx) {
        ParserRuleContext child = ctx;
        ParserRuleContext parent = ctx.getParent();
        while (parent != null) {
            if (parent instanceof LyraParser.FactorContext) {
                if (!(parent instanceof LyraParser.WrappedFactorContext))
                    return false;
            } else if (parent instanceof LyraParser.UnaryexprContext) {
                if (((LyraParser.UnaryexprContext)parent).factor() == null)
                    return false;
            } else if (parent instanceof LyraParser.ExprContext) {
                LyraParser.ExprContext expr = (LyraParser.ExprContext) parent;
                if (expr.binOp != null
                        && expr.binOp.getType() == LyraLexer.EQUALOP
                        && expr.expr(0) == child) {
                    return true;
                }
            }

            child = parent;
            parent = parent.getParent();
        }
        return  false;
    }

    @Override
    public void exitNameFactor(LyraParser.NameFactorContext ctx) {
        Symbol symbol = table.getNodeSymbol(ctx); //courtesy of TypeListener
        if (symbol instanceof MethodSymbol) {
            /* a method call to this without arguments */
            MethodSymbol method = (MethodSymbol) symbol;
            loadVar((VariableSymbol) methodSymbol.resolve("this"));
            writer.printf("invokevirtual %1$s\n", methodSpec(method));
            //this is replaced with the method return
        } else if (symbol instanceof VariableSymbol) {
            /* (class) var access, get the var value and stack it */
            VariableSymbol var = (VariableSymbol)symbol;
            if (var.isClassField() || var.getScope() == table.getGlobal()) {
                /* globals are static fields of lyra/runtime/Start */
                writer.printf("getstatic %1$s %2$s\n", var.getBinaryName(),
                        typeSpec(var.getType()));
            } else if (var.isChildOf(methodSymbol)) {
                /* local var access */
                loadVar(var);
            } else {
                /* acessing a var with implicit this */
                if (isLeftOfAssignment(ctx)) {
                    /* we are the outermost expression on the left side of an assignment, leave
                     * only a reference to this stacked, do not access the var. */
                    loadVar((VariableSymbol) methodSymbol.resolve("this"));
                } else {
                    incStackUsage(1); //this pushed
                    loadVar((VariableSymbol) methodSymbol.resolve("this"));
                    writer.printf("getfield %1$s %2$s\n",
                            var.getBinaryName(), typeSpec(var.getType()));
                    decStackUsage(1); //this popped
                }
            }
            incStackUsage(1); //var value is pushed
        }
        /* symbol may also be a ClassSymbol, this is only valid as the child factor of an
         * memberFactor, and exitMemberFactor will output the code. */
    }

    @Override
    public void enterExpr(LyraParser.ExprContext ctx) {
        if (ctx.binOp != null && ctx.binOp.getType() == LyraLexer.EQUALOP) {
            VariableSymbol var = (VariableSymbol) table.getNodeSymbol(ctx.expr(0));
            if (var.isClassField() || var.isChildOf(methodSymbol)) {
                /* class field or local var, fully handled on exitExpr() */
                muteSubtree(ctx.expr(0));
            }
            /* for member variable accesses we allow child visiting, but the child will
             * generate code that gives us the object of which the var field is being
             * accessed. */
        }
    }

    @Override
    public void exitExpr(LyraParser.ExprContext ctx) {
        if (ctx.binOp != null && ctx.binOp.getType() == LyraLexer.EQUALOP) {
            /* assignment expression
             * The left expr() should be an memberFactor or a nameFactor refering
             * to a VariableSymbol. In both cases the code for the left expression
             * was not generated, onyl code for the right expression was generated and it's result
             * is at the top of the operand stack. */

            VariableSymbol var = (VariableSymbol) table.getNodeSymbol(ctx.expr(0));
            if (var.isChildOf(methodSymbol)) {
                /* local variable. The result of the right expression is the stack top */
                storeVar(var);
            } else if (var.isClassField()) {
                /* setting a static field, consumes the stack top */
                writer.printf("putstatic %1$s %2$s\n", var.getBinaryName(),
                                                       typeSpec(var.getType()));
                decStackUsage(1);
            } else {
                /* must be an field, by elimination */
                /* some magic took place: enterExpr() allowed visiting the left subtree, but the
                 * factor of the outermost expression of the left subtree knew it was the left
                 * side of an assignment, and instead of generated only code to put the object
                 * of which it access a field on the stack. By the listerner walking, we will have
                 * [..., objectref, rhsvalue] on the stack, which is exactly what putfield
                 * expects. */
                writer.printf("putfield %1$s %2$s\n", var.getBinaryName(), typeSpec(var.getType()));
                decStackUsage(2 /*putfield*/);
            }
        }

        //TODO !!! check if child of memberFactorContext and generate conversion of method argument
    }

    @Override
    public void exitStringFactor(LyraParser.StringFactorContext ctx) {
        incStackUsage(1 /*new*/ + 1 /*dup*/ + 1 /*ldc*/);
        writer.printf("new %1$s\n" +
                        "dup\n" + /* invokespecial will consume the duplicate */
                        "ldc \"%2$s\"\n" +
                        "invokespecial %1$s/<init>(Ljava/lang/String;)V\n",
                table.getPredefinedClass("String").getBinaryName(),
                ((StringToken) ctx.STRING().getSymbol()).getContent());
        decStackUsage(2); /* only the lyra/runtime/String remains */
    }

    @Override
    public void exitNumberFactor(LyraParser.NumberFactorContext ctx) {
        NumberToken tok = (NumberToken) ctx.NUMBER();
        ClassSymbol type = table.getPredefinedClass(tok.getLyraTypeName());
        String primitive = type.getName().equals("Int") ? "I" : "D";
        incStackUsage(1 /*new*/ + 1 /*dup*/ + 1 /*ldc*/);
        writer.printf("new %1$s\n" +
                      "dup\n" +
                      "ldc %2$s\n" +
                      "invokespecial %1$s/<init>(%3$s)V\n"
                , type.getBinaryName(), tok.getText(), primitive);
        decStackUsage(2 /*dup, ldc*/);
    }

    @Override
    public void exitBoolFactor(LyraParser.BoolFactorContext ctx) {
        String tail = ctx.FALSE() != null ? "false" : "true";
        VariableSymbol var = (VariableSymbol)table.getGlobal().resolve(tail);
        incStackUsage(1);
        writer.printf("getstatic %1$s %2$s\n",
                var.getBinaryName(), var.getType().getBinaryName());
    }

    @Override
    public void exitStatement(LyraParser.StatementContext ctx) {
        if (ctx.expr() != null) {
            /* ignore the result of the expression. Assignment expressions generate code for the
             * assignment, but that code also leaves the newly written reference on the operand
             * stack. */
            writer.println("pop");
            decStackUsage(1);
        }

        //TODO !!! handle other cases?
    }

    @Override
    public void exitVarDeclUnit(LyraParser.VarDeclUnitContext ctx) {
        if ((ctx.getParent().getParent() instanceof LyraParser.VarDeclStatContext)) {
            /* method-local variable */
            VariableSymbol varSymbol = (VariableSymbol) table.getNodeSymbol(ctx);
            declareVar(varSymbol);
            ClassSymbol classSymbol = (ClassSymbol) varSymbol.getType();

            if (ctx.expr() != null) {
                /* result of expr is on stack top */
                checkAndDoConversion(table.getNodeType(ctx.expr()), classSymbol);
                storeVar(varSymbol);
            }
        }

    }

    private void checkAndDoConversion(TypeSymbol actual, ClassSymbol target) {
        if (actual.isA(target))
            return;
        MethodSymbol constructor = target.conversionConstructor(actual);
        /* dup below makes us store the original stack top into a temporary variable */
        VariableSymbol tempVar = createTempVar(actual);
        storeVar(tempVar);
        decStackUsage(1);

        /* construct a target instance with the conversion constructor */
        incStackUsage(3 /*new + dup + loadVar()*/);
        writer.printf("new %1$s\n" +
                      "dup\n", target.getBinaryName());
        loadVar(tempVar);
        writer.printf("invokespecial %1$s", methodSpec(constructor));

        decStackUsage(2); /*invokespecial*/

        /* stack top is an instance of target converted from the previous stack top */
    }

    @Override
    public void enterIfstat(LyraParser.IfstatContext ctx) {
        String endIfLabel = generateLabelAfter(ctx);
        String elseLabel = (ctx.elsestat() != null) ? generateLabel(ctx.elsestat())
                                                    : endIfLabel;

        doOnceAfter(ctx.expr(), () -> {
            /* The resulting Object from expr is stacked, but it may not be a Bool */
            checkAndDoConversion(table.getNodeType(ctx.expr()), table.getPredefinedClass("Bool"));
            /* We have a Bool on the stack top. Get it's boolean primitive and do the if */
            writer.printf("invokevirtual lyra/runtime/Bool/valueOf()Z\n" +
                    "ifeq %1$s\n", elseLabel);
            decStackUsage(1); /* ifne pops the boolean */
        });
        /* jump from the end of the true statlist to after the ifstat. */
        if (ctx.elsestat() != null) {
            doOnceAfter(ctx.statlist(), () -> {
                writer.printf("goto %1$s\n", endIfLabel);
            });
        }
        /* else needs no handling other than label generation */
    }

    @Override
    public void exitMethodDecl(LyraParser.MethodDeclContext ctx) {
        writer.flush();
        String body = "";
        try {
            body = new String(methodOutputStream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) { }
        writer.close();
        methodOutputStream = null;
        writer = classWriter;

        writer.printf(".limit stack %1$d\n" +
                ".limit locals %2$d\n" +
                "%3$s\n", methodStackUsage, methodVars.size(), body);

        /* inject lyra/runtime/Void return */
        ClassSymbol voidClass = table.getPredefinedClass("void");
        if (methodSymbol.getReturnType() == voidClass) {
            MethodSymbol constructor = voidClass.resolveOverload("constructor",
                    Collections.emptyList());
            writer.printf("new %1$s\n" +
                    "dup\n" +
                    "invokespecial %2$s\n" +
                    "areturn\n", voidClass.getBinaryName(), methodSpec(constructor));
        }

        writer.printf(".end method\n\n\n");

        methodSymbol = null;
        methodVars.clear();
        methodLabels.clear();
        super.exitMethodDecl(ctx);
    }

    @Override
    public void exitClassBody(LyraParser.ClassBodyContext ctx) {
        boolean hasGeneratedConstructor = classSymbol.getOverloads("constructor")
                .anyMatch(m -> m.getEnclosingScope() == classSymbol
                        && m.getArguments().size() == 0
                        && table.getSymbolNode(m) == null);
        if (hasGeneratedConstructor) {
            MethodSymbol parent = classSymbol.getSuperClass().resolveOverload("constructor",
                    Collections.emptyList());
            writer.printf(".method public <init>()V\n" +
                          ".limit stack 1\n" +
                          ".limit locals 1\n" +
                          ".var 0 is this %1$s\n" +
                          "aload_0\n" +
                          "invokespecial %2$s\n" +
                          "return\n" +
                          ".end method\n", typeSpec(classSymbol), methodSpec(parent));
        }
        classSymbol = null;
        endJasminFile();
    }
}
