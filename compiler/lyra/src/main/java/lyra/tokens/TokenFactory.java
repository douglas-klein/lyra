package lyra.tokens;

import lyra.LyraLexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;

/**
 * Factory that produces appropriate subclasses of CommonToken for some token types of the lyra language, and
 * delegates the task for CommonTokenFactory when there is no special subclass.
 */
public class TokenFactory extends CommonTokenFactory {

    public TokenFactory() { super(); }
    public TokenFactory(boolean copyText) { super(copyText); }

    @Override
    public CommonToken create(Pair<TokenSource, CharStream> source, int type, String text, int channel, int start, int stop, int line, int charPositionInLine) {
        CommonToken token;
        switch (type) {
            case LyraLexer.NUMBER:
                token = new NumberToken(source, type, channel, start, stop);
                break;
            default:
                return super.create(source, type, text, channel, start, stop, line, charPositionInLine);
        }
        // we created the token, fill the extras
        token.setLine(line);
        token.setCharPositionInLine(charPositionInLine);
        if (text != null) {
            token.setText(text);
        } else if (copyText && source.b != null) {
            token.setText(source.b.getText(Interval.of(start, stop)));
        }

        return token;
    }

    @Override
    public CommonToken create(int type, String text) {
        switch (type) {
            case LyraLexer.NUMBER:
                return new NumberToken(type, text);
            default:
                break;
        }
        return super.create(type, text);
    }
}
