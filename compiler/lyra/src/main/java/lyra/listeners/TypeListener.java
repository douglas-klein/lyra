package lyra.listeners;

import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.LyraParser.VarDeclUnitContext;
import lyra.symbols.*;

import lyra.tokens.NumberToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collections;
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
        table.setExprIsClassInstance(ctx, table.getExprIsClassInstance(ctx.expr()));
    }

    @Override
    public void exitNewfactor(LyraParser.NewfactorContext ctx) {
        table.setNodeType(ctx, table.getNodeType(ctx.alocExpr()));
    }

    @Override
    public void exitNameFactor(LyraParser.NameFactorContext ctx) {
        Symbol symbol = currentScope.resolve(ctx.IDENT().getText());
        if (symbol != null) {
            if (symbol instanceof VariableSymbol) {
                table.setNodeType(ctx, ((VariableSymbol)symbol).getType());
            } else if (symbol instanceof ClassSymbol) {
                table.setNodeType(ctx, (ClassSymbol)symbol);
                table.setExprIsClassInstance(ctx, true);
            } else {
                throw expectedVariableException(ctx.IDENT());
            }
        } else {
            /* it should be a this method call */

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
        if (ctx == null)
            return Collections.emptyList();
        return ctx.expr().stream().map(table::getNodeType).collect(Collectors.toList());
    }

    @Override
    public void exitMemberFactor(LyraParser.MemberFactorContext ctx) {
        TypeSymbol factorType = table.getNodeType(ctx.factor());
        List<TypeSymbol> types = getArgTypes(ctx.args());

        if (types.isEmpty()) {
            /* try field access before method call */
            VariableSymbol field = factorType.resolveField(ctx.IDENT().getText());
            if (field != null) {
                if (!field.isClassField() && table.getExprIsClassInstance(ctx.factor()))
                    throw expectedInstanceValue(ctx.factor());
                table.setNodeType(ctx, field.getType());
                return;
            }
        }

        /* method call */
        MethodSymbol method = factorType.resolveOverload(ctx.IDENT().getText(), types);
        if (method == null) {
            throw overloadNotFoundException(ctx.IDENT(), types);
        }
        table.setNodeType(ctx, method.getReturnType());
    }

    @Override
    public void exitNumberFactor(LyraParser.NumberFactorContext ctx) {
        String typeName = ((NumberToken) ctx.NUMBER().getSymbol()).getLyraTypeName();
        table.setNodeType(ctx, (TypeSymbol) currentScope.resolve(typeName));
    }

    @Override
    public void exitUnaryexpr(LyraParser.UnaryexprContext ctx) {
        table.setNodeType(ctx, table.getNodeType(ctx.factor()));
        table.setExprIsClassInstance(ctx, table.getExprIsClassInstance(ctx.factor()));
    }

    @Override
    public void exitExpr(LyraParser.ExprContext ctx) {
        if (ctx.unaryexpr() != null) {
            table.setNodeType(ctx, table.getNodeType(ctx.unaryexpr()));
            table.setExprIsClassInstance(ctx, table.getExprIsClassInstance(ctx.unaryexpr()));
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
    				throw notConvertibleException(varDeclUnit.expr(), type, exprType);
    			}
    		}
		}
    }

    @Override
    public void exitCasedecl(LyraParser.CasedeclContext ctx) {
        LyraParser.ExprContext switchExpr =
                ((LyraParser.SwitchstatContext)ctx.getParent().getParent()).expr();
        TypeSymbol switchType = table.getNodeType(switchExpr);
        TypeSymbol caseType = table.getNodeType(ctx.expr());
        if (!caseType.convertible(switchType))
            throw notConvertibleException(ctx.expr(), switchType, caseType);
    }
}
