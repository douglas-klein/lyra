package lyra.jasmin;
 
import lyra.CodeGenerator;
import lyra.symbols.ClassSymbol;
import lyra.symbols.Symbol;
import lyra.symbols.SymbolTable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 *
 *
 */
public class StaticInitializerGenerator implements CodeGenerator {
    ClassSymbol classSymbol;
    HashSet<ClassSymbol> classes = new HashSet<>();

    public StaticInitializerGenerator(SymbolTable table, Collection<ClassSymbol> classes) {
        classSymbol = new ClassSymbol("StaticInitializer", table.getGlobal(), table.getPredefinedClass("Object"));
        this.classes.addAll(classes);
    }

    @Override
    public Symbol getSymbol() {
        return classSymbol;
    }

    @Override
    public void generate(PrintWriter out, SymbolTable table) {
        out.printf(".method public <init>()V\n" +
                        ".limit stack 1\n" +
                        ".limit locals 1\n" +
                        ".var 0 is this Llyra/user/StaticInitializer;\n" +
                        "aload 0\n" +
                        "invokespecial lyra/runtime/Object/<init>()V\n" +
                        "return\n" +
                        ".end method\n\n"
        );
        out.printf(".method public static staticInit()V\n" +
                        ".limit stack 0\n" +
                        ".limit locals 0\n"
        );
        for (ClassSymbol c : classes) out.printf("invokestatic %1$s/staticInit()V\n", c.getBinaryName());
        out.printf("return\n" +
                        ".end method\n\n"
        );
    }
 }