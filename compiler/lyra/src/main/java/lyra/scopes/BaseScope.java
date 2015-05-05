package lyra.scopes;

/**
 * Created by eduardo on 29/04/15.
 */
import lyra.symbols.Symbol;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseScope implements Scope {
    Scope enclosingScope; // null if global (outermost) scope
    Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();

    public BaseScope(Scope parent) { this.enclosingScope = parent;	}

    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if ( s!=null ) return s;
        // if not here, check any enclosing scope
        if ( enclosingScope != null ) return enclosingScope.resolve(name);
        return null; // not found
    }

    public void define(Symbol sym) {
        symbols.put(sym.getName(), sym);
        sym.setScope(this); // track the scope in each symbol
    }

    public Scope getEnclosingScope() { return enclosingScope; }

    public String toString() { return symbols.keySet().toString(); }

    public String getScopeName(){return "(anonymous)";};
}
