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

    public ReferencesListener(Compiler compiler) {
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

    @Override
    public void exitType(LyraParser.TypeContext ctx) {
        /* Array types were rewritten into X$Array strings. Now we should be able to resolve X,
         * so we detect strings of this form, and ask for the SymbolTable's ArrayClassFactory
         * to get us the appropriate ClassSymbol. */
        String name = ctx.IDENT().getText();
        ArrayClassFactory factory = table.getArrayClassFactory();
        String elementTypeName = factory.getElementTypeFromArrayTypeName(name);
        int dimensions = factory.getDimensionsFromArrayTypeName(name);
        if (elementTypeName == null || dimensions < 1) {
            return; //does not apply
        }

        Symbol symbol = currentScope.resolve(elementTypeName);
        if (symbol == null || !(symbol instanceof TypeSymbol)) {
            expectedTypeError(ctx.IDENT());
            return;
        }

        /* factory will insert the generated array class into the appropriate scope, when
         * visiting our parent node, the usual treatment will find the generated class */
        factory.getArrayOf((TypeSymbol)symbol, dimensions);
    }

    @Override
    public void exitVarDeclUnit(LyraParser.VarDeclUnitContext ctx) {
        Symbol sym = currentScope.resolve(ctx.IDENT().getText());
        if (sym == null) {
            compiler.getErrorListener().semanticError(compiler.getParser(), ctx.IDENT(),
                    String.format("Undefined name, compiler bug?"));
            return;
        }

        VariableSymbol var = (VariableSymbol)sym;
        Symbol upgrade = currentScope.resolve(var.getType().getName());
        if (!(upgrade instanceof TypeSymbol)) {
            LyraParser.VarDeclContext varDecl = (LyraParser.VarDeclContext) ctx.getParent();
            unresolvedTypeError(varDecl.type().IDENT(), var.getType().getName());
            return;
        }

        var.upgradeType((TypeSymbol)upgrade);
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
