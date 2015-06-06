package lyra;


import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Represents a semantic error of the program being processed.
 *
 * This exception is used when writing yet another a listener just for some rules is
 * not pretty or not a complete solution.
 */
public class SemanticErrorException extends RuntimeException {
    private Object offendingSymbol;
    private int line;
    private int column;

    public SemanticErrorException(String message) {
        this(message, null, -1, -1);
    }
    public SemanticErrorException(String message, Object offendingSymbol) {
        this(message, offendingSymbol, -1, -1);
    }

    public SemanticErrorException(String message, Object offendingSymbol, int line, int column) {
        super(message);
        setOffendingSymbol(offendingSymbol);
        this.line = line;
        this.column = column;
    }

    public Object getOffendingSymbol() {
        return offendingSymbol;
    }
    public void setOffendingSymbol(Object offendingSymbol) {
        this.offendingSymbol = offendingSymbol;
        if (offendingSymbol instanceof TerminalNode) {
            TerminalNode node = (TerminalNode) offendingSymbol;
            setLine(node.getSymbol().getLine());
            setColumn(node.getSymbol().getCharPositionInLine());
        } else if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            setLine(token.getLine());
            setColumn(token.getCharPositionInLine());
        }
    }

    public int getLine() {
        return line;
    }
    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }
    public void setColumn(int column) {
        this.column = column;
    }
}
