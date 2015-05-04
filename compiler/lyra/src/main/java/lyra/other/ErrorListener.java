package lyra.other;

import lyra.LyraParser;
import org.antlr.v4.runtime.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Outputs syntatic error messages with added verbose information.
 *
 * For now, the extra is just the parser rule stack at the point of error.
 */
public class ErrorListener extends BaseErrorListener {
    private boolean verbose;

    private List<ParserRuleContext> errorContexts = new LinkedList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        LyraParser parser = (LyraParser)recognizer;

        String offendingSymbolString = getOffendingSymbolString(offendingSymbol, parser);
        System.err.println(String.format("Error: line %1$d:%2$d at %3$s: %4$s", line, charPositionInLine,
                                         offendingSymbolString, msg));

        errorContexts.add(parser.getContext());

        if (isVerbose()) {
            List<String> stack = parser.getRuleInvocationStack();
            Collections.reverse(stack);
            System.err.println("Parser rule stack: " + stack);
        }
    }

    private String getOffendingSymbolString(Object offendingSymbol, LyraParser parser) {
        String offendingSymbolString = offendingSymbol.toString();
        if (offendingSymbol instanceof Token) {
            Token token = ((Token) offendingSymbol);
            String tokenText = token.getText();
            String tokenTypePart = parser.getVocabulary().getDisplayName(token.getType());
            if (tokenTypePart.equals(String.format("'%1$s'", tokenText)))
                tokenTypePart = "";
            else
                tokenTypePart = " (" + tokenTypePart + ")";
            offendingSymbolString = String.format("\"%1$s\"%2$s", tokenText, tokenTypePart);

        }
        return offendingSymbolString;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public List<ParserRuleContext> getErrorContexts() {
        return errorContexts;
    }
}
