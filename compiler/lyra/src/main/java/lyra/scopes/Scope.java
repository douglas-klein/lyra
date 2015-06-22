package lyra.scopes;

import lyra.SemanticErrorException;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;

public interface Scope {
    String getScopeName();

    /** Where to look next for symbols;  */
    Scope getEnclosingScope();

    /** Define a symbol in the current scope */
    void define(Symbol sym) throws SemanticErrorException;

    /** Look up name in this scope or in enclosing scope if not here */
    Symbol resolve(String name);

    /** Look up names in this scope, but not on enclosing. */
    Symbol shallowResolve(String name);

    /** Returns true iff maybeParent is not this scope and encloses directly or indirectly it. */
    boolean isChildOf(Scope maybeParent);
}