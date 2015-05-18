package lyra.listeners;

import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.LyraParserBaseListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
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

    static private void replaceChild(ParseTree child, ParserRuleContext parent, ParseTree replacement) {
        final ListIterator<ParseTree> iterator = parent.children.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next() == child)
                iterator.set(replacement);
        }
    }
}
