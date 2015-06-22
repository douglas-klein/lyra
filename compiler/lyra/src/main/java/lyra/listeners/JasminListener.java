package lyra.listeners;

import lyra.Compiler;
import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.symbols.*;
import lyra.tokens.NumberToken;
import lyra.tokens.StringToken;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.*;
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
    private File file;
    private ByteArrayOutputStream methodOutputStream;
    private PrintWriter writer;
    private PrintWriter classWriter;

    private int methodStackUsage;
    private int methodLocalsUsage;
    /** Tracks stack usage as the method childs are visited, when this grows larger than
     *  methodLocalsUsage, methodLocalsUsage is updated. */
    private int methodCurrentStackUsage;

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

    private void incStackUsage(int count) {
        methodCurrentStackUsage += count;
        if (methodCurrentStackUsage > methodStackUsage)
            methodStackUsage = methodCurrentStackUsage;
    }
    private void decStackUsage(int count) {
        methodCurrentStackUsage -= count;
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

        MethodSymbol methodSymbol = (MethodSymbol) table.getNodeSymbol(ctx);
        List<TypeSymbol> args = methodSymbol.getArgumentTypes();

        methodLocalsUsage = 1 + args.size(); //"this" always present
        methodStackUsage = 0;
        methodCurrentStackUsage = 0;

        writer.printf(".method %1$s %2$s(", mapVisibility(methodSymbol.getVisibility()),
                methodSymbol.getBinaryName());

        for (TypeSymbol type : args) {
            writer.print(typeSpec(type));
        }
        writer.printf(")%1$s\n", typeSpec(methodSymbol.getReturnType()));

        methodOutputStream = new ByteArrayOutputStream();
        classWriter = writer;
        writer = new PrintWriter(methodOutputStream);
        writer.printf(".var 0 is this %1$s\n", typeSpec(classSymbol));
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
            } else {
                /* get the field of the stacked object, this will replace the currently
                 * stacked reference. */
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
            writer.printf("invokevirtual %1$s(%2$s)%3$s\n", method.getBinaryName(),
                    method.getArgumentTypes().stream().map(t -> typeSpec(t))
                            .reduce((a, b) -> a + b).orElse(""),
                    typeSpec(method.getReturnType()));
            /* pop object and arguments, leave a result */
            decStackUsage(1 + method.getArgumentTypes().size() - 1);
        }
    }

    private boolean isLeftOfAssignment(LyraParser.NameFactorContext ctx) {
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
        if (isLeftOfAssignment(ctx)) return;

        Symbol symbol = table.getNodeSymbol(ctx); //courtesy of TypeListener
        if (symbol instanceof MethodSymbol) {
            /* a method call to this without arguments */
            MethodSymbol method = (MethodSymbol) symbol;
            incStackUsage(1); //this will be pushed
            writer.printf("aload_0\n" + /* this is always var 0 */
                          "invokevirtual %1$s()%3$s\n",
                    method.getBinaryName(), typeSpec(method.getReturnType()));
            //this is replaced with the method return
        } else if (symbol instanceof VariableSymbol) {
            /* (class) field access, get the field value and stack it */
            VariableSymbol field = (VariableSymbol)symbol;
            if (field.isClassField() || field.getScope() == table.getGlobal()) {
                /* globals are static fields of lyra/runtime/Start */
                writer.printf("getstatic %1$s %2$s\n", field.getBinaryName(),
                        typeSpec(field.getType()));
            } else {
                incStackUsage(1); //this pushed
                writer.printf("aload_0\n" +
                              "getfield %1$s %2$s\n", field.getBinaryName(),
                                                      typeSpec(field.getType()));
                decStackUsage(1); //this popped
            }
            incStackUsage(1); //field value is pushed
        }
        /* symbol may also be a ClassSymbol, this is only valid as the child factor of an
         * memberFactor, and exitMemberFactor will output the code. */
    }

    @Override
    public void exitExpr(LyraParser.ExprContext ctx) {
        //TODO !!! handle assignment
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
                "%3$s\n" +
                "return\n" +
                ".end method\n", methodStackUsage, methodLocalsUsage, body);
        super.exitMethodDecl(ctx);
    }

    @Override
    public void exitClassBody(LyraParser.ClassBodyContext ctx) {
        classSymbol = null;
        endJasminFile();
    }
}
