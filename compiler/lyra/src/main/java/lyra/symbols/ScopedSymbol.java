package lyra.symbols;

import lyra.scopes.BaseScope;
import lyra.scopes.Scope;

import java.util.Map;

public abstract class ScopedSymbol extends Symbol implements Scope {
    BaseScope myScope;

    public ScopedSymbol(String name, SymbolType symbolType, Scope enclosingScope) {
        super(name, symbolType);
        this.myScope = new BaseScope(enclosingScope);
    }

    @Override
    public Symbol resolve(String name) { return myScope.resolve(name); }

    @Override
    public Symbol shallowResolve(String name) { return myScope.shallowResolve(name); }

    @Override
    public void define(Symbol sym) { myScope.define(sym); }

    @Override
    public Scope getEnclosingScope() { return myScope.getEnclosingScope(); }

    public String getScopeName() { return getName(); }
}
