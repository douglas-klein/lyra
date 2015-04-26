package lyra;

import org.antlr.v4.runtime.*;

/**
 * Error handler that recovers by forgetting the predicted token, and resuming the parse at the
 * offending token.
 *
 * Técnica 5 nos slides
 */
public class LemonadeErrorHandler extends DefaultErrorStrategy {

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        reportError(recognizer, e);
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        InputMismatchException exception = new InputMismatchException(recognizer);
        reportError(recognizer, exception);
        return exception.getOffendingToken();
    }

    @Override
    public void sync(Parser recognizer) throws RecognitionException { }
}
