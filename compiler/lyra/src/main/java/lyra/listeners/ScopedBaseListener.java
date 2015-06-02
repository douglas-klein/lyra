package lyra.listeners;

import jdk.nashorn.internal.ir.TernaryNode;
import lyra.*;
import lyra.Compiler;
import lyra.LyraParser;
import lyra.scopes.Scope;
import lyra.symbols.MethodSymbol;
import lyra.symbols.SymbolTable;
import lyra.symbols.TypeSymbol;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Collections;

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
        compiler.getErrorListener().semanticError(compiler.getParser(), offendingSymbol,
                "Unresolved type" + typeName + ".");
    }

    protected void expectedTypeError(Object offendingSymbol) {
        compiler.getErrorListener().semanticError(compiler.getParser(), offendingSymbol,
                "Expected a type.");
    }

    protected void overloadNotFoundError(Object offendingSymbol, Collection<TypeSymbol> types) {
        String typeNames = types.stream().map(TypeSymbol::getQualifiedName).reduce("", (a, b) -> a + ", " + b);
        compiler.getErrorListener().semanticError(compiler.getParser(), offendingSymbol,
                "Overload not found for arguments: " + typeNames);
    }

}
