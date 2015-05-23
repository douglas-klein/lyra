package lyra.symbols;

/**
 * Symbol implementation for classes.
 */
import lyra.scopes.Scope;
import org.omg.CosNaming.NamingContextPackage.InvalidNameHolder;

import java.util.*;

public class ClassSymbol extends TypeSymbol {
    /** This is the superclass not enclosingScope field. We still record
     *  the enclosing scope so we can push in and pop out of class defs. */
    ClassSymbol superClass;

    /** Interface implemented by the class, may be null. */
    private InterfaceSymbol interfaceSymbol;

    /** List of all fields and methods */
    public Map<String,Symbol> members = new LinkedHashMap<>();
    private boolean aFinal;
    private boolean aAbstract;

    public ClassSymbol(String name, Scope enclosingScope, ClassSymbol superClass) {
        super(name, SymbolType.CLASS, enclosingScope);
        this.superClass = superClass;
    }

    @Override
    public Symbol inheritanceResolve(String name) {
        Symbol symbol = members.get(name);
        if (symbol != null) return symbol;
        if (superClass != null) {
            symbol = superClass.inheritanceResolve(name);
            if (symbol != null) return symbol;
        }
        if (interfaceSymbol != null) {
            symbol = interfaceSymbol.inheritanceResolve(name);
            if (symbol != null) return symbol;
        }
        return null;
    }

    @Override
    public void define(Symbol sym) {
        members.put(sym.getName(), sym);
        super.define(sym);
    }

    public InterfaceSymbol getInterfaceSymbol() {
        return interfaceSymbol;
    }

    public void setInterfaceSymbol(InterfaceSymbol interfaceSymbol) {
        this.interfaceSymbol = interfaceSymbol;
    }

    public boolean isFinal() {
        return aFinal;
    }

    public void setFinal(boolean aFinal) {
        this.aFinal = aFinal;
    }

    public void setAbstract(boolean anAbstract) {
        this.aAbstract = anAbstract;
    }

    public boolean isAbstract() {
        return aAbstract;
    }

    /**
     * Replaces any type used inside this class (recursing into methods) that has the same
     * qualified name as the given type with the given type.
     *
     * @param type new TypeSymbol
     */
    public void upgradeType(TypeSymbol type) {
        if (interfaceSymbol != null) {
            if (interfaceSymbol.getQualifiedName().equals(type.getQualifiedName()))
                interfaceSymbol = (InterfaceSymbol) type;
        }

        for (Symbol symbol : members.values()) {
            if (symbol instanceof MethodSymbol)
                ((MethodSymbol)symbol).upgradeType(type);
            if (symbol instanceof VariableSymbol)
                ((VariableSymbol)symbol).upgradeType(type);
        }
    }
}
