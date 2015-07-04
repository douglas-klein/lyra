package lyra.jasmin;

import lyra.symbols.ClassSymbol;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.symbols.SymbolTable;
import java.io.PrintWriter;

/**
 *
 */
public class DefaultConstructor extends IntraMethodCodeGenerator {
    ClassSymbol classSymbol;
    MethodSymbol symbol;
    IntraMethodCodeGenerator initializersGenerator;

    public DefaultConstructor(MethodSymbol constructor) {
        symbol = constructor;
        this.classSymbol = (ClassSymbol)constructor.getEnclosingScope();
    }

    public void setInitializers(IntraMethodCodeGenerator initializersGenerator) {
        this.initializersGenerator = initializersGenerator;
    }

    @Override
    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public void generate(PrintWriter writer, SymbolTable table) {
        MethodSymbol parent = classSymbol.getSuperClass().resolveOverload("constructor");
        methodHelper = new MethodHelper(writer, symbol, table);
        methodHelper.writeHeader();
        writer = methodHelper.createBodyWriter();
        methodHelper.writeVars();

        methodHelper.incStackUsage(1);
        writer.printf("aload 0\n" +
                "invokespecial %2$s\n", Utils.typeSpec(classSymbol), Utils.methodSpec(parent));
        methodHelper.decStackUsage(1);
        if (initializersGenerator != null) {
            initializersGenerator.setMethodHelper(methodHelper);
            initializersGenerator.generate(writer, table);
        }

        writer = methodHelper.writePreludeAndBody();
        methodHelper.writePrologue();
    }
}
