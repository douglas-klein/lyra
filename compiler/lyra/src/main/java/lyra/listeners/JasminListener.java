package lyra.listeners;

import lyra.Compiler;
import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.symbols.*;
import lyra.tokens.NumberToken;
import lyra.tokens.StringToken;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class JasminListener extends ScopedBaseListener {
    private File outputDir;
    private List<File> jasminFiles = new LinkedList<>();
    private List<File> classFiles = new LinkedList<>();

    private ClassSymbol classSymbol;
    private MethodSymbol methodSymbol;

    private File file;
    private ByteArrayOutputStream methodOutputStream;
    private PrintWriter writer;
    private PrintWriter classWriter;

    private int methodStackUsage;
    private int methodLocalsUsage;
    /** Tracks stack usage as the method childs are visited, when this grows larger than
     *  methodLocalsUsage, methodLocalsUsage is updated. */
    private int methodCurrentStackUsage;

    private HashMap<VariableSymbol, Integer> methodVars = new HashMap<>();

    public JasminListener(Compiler compiler, File outputDir) {
        super(compiler);
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

    private void createJasminFile(String className) {
        file = new File(className + ".j");
        if (file.exists())
            file.delete();
        try {
            writer = new PrintWriter(file, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        jasminFiles.add(file);
    }

    private void endJasminFile() {
        writer.flush();
        writer.close();
        writer = null;
        compileJasmin();
    }

    private void compileJasmin() {
        /* TODO !!! implementar, acho que vamos ter spawnar um java -jar jasmine.jar
         * e embarcar esse jar no muque dentro do nosso. */
    }

    private void closeJasminFile() {
        endJasminFile();
        file = null;
    }

    @Override
    public void enterClassBody(LyraParser.ClassBodyContext ctx) {
        LyraParser.ClassdeclContext parent = (LyraParser.ClassdeclContext) ctx.getParent();
        classSymbol = (ClassSymbol) table.getNodeSymbol(parent);
        createJasminFile(classSymbol.getName());

        writer.println(".source " + file.getName() + ".j");
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

        methodLocalsUsage = 1 + args.size(); //"this" always present
        methodStackUsage = 0;
        methodCurrentStackUsage = 0;
        methodVars.clear();

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
        if (ctx.getParent() instanceof LyraParser.UnaryexprContext) {
            if (ctx.getParent().getParent() instanceof LyraParser.ExprContext) {
                LyraParser.ExprContext maybeLeft =
                        (LyraParser.ExprContext) ctx.getParent().getParent();
                LyraParser.ExprContext maybeAssign =
                        (LyraParser.ExprContext) maybeLeft.getParent();
                if (maybeAssign.binOp != null
                        && maybeAssign.binOp.getType() == LyraLexer.EQUALOP
                        && maybeAssign.expr(0) == maybeLeft) {
                    return true;
                }
            }
        }
        return false;
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
            } else if (var.getScope().isChildOf(methodSymbol)) {
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
            if (var.isClassField() || var.getScope().isChildOf(methodSymbol)) {
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
            if (var.getScope().isChildOf(methodSymbol)) {
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
                      "ldc %2$s" +
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
                "%3$s\n", methodStackUsage, methodLocalsUsage, body);

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

        writer.printf(".end method\n");

        methodSymbol = null;
        methodVars.clear();
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
