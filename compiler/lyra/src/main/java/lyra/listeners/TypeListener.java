package lyra.listeners;

import lyra.*;
import lyra.Compiler;
import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.LyraParser.VarDeclContext;
import lyra.LyraParser.VarDeclUnitContext;
import lyra.scopes.Scope;
import lyra.symbols.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
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
    public void exitNameFactor(LyraParser.NameFactorContext ctx) {
        Symbol symbol = currentScope.resolve(ctx.IDENT().getText());
        if (symbol != null) {
            if (!(symbol instanceof VariableSymbol)) {
                throw expectedVariableException(ctx.IDENT());
            } else {
                VariableSymbol var = (VariableSymbol) symbol;
                table.setNodeType(ctx, var.getType());
            }
        } else {
            Symbol thisSym = currentScope.resolve("this");
            if (thisSym == null) {
                throw  undefinedNameException(ctx.IDENT());
            }
            VariableSymbol me = (VariableSymbol) thisSym;
            MethodSymbol method = me.getType().resolveOverload(ctx.IDENT().getText(),
                    Collections.<TypeSymbol>emptyList());
            if (method == null) {
                throw noOverloadException(ctx.IDENT(), me.getType().getName(), ctx.IDENT().getText(),
                        Collections.<TypeSymbol>emptyList());
            }
            table.setNodeType(ctx, method.getReturnType());
        }
    }

    @Override
    public void exitObjectAlocExpr(LyraParser.ObjectAlocExprContext ctx) {
        Symbol symbol = currentScope.resolve(ctx.IDENT().getText());

        if(symbol == null || !(symbol instanceof TypeSymbol)){
            throw expectedTypeException(ctx.IDENT());
        }
        TypeSymbol typeSymbol = (TypeSymbol) symbol;

        List<TypeSymbol> types = getArgTypes(ctx.args());
        MethodSymbol constructor = typeSymbol.resolveOverload("constructor", types);
        if(constructor == null) {
            throw overloadNotFoundException(ctx.IDENT(), types);
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
        if(method == null) {
            throw  overloadNotFoundException(ctx.IDENT(), types);
        }
        table.setNodeType(ctx, method.getReturnType());
    }

    @Override
    public void exitNumberFactor(LyraParser.NumberFactorContext ctx) {
        table.setNodeType(ctx, (TypeSymbol) currentScope.resolve("Number"));
    }

    @Override
    public void exitUnaryexpr(LyraParser.UnaryexprContext ctx) {
        table.setNodeType(ctx, table.getNodeType(ctx.factor()));
    }

    @Override
    public void exitExpr(LyraParser.ExprContext ctx) {
        if (ctx.unaryexpr() != null) {
            table.setNodeType(ctx, table.getNodeType(ctx.unaryexpr()));
        } else if (ctx.binOp.getType() == LyraLexer.EQUALOP) {
            TypeSymbol left = table.getNodeType(ctx.expr(0));
            TypeSymbol right = table.getNodeType(ctx.expr(1));
            if (!right.convertible(left)) {
                throw notConvertibleException(ctx.expr(1), left, right);
            }
            table.setNodeType(ctx, left);
        }
    }

    @Override
    public void exitVarDecl(LyraParser.VarDeclContext ctx) {
    	TerminalNode typeNode = ctx.type().IDENT();
    	Symbol type = currentScope.resolve(typeNode.getText());
    	if(type == null || !(type instanceof TypeSymbol)){
    		throw expectedTypeException(typeNode);
    	}
    	for (VarDeclUnitContext varDeclUnit : ctx.varDeclUnit()) {
    		if(varDeclUnit.expr() != null){
    			TypeSymbol exprType = table.getNodeType(varDeclUnit.expr());
    			if(!exprType.convertible((TypeSymbol) type)){
    				throw notConvertibleException(varDeclUnit.expr(), exprType, type);
    			}
    		}
		}
    }
}
