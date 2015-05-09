package lyra.symbols;

import lyra.scopes.Scope;

import java.util.LinkedHashMap;

/**
 * A symbol that we know to be a subclass of TypeSymbol, but we don't known
 * what is this symbol definition, its subclass and even if it exists.
 */
public class UnresolvedType  extends TypeSymbol {
    private LinkedHashMap<String, Symbol> members = new LinkedHashMap<>();

    public UnresolvedType(String name) {
        super(name, SymbolType.UNRESOLVED_TYPE, null);
    }

    @Override
    public Symbol inheritanceResolve(String name) {
        return null; //nothing to resolve
        //TODO throw exception?
    }

}
