package lyra.tokens;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

/**
 * Specialization of CommonToken allowing access to a lyra string literal contents
 */
public class StringToken extends CommonToken {
    public StringToken(int type, String text) {
        super(type, text);
    }

    public StringToken(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
        super(source, type, channel, start, stop);
    }

    public String getContent() {
        String text = getText();
        return text.length() <= 2 ? "" : text.substring(1, text.length()-1);
    }
}
