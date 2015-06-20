package lyra.symbols;

import lyra.SemanticErrorException;

import java.util.Collection;

/**
 * A symbol that we know to be a subclass of TypeSymbol, but we don't known
 * what is this symbol definition, its subclass and even if it exists.
 */
public class UnresolvedType  extends TypeSymbol {

    public UnresolvedType(String name) {
        super(name, SymbolType.UNRESOLVED_TYPE, null);
    }

    @Override
    public VariableSymbol resolveField(String name) {
        throw new UnresolvedTypeException("Tried to resolveField() on UnresolvedType");
    }

    @Override
    public MethodSymbol resolveOverload(String name, Collection<TypeSymbol> argTypes) {
        throw new UnresolvedTypeException("Tried to resolveOverload() on UnresolvedType");
    }

    @Override
    public String getBinaryNamePrefix() {
        throw new UnresolvedTypeException("Tried to getBinaryNamePrefix() on UnresolvedType");
    }

    @Override
    public boolean isA(TypeSymbol type) {
        throw new UnresolvedTypeException("Tried isA() on unresolved type");
    }

    @Override
    public boolean convertible(TypeSymbol type) {
        throw new UnresolvedTypeException("Tried convertible() on unresolved type");
    }

    @Override
    public boolean converts(TypeSymbol type) {
        throw new UnresolvedTypeException("Tried converts() on unresolved type");
    }

    @Override
    public void define(Symbol sym) throws SemanticErrorException {
        throw new UnresolvedTypeException("Tried to define a symbol inside an UnresolvedType");
    }

    @Override
    public Symbol shallowResolve(String name) {
        throw new UnresolvedTypeException("Tried to shallowResolve() on an UnresolvedType");
    }
}
