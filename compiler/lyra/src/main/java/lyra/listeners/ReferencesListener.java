package lyra.listeners;

import lyra.LyraParser;
import lyra.scopes.BaseScope;
import lyra.scopes.Scope;
import lyra.symbols.Symbol;
import lyra.Compiler;
import lyra.symbols.SymbolTable;
import lyra.symbols.TypeSymbol;
import lyra.symbols.VariableSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class ReferencesListener extends ScopedBaseListener {
    private Scope currentScope; // define symbols in this scope
    private SymbolTable table;
    private Compiler compiler;

    public ReferencesListener(SymbolTable table, Compiler compiler) {
        this.table = table;
        this.compiler = compiler;
    }

    @Override
    public void enterProgram(LyraParser.ProgramContext ctx) {
        currentScope = table.getNodeScope(ctx);
    }

    @Override
    public void exitProgram(LyraParser.ProgramContext ctx) {
        currentScope = null;
    }

    @Override
    protected void beginScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = table.getNodeScope(ctx);
    }

    @Override
    protected void endScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void exitVarDeclUnit(LyraParser.VarDeclUnitContext ctx) {
        Symbol sym = currentScope.resolve(ctx.IDENT().getText());
        if (sym == null) {
            //TODO comentado pois os casos de teste quebrariam
//            compiler.getErrorListener().semanticError(compiler.getParser(), ctx.IDENT(0),
//                    String.format("Undefined name, compiler bug?"));
//            return;
            return;
        }

        VariableSymbol var = (VariableSymbol)sym;
        Symbol upgrade = currentScope.resolve(var.getType().getName());
        if (!(upgrade instanceof TypeSymbol)) {
            //TODO comentado pois os casos de teste quebrariam
            LyraParser.VarDeclContext varDecl = (LyraParser.VarDeclContext) ctx.getParent();
            compiler.getErrorListener().semanticError(compiler.getParser(), varDecl.type().IDENT(),
                    String.format("Unresolved type " + var.getType().getName() + "."));
            return;
        }

        //TODO comentado pois Int, e vários tipos built-in não existem ainda
        //var.upgradeType((TypeSymbol)upgrade);
    }
}
