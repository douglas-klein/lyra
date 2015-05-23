package lyra.symbols;

import lyra.scopes.Scope;

/**
 * Umbrella class for all things that represent types: classes, interfaces and enums.
 */
public abstract class TypeSymbol extends ScopedSymbol {
    public TypeSymbol(String name, SymbolType symbolType, Scope enclosingScope) {
        super(name, symbolType, enclosingScope);
    }

    /**
     * Look for symbols first with inheritanceResolve(), then with super.resolve().
     *
     * @param name Name of the symbol looked for
     * @return Symbol instance found or null if not found
     */
    @Override
    public Symbol resolve(String name) {
        Symbol symbol = inheritanceResolve(name);
        if (symbol != null) return symbol;
        return super.resolve(name);
    }

    /**
     * Look for a symbol by its name only in the inheritance tree of this TypeSymbol: the scopes where the type and
     * its super types are defined is ignored.
     *
     * @param name Name of the symbol looked for.
     * @return Symbol instance found, or null.
     */
    public abstract Symbol inheritanceResolve(String name);

    @Override
    public String toString() {
        return getQualifiedName();
    }


    public String getQualifiedName() {
        String qualifiedName = "";
        Scope parent = getEnclosingScope();
        while (parent != null) {
            if (!parent.getScopeName().matches("^\\(.*\\)$"))
                qualifiedName = parent.getScopeName() + "." + qualifiedName;
            parent = parent.getEnclosingScope();
        }
        return qualifiedName;
    }
}
