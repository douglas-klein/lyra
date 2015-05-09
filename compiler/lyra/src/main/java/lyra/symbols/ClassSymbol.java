package lyra.symbols;

/**
 * Symbol implementation for classes.
 */
import lyra.scopes.Scope;

import java.util.*;

public class ClassSymbol extends TypeSymbol {
    /** This is the superclass not enclosingScope field. We still record
     *  the enclosing scope so we can push in and pop out of class defs. */
    ClassSymbol superClass;

    /** Interface implemented by the class, may be null. */
    private InterfaceSymbol interfaceSymbol;

    /** List of all fields and methods */
    public Map<String,Symbol> members = new LinkedHashMap<>();

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
}
