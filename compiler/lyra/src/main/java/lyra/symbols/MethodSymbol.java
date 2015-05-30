package lyra.symbols;


import lyra.SemanticErrorException;
import lyra.scopes.BaseScope;
import lyra.scopes.Scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Method Symbol, used for classes and interfaces.
 */
public class MethodSymbol extends ScopedSymbol {
    LinkedHashMap<String, VariableSymbol> arguments = new LinkedHashMap<>();
    ArrayList<TypeSymbol> cachedArgumentTypes;
    private boolean infix = false;
    TypeSymbol returnType;
    BaseScope scope;

    public MethodSymbol(String name, TypeSymbol returnType, Scope enclosingScope) {
        super(name, SymbolType.METHOD, enclosingScope);
        scope = new BaseScope(enclosingScope);
        this.returnType = returnType;
    }

    public void addArgument(VariableSymbol argument) {
        this.arguments.put(argument.getName(), argument);
        cachedArgumentTypes = null;
        scope.define(argument);
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

    public List<TypeSymbol> getArgumentTypes() {
        if (cachedArgumentTypes != null) return cachedArgumentTypes;
        cachedArgumentTypes = new ArrayList<>(arguments.size());
        cachedArgumentTypes.addAll(arguments.values().stream()
                .map(VariableSymbol::getType)
                .collect(Collectors.toList()));
        return cachedArgumentTypes;
    }

    @Override
    public void define(Symbol sym) throws SemanticErrorException {
        scope.define(sym);
    }

    @Override
    public Symbol resolve(String name) {
        return scope.resolve(name);
    }

    @Override
    public Symbol shallowResolve(String name) {
        return scope.shallowResolve(name);
    }
}