package lyra.symbols;


import lyra.scopes.Scope;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Method Symbol, used for classes and interfaces.
 */
public class MethodSymbol extends ScopedSymbol {
    LinkedHashMap<String, VariableSymbol> arguments = new LinkedHashMap<>();
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

}