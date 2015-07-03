package lyra.jasmin;

import lyra.CodeGenerator;
import lyra.symbols.ClassSymbol;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.symbols.SymbolTable;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collections;

/**
 *
 */
public class DefaultConstructor implements CodeGenerator {
    ClassSymbol classSymbol;
    MethodSymbol symbol;

    public DefaultConstructor(MethodSymbol constructor) {
        symbol = constructor;
        this.classSymbol = (ClassSymbol)constructor.getEnclosingScope();
    }

    @Override
    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public void generate(PrintWriter writer, SymbolTable table) {
        MethodSymbol parent = classSymbol.getSuperClass().resolveOverload("constructor");
        writer.printf(".method public <init>()V\n" +
                ".limit stack 1\n" +
                ".limit locals 1\n" +
                ".var 0 is this %1$s\n" +
                "aload_0\n" +
                "invokespecial %2$s\n" +
                "return\n" +
                ".end method\n", Utils.typeSpec(classSymbol), Utils.methodSpec(parent));

    }
}
