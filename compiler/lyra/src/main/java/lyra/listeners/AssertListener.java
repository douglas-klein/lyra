package lyra.listeners;

import lyra.LyraParser;
import lyra.symbols.ClassSymbol;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.symbols.TypeSymbol;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 */
public class AssertListener extends ScopedBaseListener {

    public AssertListener(lyra.Compiler compiler) {
        super(compiler);
    }

    @Override
    protected void beginScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = table.getNodeScope(ctx);
    }

    @Override
    protected void endScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    private void checkNodeIsConvertibleTo(ParserRuleContext ctx, TypeSymbol type) {
        TypeSymbol exprType = table.getNodeType(ctx);
        if (!exprType.convertible(type))
            reportSemanticException(notConvertibleException(ctx, type, exprType));
    }

    private void checkNodeIsConvertibleToPredefined(ParserRuleContext ctx, String typeName) {
        checkNodeIsConvertibleTo(ctx, table.getPredefinedClass(typeName));
    }

    @Override
    public void exitIfstat(LyraParser.IfstatContext ctx) {
        checkNodeIsConvertibleToPredefined(ctx.expr(), "Bool");
    }

    @Override
    public void exitForstat(LyraParser.ForstatContext ctx) {
        checkNodeIsConvertibleToPredefined(ctx.expr(0), "Bool");
    }

    @Override
    public void exitReturnstat(LyraParser.ReturnstatContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        while (parent != null && !(parent instanceof LyraParser.MethodDeclContext)) {
            parent = parent.getParent();
        }
        Symbol symbol = table.getNodeSymbol(parent);
        MethodSymbol method = (MethodSymbol) symbol;

        if (method.getReturnType().isA(table.getPredefinedClass("void"))) {
            if (ctx.expr() != null)
                reportSemanticException(returnWithExpressionInVoidMethod(ctx));
        } else {
            if (ctx.expr() == null) {
                reportSemanticException(returnWithoutExpression(ctx));
            } else {
                checkNodeIsConvertibleTo(ctx.expr(), method.getReturnType());
            }
        }

    }
}
