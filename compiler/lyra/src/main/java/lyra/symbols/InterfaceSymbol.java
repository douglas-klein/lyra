package lyra.symbols;

import lyra.scopes.Scope;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Symbol for interfaces (interfacedecl on the grammar).
 */
public class InterfaceSymbol extends ScopedSymbol implements Scope {
    LinkedHashMap<String, Symbol> members = new LinkedHashMap<>();

    public InterfaceSymbol(String name, Scope enclosingScope) {
        super(name, enclosingScope);
    }

    @Override
    public Map<String, Symbol> getMembers() {
        return members;
    }
}
