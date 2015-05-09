package lyra.symbols;

import lyra.scopes.Scope;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Symbol for interfaces (interfacedecl on the grammar).
 */
public class InterfaceSymbol extends TypeSymbol {
    private LinkedHashMap<String, Symbol> members = new LinkedHashMap<>();

    public InterfaceSymbol(String name, Scope enclosingScope) {
        super(name, SymbolType.INTERFACE, enclosingScope);
    }

    @Override
    public Symbol inheritanceResolve(String name) {
        return null;
    }

    @Override
    public void define(Symbol sym) {
        members.put(sym.getName(), sym);
        super.define(sym);
    }
}
