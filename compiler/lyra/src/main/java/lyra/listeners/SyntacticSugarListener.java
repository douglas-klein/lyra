package lyra.listeners;

import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.LyraParserBaseListener;
import lyra.LyraParser.VarDeclContext;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.HashSet;
import java.util.ListIterator;

/**
 *
 */
public class SyntacticSugarListener extends TreeRewriterBaseListener {
    @Override
    public void exitWhilestat(LyraParser.WhilestatContext ctx) {
        ParserRuleContext parent = (ParserRuleContext)ctx.parent;
        LyraParser.ForstatContext replacement = new LyraParser.ForstatContext(parent, -1);
        //'for' varDecl?  ';'  expr ';' expr?  '{' statlist '}';

        replacement.addChild(new CommonToken(LyraLexer.FOR, "for"));
        replacement.addChild(new CommonToken(LyraLexer.SEMICOLON, ";"));

        LyraParser.ExprContext expr = ctx.expr();
        expr.parent = replacement;
        replacement.addChild(expr);

        replacement.addChild(new CommonToken(LyraLexer.SEMICOLON, ";"));
        replacement.addChild(new CommonToken(LyraLexer.LEFTCURLYBRACE, "{"));

        LyraParser.StatlistContext statlist = ctx.statlist();
        statlist.parent = replacement;
        replacement.addChild(statlist);

        replacement.addChild(new CommonToken(LyraLexer.RIGHTCURLYBRACE, "}"));

        replaceChild(ctx, parent, replacement);
    }

    @Override
    public void exitForever(LyraParser.ForeverContext ctx) {
        LyraParser.ForstatContext rewritten = new LyraParser.ForstatContext(ctx.getParent(), -1);

        rewritten.addChild(new CommonToken(LyraLexer.FOR, "for"));
        rewritten.addChild(new CommonToken(LyraLexer.SEMICOLON, ";"));

        LyraParser.ExprContext expr = new LyraParser.ExprContext(rewritten, -1);
        LyraParser.UnaryexprContext uExpr = new LyraParser.UnaryexprContext(expr, -1);
        LyraParser.BoolFactorContext factor = new LyraParser.BoolFactorContext(
                new LyraParser.FactorContext(uExpr, -1));
        factor.addChild(new CommonToken(LyraLexer.TRUE, "true"));
        uExpr.addChild(factor);
        expr.addChild(uExpr);
        rewritten.addChild(expr);

        rewritten.addChild(new CommonToken(LyraLexer.SEMICOLON, ";"));
        rewritten.addChild(new CommonToken(LyraLexer.LEFTCURLYBRACE, "{"));

        LyraParser.StatlistContext statlist = ctx.statlist();
        statlist.parent = rewritten;
        rewritten.addChild(statlist);

        rewritten.addChild(new CommonToken(LyraLexer.RIGHTCURLYBRACE, "}"));

        replaceChild(ctx, ctx.getParent(), rewritten);
    }

    @Override
    public void exitThisMethodFactor(LyraParser.ThisMethodFactorContext ctx) {
        LyraParser.MemberFactorContext rewritten = new LyraParser.MemberFactorContext(new LyraParser.FactorContext(ctx.getParent(), -1));

        LyraParser.NameFactorContext _this = new LyraParser.NameFactorContext(new LyraParser.FactorContext(rewritten, -1));
        _this.addChild(new CommonToken(LyraLexer.IDENT, "this"));
        rewritten.addChild(_this);

        rewritten.addChild(new CommonToken(LyraLexer.DOT, "."));
        rewritten.addChild(new CommonToken(LyraLexer.IDENT, ctx.IDENT().getText()));
        if(ctx.LEFTPARENTHESES() != null){
            rewritten.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));
            LyraParser.ArgsContext args = ctx.args();
            args.parent = rewritten;
            rewritten.addChild(args);
            rewritten.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));
        }

        replaceChild(ctx, ctx.getParent(), rewritten);
    }

    @Override
    public void exitMethodDecl(LyraParser.MethodDeclContext ctx) {
        if (ctx.type() != null)  return;

        /* default return type is void */
        LyraParser.TypeContext type = new LyraParser.TypeContext(ctx, -1);
        type.addChild(new CommonToken(LyraLexer.IDENT, "void"));
        ctx.addChild(type);
    }

    @Override
    public void exitMethodDeclAbstract(LyraParser.MethodDeclAbstractContext ctx) {
        if (ctx.type() != null) return;

        LyraParser.TypeContext type = new LyraParser.TypeContext(ctx, -1);
        type.addChild(new CommonToken(LyraLexer.IDENT, "void"));
        ctx.addChild(type);
    }

    @Override
    public void exitExpr(LyraParser.ExprContext ctx) {
        if (ctx.unaryexpr() != null)
            return; //handled at exitUnaryexpr
        if (ctx.binOp.getType() == LyraLexer.EQUALOP) {
            return; //handled by ArrayGeneratorListener
        }

        /* input has the form rewritten : rewritten opBin rewritten
         * get appropriate method name and rewrite to (rewritten(0)).method(rewritten(1)) */

        ParserRuleContext parent = ctx.getParent();
        LyraParser.ExprContext rewritten = new LyraParser.ExprContext(parent, -1);
        LyraParser.UnaryexprContext uExpr = new LyraParser.UnaryexprContext(rewritten, -1);
        LyraParser.FactorContext factor = new LyraParser.FactorContext(uExpr, -1);
        LyraParser.MemberFactorContext memberFactor
                = new LyraParser.MemberFactorContext(factor);

        memberFactor.addChild(wrapExpressionIntoFactor(memberFactor, ctx.expr(0)));
        memberFactor.addChild(new CommonToken(LyraLexer.DOT, "."));
        memberFactor.addChild(new CommonToken(LyraLexer.IDENT, getBinaryOperatorMethod(ctx.binOp)));
        memberFactor.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));

        LyraParser.ArgsContext args = new LyraParser.ArgsContext(memberFactor, -1);
        args.addChild(wrapExpression(args, ctx.expr(1)));
        memberFactor.addChild(args);

        memberFactor.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));

        uExpr.addChild(memberFactor);
        rewritten.addChild(uExpr);

        replaceChild(ctx, parent, rewritten);
    }

    @Override
    public void exitUnaryexpr(LyraParser.UnaryexprContext ctx) {
        if (ctx.factor() != null) return;

        String method = null;
        if (ctx.prefixOp != null) {
            method = getPrefixOperatorMethod(ctx.prefixOp);
        } else if (ctx.postfixOp != null) {
            method = getPostfixOperatorMethod(ctx.postfixOp);
        }

        ParserRuleContext parent = ctx.getParent();
        LyraParser.UnaryexprContext rewritten = new LyraParser.UnaryexprContext(parent, -1);
        LyraParser.MemberFactorContext factor = new LyraParser.MemberFactorContext(
                new LyraParser.FactorContext(rewritten, -1));

        /* Due to the parse tree algorithm, our operand is already converted to the
         * "unaryexpr -> factor" form. */
        LyraParser.FactorContext operand = ctx.unaryexpr().factor();
        operand.parent = factor;
        factor.addChild(operand);

        factor.addChild(new CommonToken(LyraLexer.DOT, "."));
        factor.addChild(new CommonToken(LyraLexer.IDENT, method));
        factor.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));
        /* args rule is required (but it matches epsilon) */
        factor.addChild(new LyraParser.ArgsContext(factor, -1));
        factor.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));

        rewritten.addChild(factor);

        replaceChild(ctx, parent, rewritten);
    }
}
