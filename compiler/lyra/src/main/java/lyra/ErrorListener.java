package lyra;

import lyra.LyraLexer;
import lyra.LyraParser;
import org.antlr.v4.runtime.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Outputs syntactic error messages with added verbose information.
 *
 * For now, the extra is just the parser rule stack at the point of error.
 */
public class ErrorListener extends BaseErrorListener {
    private Verbosity verbosity = Verbosity.DEFAULT;

    private List<ParserRuleContext> errorContexts = new LinkedList<>();
    private long numberOfErrors;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        ++numberOfErrors;
        if (!isQuiet()) {
            String offendingSymbolString = getOffendingSymbolString(offendingSymbol, recognizer);
            System.err.println(String.format("Error: line %1$d:%2$d at %3$s: %4$s", line,
                    charPositionInLine, offendingSymbolString, msg));
        }

        captureErrorContext(recognizer);
    }

    private void captureErrorContext(Recognizer<?, ?> recognizer) {
        if (recognizer instanceof LyraParser) {
            LyraParser parser = (LyraParser)recognizer;
            errorContexts.add(parser.getContext());

            if (isVerbose()) {
                List<String> stack = parser.getRuleInvocationStack();
                Collections.reverse(stack);
                System.err.println("Parser rule stack: " + stack);
            }
        }
    }

    public void semanticError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                              String msg) {
        ++numberOfErrors;

        if (!isQuiet()) {
            String offendingSymbolString = getOffendingSymbolString(offendingSymbol, recognizer);
            if (offendingSymbolString.length() > 0)
                offendingSymbolString = offendingSymbolString + " ";
            String where = getWhereString(line, charPositionInLine);
            if (where.length() > 0)
                where = where + " ";

            System.err.println(String.format("Error: %1$s%2$s%3$s", where, offendingSymbolString, msg));
        }

        captureErrorContext(recognizer);
    }

    public void semanticError(Recognizer<?, ?> recognizer, SemanticErrorException exception) {
        semanticError(recognizer, exception.getOffendingSymbol(), exception.getLine(),
                exception.getColumn(), exception.getMessage());
    }

    private String getWhereString(int line, int charPositionInLine) {
        String where = "";
        if (line > 0)
            where = String.format("line %1$d", line);
        if (charPositionInLine > 0)
            where = String.format("%1$s:%2$d", where, charPositionInLine);
        return where;
    }

    public void semanticError(Recognizer<?, ?> recognizer, Object offendingSymbol, String msg) {
        semanticError(recognizer, offendingSymbol, -1, -1, msg);
    }

    private String getOffendingSymbolString(Object offendingSymbol, Recognizer<?, ?> recognizer) {
        String offendingSymbolString = offendingSymbol.toString();
        if (offendingSymbol instanceof Token) {
            Token token = ((Token) offendingSymbol);
            String tokenText = token.getText();
            String tokenTypePart = recognizer.getVocabulary().getDisplayName(token.getType());
            if (tokenTypePart.equals(String.format("'%1$s'", tokenText)))
                tokenTypePart = "";
            else
                tokenTypePart = " (" + tokenTypePart + ")";
            offendingSymbolString = String.format("\"%1$s\"%2$s", tokenText, tokenTypePart);

        }
        return offendingSymbolString;
    }

    public List<ParserRuleContext> getErrorContexts() {
        return errorContexts;
    }

    public Verbosity getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(Verbosity verbosity) {
        this.verbosity = verbosity;
    }

    private boolean isVerbose() {
        return getVerbosity() == Verbosity.VERBOSE;
    }
    private boolean isQuiet() {
        return getVerbosity() == Verbosity.QUIET;
    }

    public long getNumberOfErrors() {
        return numberOfErrors;
    }
}
