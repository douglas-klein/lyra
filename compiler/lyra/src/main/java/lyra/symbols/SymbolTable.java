package lyra.symbols;

import lyra.CodeGenerator;
import lyra.LyraParser;
import lyra.SemanticErrorException;
import lyra.jasmin.ArrayGenerator;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds together the global scope with predefined symbols the ParseTreeProperty that
 * relates parse tree nodes with their scope, and whatever else becomes related.
 */
public class SymbolTable {
    private BaseScope global = new BaseScope(null);
    private ParseTreeProperty<Scope> nodeScope = new ParseTreeProperty<>();
    private ParseTreeProperty<TypeSymbol> nodeType = new ParseTreeProperty<>();
    private ParseTreeProperty<Symbol> nodeSymbol = new ParseTreeProperty<>();
    private HashMap<Symbol, ParseTree> symbolNode = new HashMap<>();
    private ParseTreeProperty<Boolean> exprIsClassInstance = new ParseTreeProperty<>();
    private ParseTreeProperty<Boolean> methodHasExplicitSuper = new ParseTreeProperty<>();
    private ArrayClassFactory arrayClassFactory = new ArrayClassFactory(this);

    private ArrayList<PredefinedSymbol> predefinedSymbols = new ArrayList<>();

    public SymbolTable() {
        global.setScopeName("(global)");
        predefinedSymbols.add(new Object());
        predefinedSymbols.add(new Void());
        predefinedSymbols.add(new Number());
        predefinedSymbols.add(new Int());
        predefinedSymbols.add(new Bool());
        predefinedSymbols.add(new LyraString());
        predefinedSymbols.add(new Array());
        predefinedSymbols.add(new Output());
        predefinedSymbols.add(new Input());
        predefinedSymbols.add(new Random());

        for (PredefinedSymbol s : predefinedSymbols)
            s.forward(global);
        for (PredefinedSymbol s : predefinedSymbols)
            s.resolveTypes(global);
    }

    public Scope getGlobal() {
        return global;
    }

    public ClassSymbol getPredefinedClass(String name) {
        Symbol symbol = getGlobal().resolve(name);
        if (symbol == null)
            throw new SemanticErrorException("Predefined symbol " + name + " is not defined!");
        if (!(symbol instanceof ClassSymbol)) {
            throw new SemanticErrorException("Predefined class symbol " + name + " has been " +
                    "redefined to something else");
        }
        return (ClassSymbol)symbol;
    }

    public void setNodeScope(ParseTree node, Scope scope) {
        nodeScope.put(node, scope);
    }
    public Scope getNodeScope(ParseTree node) {
        return nodeScope.get(node);
    }

    public void setNodeType(ParseTree node, TypeSymbol symbol) {
        nodeType.put(node, symbol);
    }
    public TypeSymbol getNodeType(ParseTree node) {return nodeType.get(node);}

    public void setNodeSymbol(ParseTree node, Symbol symbol) {
        nodeSymbol.put(node, symbol);
        if (symbol != null)
            symbolNode.put(symbol, node);
    }
    public Symbol getNodeSymbol(ParseTree node) { return nodeSymbol.get(node);}

    public ArrayClassFactory getArrayClassFactory() {
        return arrayClassFactory;
    }

    public boolean getExprIsClassInstance(ParseTree node) {
        Boolean is = exprIsClassInstance.get(node);
        return is != null && is.booleanValue();
    }
    public void setExprIsClassInstance(ParseTree node, boolean value) {
        exprIsClassInstance.put(node, value);
    }

    public void setSymbolNode(Symbol symbol, ParseTree node) {
        nodeSymbol.put(node, symbol);
        if (node != null) symbolNode.put(symbol, node);
    }
    public ParseTree getSymbolNode(Symbol symbol) { return symbolNode.get(symbol); }

    public List<CodeGenerator> getGeneratedClassesGenerators() {
        return arrayClassFactory.getAllGeneratedArrays().stream()
                .map(a -> new ArrayGenerator(a)).collect(Collectors.toList());
    }

    public boolean getMethodHasExplicitSuper(ParseTree node) {
        Boolean has = methodHasExplicitSuper.get(node);
        return has != null && has.booleanValue();
    }
    public void setMethodHasExplicitSuper(ParseTree node) {
        methodHasExplicitSuper.put(node, Boolean.TRUE);
    }
}
