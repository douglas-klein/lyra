package lyra.symbols;

import lyra.scopes.Scope;

import java.sql.Types;
import java.util.Collection;

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
        Symbol symbol = resolveField(name);
        if (symbol != null) return symbol;
        return getEnclosingScope().resolve(name);
    }

    /**
     * Look for a field by its name only in the inheritance tree of this TypeSymbol: the scopes
     * where the type and its super types are defined are ignored.
     *
     * @param name Name of the symbol looked for.
     * @return VariableSymbol instance found, or null.
     */
    public abstract VariableSymbol resolveField(String name);


    /**
     * Look for a MethodSymbol on this class or in super types that has the given name and
     * expects a list of arguments which are compatible with \p argTypes.
     *
     * Check the algorithm on the project wiki or on the ClassSymbol implementation.
     *
     * @param name Name of the method
     * @param argTypes List of types of the arguments the caller can provide.
     * @return MethodSymbol that is a unambiguous match or null if none or more than one match is
     *         found.
     */
    public abstract MethodSymbol resolveOverload(String name, Collection<TypeSymbol> argTypes);


    @Override
    public String toString() {
        return getQualifiedName();
    }


    public String getQualifiedName() {
        String qualifiedName = getName();
        Scope parent = getEnclosingScope();
        while (parent != null) {
            if (!parent.getScopeName().matches("^\\(.*\\)$"))
                qualifiedName = parent.getScopeName() + "." + qualifiedName;
            parent = parent.getEnclosingScope();
        }
        return qualifiedName;
    }

    public abstract boolean isA(TypeSymbol type);

    public boolean convertible(TypeSymbol type) {
        return type.isA(this) ? true : type.converts(this);
    }

    public abstract boolean converts(TypeSymbol type);
}
