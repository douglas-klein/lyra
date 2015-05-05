package lyra.symbols;

/**
 * Symbol implementation for classes.
 */
import lyra.scopes.Scope;

import java.util.*;

public class ClassSymbol extends ScopedSymbol implements Scope {
    /** This is the superclass not enclosingScope field. We still record
     *  the enclosing scope so we can push in and pop out of class defs.
     */
    ClassSymbol superClass;
    /** List of all fields and methods */
    public Map<String,Symbol> members = new LinkedHashMap<>();

    public ClassSymbol(String name, Scope enclosingScope, ClassSymbol superClass) {
        super(name, enclosingScope);
        this.superClass = superClass;
    }

    public Scope getParentScope() {
        if ( superClass==null ) return enclosingScope; // globals
        return superClass; // if not root object, return super
    }

    @Override
    public Symbol resolve(String name) {
        //Look first on the super class, fallback to enclosing scope if failed.
        Symbol result = getParentScope().resolve(name);
        return result != null ? result : super.resolve(name);
    }

    public Map<String, Symbol> getMembers() { return members; }
    public String toString() {
        return "class "+name+":{"+
                stripBrackets(members.keySet().toString())+"}";
    }
}
