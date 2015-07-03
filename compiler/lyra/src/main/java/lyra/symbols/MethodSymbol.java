package lyra.symbols;


import lyra.CodeGenerator;
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
    private LinkedHashMap<String, VariableSymbol> arguments = new LinkedHashMap<>();
    private ArrayList<TypeSymbol> cachedArgumentTypes;
    private boolean infix = false;
    private boolean _abstract = false;
    private TypeSymbol returnType;
    private BaseScope scope;
    private CodeGenerator generator = null;

    public MethodSymbol(String name, TypeSymbol returnType, Scope enclosingScope) {
        super(name, SymbolType.METHOD, enclosingScope);
        scope = new BaseScope(enclosingScope);
        this.returnType = returnType;
    }

    public void addArgument(VariableSymbol argument) {
        this.arguments.put(argument.getName(), argument);
        cachedArgumentTypes = null;
        scope.define(argument);
        argument.setScope(this);
    }

    @Override
    public void setScope(Scope s) {
        super.setScope(s);
        if (!(s instanceof TypeSymbol))
            throw new RuntimeException("Methods may only be placed in class or interface scope");
        define(new VariableSymbol("this", (TypeSymbol)s));
    }

    public VariableSymbol resolveArgument(String name) {
        return arguments.get(name);
    }

    public List<VariableSymbol> getArguments() {
        return arguments.values().stream().collect(Collectors.toList());
    }

    public TypeSymbol getReturnType(){
        return returnType;
    }

    public boolean isInfix() {
        return infix;
    }

    public void setInfix(boolean infix) {
        this.infix = infix;
    }

    public boolean isAbstract() {
		return _abstract;
	}

	public void setAbstract(boolean _abstract) {
		this._abstract = _abstract;
	}

    /**
     * Upgrade any UnresolvedType anywhere in this method that has the same qualified names as
     * type to use the given type object.
     *
     * @param type new TypeSymbol
     */
    public void upgradeType(TypeSymbol type) {
        String name = type.getQualifiedName();
        if (this.returnType.getQualifiedName().equals(name)) {
            this.returnType = type;
            cachedArgumentTypes = null;
        }
        for (VariableSymbol var : arguments.values()) {
            if (var.getType().getQualifiedName().equals(name)) {
                var.upgradeType(type);
                cachedArgumentTypes = null;
            }
        }
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
        sym.setScope(this); //I am the scope
    }

    @Override
    public Symbol resolve(String name) {
        return scope.resolve(name);
    }

    @Override
    public Symbol shallowResolve(String name) {
        return scope.shallowResolve(name);
    }

    public String getBinaryName() {
        if (isConstructor()) {
            return "<init>";
        }
        return "lyra_" + getName();
    }

    public boolean isConstructor() {
        return getName().equals("constructor");
    }

    public boolean isGenerated() {
        return generator != null;
    }
    public CodeGenerator getGenerator() {
        return generator;
    }
    public void setGenerator(CodeGenerator generator) {
        this.generator = generator;
    }
}
