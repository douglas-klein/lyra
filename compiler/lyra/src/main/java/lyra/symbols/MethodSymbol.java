package lyra.symbols;


import lyra.scopes.Scope;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Method Symbol, used for classes and interfaces.
 */
public class MethodSymbol extends ScopedSymbol {
    LinkedHashMap<String, VariableSymbol> arguments = new LinkedHashMap<>();
    private boolean infix = false;
    TypeSymbol returnType;

    public MethodSymbol(String name, TypeSymbol returnType, Scope enclosingScope) {
        super(name, SymbolType.METHOD, enclosingScope);
        this.returnType = returnType;
    }

    public void addArgument(VariableSymbol argument) {
        this.arguments.put(argument.getName(), argument);
        define(argument);
    }

    public VariableSymbol resolveArgument(String name) {
        return arguments.get(name);
    }

    public Collection<VariableSymbol> getArguments() {
        return arguments.values();
    }

    public boolean isInfix() {
        return infix;
    }

    public void setInfix(boolean infix) {
        this.infix = infix;
    }

    /**
     * Upgrade any UnresolvedType anywhere in this method that has the same qualified names as
     * type to use the given type object.
     *
     * @param type new TypeSymbol
     */
    public void upgradeType(TypeSymbol type) {
        String name = type.getQualifiedName();
        if (this.returnType.getQualifiedName().equals(name))
            this.returnType = type;
        arguments.values().stream().filter(
                var -> var.getType().getQualifiedName().equals(name)
            ).forEach(var -> var.upgradeType(type));
    }
}