package lyra.listeners;

import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.LyraParserBaseListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ListIterator;

/**
 *
 */
public class SyntacticSugarListener extends LyraParserBaseListener {
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
    public void exitExpr(LyraParser.ExprContext ctx) {
        if (ctx.unaryexpr() != null)
            return; //handled at exitUnaryexpr
        if (ctx.IDENT() != null)
            return; //non-rewritable

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

    private String getPostfixOperatorMethod(Token token) {
        String method = null;
        switch (token.getType()) {
            case LyraLexer.INCREMENT:
                method = "__inc";
                break;
            case LyraLexer.DECREMENT:
                method = "__dec";
                break;
        }
        return method;
    }

    private String getPrefixOperatorMethod(Token token) {
        String method = null;
        switch (token.getType()) {
            case LyraLexer.NOT:
                method = "__not";
                break;
            case LyraLexer.PLUS:
                method = "__positive";
                break;
            case LyraLexer.MINUS:
                method = "__negative";
                break;
        }
        return method;
    }

    private String getBinaryOperatorMethod(Token token) {
        String methodName = null;
        switch (token.getType()) {
            case LyraLexer.MULTOP:
                methodName = "__multiplied";
                break;
            case LyraLexer.SLASH:
                methodName = "__divided";
                break;
            case LyraLexer.MODOP:
                methodName = "__remainder";
                break;
            case LyraLexer.PLUS:
                methodName = "__added";
                break;
            case LyraLexer.MINUS:
                methodName = "__subtracted";
                break;
            case LyraLexer.LESSTHAN:
                methodName = "__less";
                break;
            case LyraLexer.LESSTHANOREQUAL:
                methodName = "__lessorequal";
                break;
            case LyraLexer.MORETHANOREQUAL:
                methodName = "__greaterorequal";
                break;
            case LyraLexer.MORETHAN:
                methodName = "__greater";
                break;
            case LyraLexer.DOUBLEEQUALOP:
                methodName = "__equals";
                break;
            case LyraLexer.NOTEQUAL:
                methodName = "__notequals";
                break;
            case LyraLexer.IS:
                methodName = "__is";
                break;
            case LyraLexer.AND:
                methodName = "__and";
                break;
            case LyraLexer.OR:
                methodName = "__or";
                break;
            case LyraLexer.IDENT:
                methodName = token.getText();
                break;
        }
        return methodName;
    }

    private LyraParser.ExprContext wrapExpression(ParserRuleContext parent,
                                                  LyraParser.ExprContext expr) {
        LyraParser.ExprContext wrapped = new LyraParser.ExprContext(parent, -1);
        LyraParser.UnaryexprContext uExpr = new LyraParser.UnaryexprContext(wrapped, -1);

        uExpr.addChild(wrapExpressionIntoFactor(uExpr, expr));
        wrapped.addChild(uExpr);

        return wrapped;
    }

    private LyraParser.FactorContext wrapExpressionIntoFactor(ParserRuleContext parent,
                                                              LyraParser.ExprContext expr) {
        LyraParser.FactorContext factor = new LyraParser.FactorContext(parent, -1);
        factor.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));
        expr.parent = factor;
        factor.addChild(expr);
        factor.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));
        return factor;
    }

    static private void replaceChild(ParseTree child, ParserRuleContext parent, ParseTree replacement) {
        final ListIterator<ParseTree> iterator = parent.children.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next() == child)
                iterator.set(replacement);
        }
    }
}
