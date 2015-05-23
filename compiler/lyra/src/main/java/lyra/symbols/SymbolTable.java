package lyra.symbols;

import lyra.scopes.BaseScope;
import lyra.scopes.Scope;
import lyra.symbols.predefined.*;
import lyra.symbols.predefined.Number;
import lyra.symbols.predefined.Object;
import lyra.symbols.predefined.Void;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.text.AttributedString;
import java.util.ArrayList;

/**
 * Holds together the global scope with predefined symbols the ParseTreeProperty that
 * relates parse tree nodes with their scope, and whatever else becomes related.
 */
public class SymbolTable {
    private BaseScope global = new BaseScope(null);
    private ParseTreeProperty<Scope> nodeScope = new ParseTreeProperty<>();
    private ArrayList<PredefinedSymbol> predefinedSymbols = new ArrayList<>();


    public SymbolTable() {
        global.setScopeName("(global)");
        predefinedSymbols.add(new Object());
        predefinedSymbols.add(new Void());
        predefinedSymbols.add(new Number());
        predefinedSymbols.add(new Int());
        predefinedSymbols.add(new Bool());
        predefinedSymbols.add(new LyraString());
        predefinedSymbols.add(new Output());
        predefinedSymbols.add(new Input());

        for (PredefinedSymbol s : predefinedSymbols)
            s.forward(global);
        for (PredefinedSymbol s : predefinedSymbols)
            s.resolveTypes(global);
    }

    public Scope getGlobal() {
        return global;
    }

    public void setNodeScope(ParseTree node, Scope scope) {
        nodeScope.put(node, scope);
    }
    public Scope getNodeScope(ParseTree node) {
        return nodeScope.get(node);
    }
}
