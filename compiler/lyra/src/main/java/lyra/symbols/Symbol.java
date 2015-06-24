package lyra.symbols;

import lyra.scopes.Scope;

public class Symbol { // A generic programming language symbol
    protected String name;      // All symbols at least have a name
    protected SymbolType symbolType;
    protected Scope scope;      // All symbols know what scope contains them.
    private Visibility visibility;

    public Symbol(String name, SymbolType type) { this.name = name; this.symbolType= type; }
    public String getName() { return name; }
    public Scope getScope() {return scope;}
    public void setScope(Scope s){ this.scope = s;}

    public String toString() {
        return symbolType.toString() + ":" + name;
    }

    public static String stripBrackets(String s) {
        return s.substring(1,s.length()-1);
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isChildOf(Scope scope) {
        return getScope() == scope || getScope().isChildOf(scope);
    }
}
