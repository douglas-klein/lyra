package lyra.listeners;

import lyra.Compiler;
import lyra.LyraParser;
import lyra.symbols.*;
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

    private File file;
    private PrintWriter writer;

    private int methodStackUsage;
    private int methodLocalsUsage;
    /** Tracks stack usage as the method childs are visited, when this grows larger than
     *  methodLocalsUsage, methodLocalsUsage is updated. */
    private int methodCurrentStackUsage;
    private String methodJasminBody;

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
        spec += type.getBinaryName();
        return spec;
    }

    private void incStackUsage() {
        ++methodCurrentStackUsage;
        if (methodCurrentStackUsage > methodStackUsage)
            methodStackUsage = methodCurrentStackUsage;
    }
    private void decStackUsage() {
        --methodCurrentStackUsage;
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
        ClassSymbol classSymbol = (ClassSymbol) table.getNodeSymbol(parent);
        createJasminFile(classSymbol.getName());
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

        methodJasminBody = "";
        methodLocalsUsage = 1 + args.size(); //"this" always present
        methodStackUsage = 0;
        methodCurrentStackUsage = 0;

        writer.printf(".method %1$s %2$s(", mapVisibility(methodSymbol.getVisibility()),
                methodSymbol.getBinaryName());

        for (TypeSymbol type : args) {
            writer.print(typeSpec(type) + ";");
        }
        writer.printf(")%1$s;\n", typeSpec(methodSymbol.getReturnType()));
    }

    @Override
    public void exitMethodDecl(LyraParser.MethodDeclContext ctx) {
        writer.printf(".limit stack %1$d\n" +
                      ".limit locals %2$d\n" +
                      "%3$s\n" +
                      ".end method\n", methodStackUsage, methodLocalsUsage, methodJasminBody);
        super.exitMethodDecl(ctx);
    }

    @Override
    public void exitClassBody(LyraParser.ClassBodyContext ctx) {
        endJasminFile();

    }
}
