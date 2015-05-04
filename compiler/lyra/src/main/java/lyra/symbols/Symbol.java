package lyra.symbols;

import lyra.scopes.Scope;

/**
 * Created by eduardo on 29/04/15.
 */
public class Symbol { // A generic programming language symbol
    protected String name;      // All symbols at least have a name
    protected Type type;
    protected Scope scope;      // All symbols know what scope contains them.

    public Symbol(String name) { this.name = name; }
    public Symbol(String name, Type type) { this(name); this.type = type; }
    public String getName() { return name; }
    public Scope getScope() {return scope;}
    public void setScope(Scope s){ this.scope = s;}

    public String toString() {
        String s = "";
        if ( scope!=null ) s = scope.getScopeName()+".";
        if ( type!=null ) return '<'+s+getName()+":"+type+'>';
        return s+getName();
    }

    public static String stripBrackets(String s) {
        return s.substring(1,s.length()-1);
    }
}
