package lyra.symbols;

import lyra.scopes.Scope;

public abstract class ScopedSymbol extends Symbol implements Scope {
    private Scope enclosingScope;

    public ScopedSymbol(String name, SymbolType symbolType, Scope enclosingScope) {
        super(name, symbolType);
        this.enclosingScope = enclosingScope;
    }

    @Override
    public Scope getEnclosingScope() { return enclosingScope; }

    public String getScopeName() { return getName(); }
}
