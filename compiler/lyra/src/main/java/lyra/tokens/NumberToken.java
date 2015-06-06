package lyra.tokens;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;


/**
 * Special Token class that parses the string representation of a number (CommonToken.getText()) and makes the value
 * available.
 */
public class NumberToken extends CommonToken {

    private Number value;

    public NumberToken(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
        super(source, type, channel, start, stop);
        parseContent();
    }

    public NumberToken(int type, String text) {
        super(type, text);
        parseContent();
    }

    private void parseContent() {
        String text = getText();
        if (text.indexOf('.') == -1)
            value = Integer.parseInt(text);
        else
            value = Double.parseDouble(text);
    }

    public Number getValue() {
        return value;
    }

    public String getLyraTypeName() {
        return (value instanceof Integer) ? "Int" : "Number";
    }
}
