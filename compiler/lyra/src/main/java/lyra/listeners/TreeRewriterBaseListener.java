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
 * Created by alexis on 6/4/15.
 */
public class TreeRewriterBaseListener extends LyraParserBaseListener {
    protected static void replaceChild(ParseTree child, ParserRuleContext parent, ParseTree replacement) {
        final ListIterator<ParseTree> iterator = parent.children.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next() == child)
                iterator.set(replacement);
        }
    }

    protected LyraParser.ExprContext wrapExpression(ParserRuleContext parent,
                                                    LyraParser.ExprContext expr) {
        LyraParser.ExprContext wrapped = new LyraParser.ExprContext(parent, -1);
        LyraParser.UnaryexprContext uExpr = new LyraParser.UnaryexprContext(wrapped, -1);

        uExpr.addChild(wrapExpressionIntoFactor(uExpr, expr));
        wrapped.addChild(uExpr);

        return wrapped;
    }

    protected LyraParser.FactorContext wrapExpressionIntoFactor(ParserRuleContext parent,
                                                                LyraParser.ExprContext expr) {
        LyraParser.FactorContext factor = new LyraParser.FactorContext(parent, -1);
        factor.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));
        expr.parent = factor;
        factor.addChild(expr);
        factor.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));
        return factor;
    }

    protected String getPostfixOperatorMethod(Token token) {
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

    protected String getPrefixOperatorMethod(Token token) {
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

    protected String getBinaryOperatorMethod(Token token) {
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
            case LyraLexer.IS:
                methodName = "__is";
                break;
            case LyraLexer.NOTEQUAL:
                methodName = "__notequals";
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
}
