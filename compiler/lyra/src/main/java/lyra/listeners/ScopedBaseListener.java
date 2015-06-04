package lyra.listeners;

import java.util.Collection;

import lyra.Compiler;
import lyra.LyraParser;
import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.Symbol;
import lyra.symbols.SymbolTable;
import lyra.symbols.TypeSymbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Helper base class that, for all scope-creating rules, calls abstract methods
 * when entering and exiting those rules.
 *
 * This allows for a single pair of beginScopeVisit() endScopeVisit() instead of
 * overriding methods for all rules.
 */
public abstract class ScopedBaseListener extends lyra.LyraParserBaseListener {

    protected Compiler compiler;
    protected Scope currentScope; // define symbols in this scope
    protected SymbolTable table;

    protected ScopedBaseListener(lyra.Compiler compiler){
        this.compiler = compiler;
        this.table = compiler.getSymbolTable();
    }

    @Override
    public void enterProgram(LyraParser.ProgramContext ctx) {
        currentScope = table.getGlobal();
    }

    @Override
    public void exitProgram(LyraParser.ProgramContext ctx) {
        currentScope = null;
    }

    protected void reportSemanticException(TerminalNode node, SemanticErrorException e) {
        e.setOffendingSymbol(node);
        compiler.getErrorListener().semanticError(compiler.getParser(), e);
    }

    @Override
    public void enterMethodDecl(LyraParser.MethodDeclContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitMethodDecl(LyraParser.MethodDeclContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterMethodDeclAbstract(LyraParser.MethodDeclAbstractContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitMethodDeclAbstract(LyraParser.MethodDeclAbstractContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterClassdecl(LyraParser.ClassdeclContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitClassdecl(LyraParser.ClassdeclContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterInterfacedecl(LyraParser.InterfacedeclContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitInterfacedecl(LyraParser.InterfacedeclContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterScopestat(LyraParser.ScopestatContext ctx) {
        beginScopeVisit(false, ctx);
    }
    @Override
    public void exitScopestat(LyraParser.ScopestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterForstat(LyraParser.ForstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitForstat(LyraParser.ForstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterWhilestat(LyraParser.WhilestatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitWhilestat(LyraParser.WhilestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterForever(LyraParser.ForeverContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitForever(LyraParser.ForeverContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterSwitchstat(LyraParser.SwitchstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitSwitchstat(LyraParser.SwitchstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterIfstat(LyraParser.IfstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitIfstat(LyraParser.IfstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterElsestat(LyraParser.ElsestatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitElsestat(LyraParser.ElsestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    protected abstract void beginScopeVisit(boolean named, ParserRuleContext ctx);
    protected abstract void endScopeVisit(boolean named, ParserRuleContext ctx);


    protected void unresolvedTypeError(Object offendingSymbol, String typeName) {
        compiler.getErrorListener()
        		.semanticError(compiler.getParser(), 
        					   offendingSymbol,
        					   "Unresolved type" + typeName + ".");
    }

    protected void expectedTypeError(Object offendingSymbol) {
        compiler.getErrorListener()
        		.semanticError(compiler.getParser(), 
        		offendingSymbol,
                "Expected a type.");
    }

    protected void overloadNotFoundError(Object offendingSymbol, Collection<TypeSymbol> types) {
        String typeNames = types.stream().map(TypeSymbol::getQualifiedName).reduce("", (a, b) -> a + ", " + b);

        compiler.getErrorListener()
        		.semanticError(compiler.getParser(), 
        					   offendingSymbol,
        					   "Overload not found for arguments: " + typeNames);
    }

    protected void notConvertibleError(Object offendingSymbol, Symbol type) {
        compiler.getErrorListener()
        	    .semanticError(compiler.getParser(), 
        	    			   offendingSymbol, 
        	    			   "Type is not convertible to: " + type.getName());
    }

}
