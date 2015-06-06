package lyra.listeners;

import jdk.nashorn.internal.ir.Terminal;
import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.LyraParserBaseListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ListIterator;

/**
 * Common code between all listeners that rewrite the parse tree before the semantic analysis.
 */
public class TreeRewriterBaseListener extends LyraParserBaseListener {
    protected static void replaceChild(ParseTree victim, ParserRuleContext parent,
                                       ParseTree replacement) {
        int line = getNodeLine(victim);
        if (line > 0)
            updateNodeTokensLine(replacement, line);

        final ListIterator<ParseTree> iterator = parent.children.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next() == victim)
                iterator.set(replacement);
        }
    }

    private static void updateNodeTokensLine(ParseTree node, int line) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (!(child instanceof TerminalNode)) {
                updateNodeTokensLine(child, line);
            } else {
                Token symbol = ((TerminalNode) child).getSymbol();
                if (!(symbol instanceof CommonToken)) continue;

                CommonToken token = (CommonToken) symbol;
                if (token.getLine() <= 0)
                    token.setLine(line);
            }
        }
    }

    private static int getNodeLine(ParseTree node) {
        int line = 0;

        for (int i = 0; line <= 0 && i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof TerminalNode) {
                line = ((TerminalNode) child).getSymbol().getLine();
            } else  {
                line = getNodeLine(child);
            }
        }
        return line;
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
        LyraParser.WrappedFactorContext factor = new LyraParser.WrappedFactorContext(
                new LyraParser.FactorContext(parent, -1));
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
