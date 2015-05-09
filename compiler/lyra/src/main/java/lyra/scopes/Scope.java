package lyra.scopes;

import lyra.symbols.Symbol;

public interface Scope {
    String getScopeName();

    /** Where to look next for symbols;  */
    Scope getEnclosingScope();

    /** Define a symbol in the current scope */
    void define(Symbol sym);

    /** Look up name in this scope or in enclosing scope if not here */
    Symbol resolve(String name);

    /** Look up names in this scope, but not on enclosing. */
    Symbol shallowResolve(String name);
}