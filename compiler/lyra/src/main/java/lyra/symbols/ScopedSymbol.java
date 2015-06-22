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

    @Override
    public boolean isChildOf(Scope maybeParent) {
        Scope parent = getEnclosingScope();
        while (parent != null) {
            if (parent == maybeParent)
                return true;
            parent = parent.getEnclosingScope();
        }
        return false;
    }
}
