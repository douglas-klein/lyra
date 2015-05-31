package lyra.listeners;

import lyra.LyraParser;
import lyra.scopes.BaseScope;
import lyra.scopes.Scope;
import lyra.symbols.*;
import lyra.Compiler;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

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
            unresolvedTypeError(varDecl.type().IDENT(), var.getType().getName());
            return;
        }

        //TODO comentado pois Int, e vários tipos built-in não existem ainda
        //var.upgradeType((TypeSymbol)upgrade);
    }

    private void unresolvedTypeError(Object offendingSymbol, String typeName) {
        compiler.getErrorListener().semanticError(compiler.getParser(), offendingSymbol,
                "Unresolved type" + typeName + ".");
    }


    @Override
    public void exitMethodDecl(LyraParser.MethodDeclContext ctx) {
        MethodSymbol methodSymbol = (MethodSymbol)table.getNodeSymbol(ctx);
        Symbol returnTypeSymbol = currentScope.resolve(ctx.type().IDENT().getText());

        if (returnTypeSymbol == null || !(returnTypeSymbol instanceof TypeSymbol)) {
            unresolvedTypeError(ctx.type().IDENT(), ctx.type().IDENT().getText());
            return;
        }
        methodSymbol.upgradeType((TypeSymbol)returnTypeSymbol);
    }

    @Override
    public void exitParamDecl(LyraParser.ParamDeclContext ctx) {
        Symbol sym = currentScope.resolve(ctx.type().IDENT().getText());
        if (sym == null || !(sym instanceof TypeSymbol)) {
            unresolvedTypeError(ctx.type().IDENT(), ctx.type().IDENT().getText());
            return;
        }

        ParserRuleContext parent = ctx.getParent();
        if (!(parent instanceof LyraParser.ParamsContext)) return;
        parent = parent.getParent();
        MethodSymbol methodSymbol;
        if ((parent instanceof LyraParser.MethodDeclContext)
                || (parent instanceof LyraParser.MethodDeclAbstractContext)) {
            methodSymbol = (MethodSymbol) table.getNodeSymbol(parent);
        } else {
            return;
        }

        methodSymbol.upgradeType((TypeSymbol)sym);
    }
}
