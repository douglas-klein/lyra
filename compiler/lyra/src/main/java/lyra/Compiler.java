package lyra;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import lyra.LyraLexer;
import lyra.LyraParser;
import java.io.IOException;
import java.io.Reader;

public class Compiler {

    private LyraParser parser;
    private ErrorListener errorListener = new ErrorListener();
    private boolean lemonadeRecovery;
    private LyraParser.ProgramContext parseTree;

    public void init(Reader input) throws IOException {
        ANTLRInputStream antlrIn = new ANTLRInputStream(input);
        LyraLexer lexer = new LyraLexer(antlrIn);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser = new LyraParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        if (useLemonadeRecovery())
            parser.setErrorHandler(new LemonadeErrorHandler());
    }

    public boolean parse() {
        parseTree = parser.program();
        return parser.getNumberOfSyntaxErrors() == 0;
    }

    public boolean compile() {
        if (!parse())
            return false;
        // Add more steps
        return true;
    }

    public void setLemonadeRecovery(boolean lemonadeRecovery) {
        this.lemonadeRecovery = lemonadeRecovery;
    }
    public boolean useLemonadeRecovery() {
        return lemonadeRecovery;
    }

    public LyraParser.ProgramContext getParseTree() {
        return parseTree;
    }
    public ErrorListener getErrorListener() {
        return errorListener;
    }
    public LyraParser getParser() {
        return parser;
    }
}
