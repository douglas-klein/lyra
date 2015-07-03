package lyra;

import lyra.symbols.Symbol;
import lyra.symbols.SymbolTable;

import java.io.PrintWriter;

/**
 * Outputs code directly to the generated code stream.
 */
public interface CodeGenerator {
    Symbol getSymbol();
    void generate(PrintWriter out, SymbolTable table);
}
