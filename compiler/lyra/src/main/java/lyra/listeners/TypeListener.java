package lyra.listeners;

import lyra.*;
import lyra.Compiler;
import lyra.LyraParser;
import lyra.scopes.Scope;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.symbols.SymbolTable;
import lyra.symbols.TypeSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeListener extends ScopedBaseListener {

    public TypeListener(lyra.Compiler compiler){
        super(compiler);
    }

    @Override
    public void enterProgram(LyraParser.ProgramContext ctx) {
        currentScope = table.getNodeScope(ctx);
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
    public void exitBoolFactor(LyraParser.BoolFactorContext ctx) {
        table.setNodeType(ctx, (TypeSymbol) currentScope.resolve("Bool"));
    }

    @Override
    public void exitNullFactor(LyraParser.NullFactorContext ctx) {
        table.setNodeType(ctx, (TypeSymbol)currentScope.resolve("Object"));
    }

    @Override
    public void exitStringFactor(LyraParser.StringFactorContext ctx) {
        table.setNodeType(ctx, (TypeSymbol)currentScope.resolve("String"));
    }

    @Override
    public void exitWrappedFactor(LyraParser.WrappedFactorContext ctx) {
        table.setNodeType(ctx, table.getNodeType(ctx.expr()));
    }

    @Override
    public void exitNewfactor(LyraParser.NewfactorContext ctx) {
        table.setNodeType(ctx, table.getNodeType(ctx.alocExpr()));
    }

    @Override
    public void exitObjectAlocExpr(LyraParser.ObjectAlocExprContext ctx) {
        Symbol symbol = currentScope.resolve(ctx.IDENT().getText());

        if(symbol == null || !(symbol instanceof TypeSymbol)){
            expectedTypeError(ctx.IDENT());
        }
        TypeSymbol typeSymbol = (TypeSymbol) symbol;

        List<TypeSymbol> types = getArgTypes(ctx.args());
        MethodSymbol constructor = typeSymbol.resolveOverload("constructor", types);
        if(constructor == null){
            overloadNotFoundError(ctx.IDENT(), types);
        }

        table.setNodeType(ctx, typeSymbol);
    }

    private List<TypeSymbol> getArgTypes(LyraParser.ArgsContext ctx) {
        return ctx.expr().stream()
                .map(table::getNodeType)
                .collect(Collectors.toList());
    }

    @Override
    public void exitMemberFactor(LyraParser.MemberFactorContext ctx) {
        TypeSymbol factor = table.getNodeType(ctx.factor());

        List<TypeSymbol> types = getArgTypes(ctx.args());
        MethodSymbol method = factor.resolveOverload(ctx.IDENT().getText(), types);
        if(method== null){
            overloadNotFoundError(ctx.IDENT(), types);
        }
        table.setNodeType(ctx, method.getReturnType());
    }


    @Override
    public void exitArrayAlocExpr(LyraParser.ArrayAlocExprContext ctx) {
        // TODO
    }

    @Override
    public void exitNumberFactor(LyraParser.NumberFactorContext ctx) {
        table.setNodeType(ctx, (TypeSymbol) currentScope.resolve("Number"));
    }
}
