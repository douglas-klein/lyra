package lyra;

import lyra.listeners.*;
import lyra.scopes.Scope;
import lyra.symbols.SymbolTable;
import lyra.tokens.TokenFactory;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.Reader;

public class Compiler {

    private lyra.LyraParser parser;
    private ErrorListener errorListener = new ErrorListener();
    private boolean lemonadeRecovery;
    private lyra.LyraParser.ProgramContext parseTree;
    private lyra.LyraLexer lexer;
    private ParseTreeProperty<Scope> treeScopes;
    private SymbolTable symbolTable;

    public void init(Reader input) throws IOException {
        ANTLRInputStream antlrIn = new ANTLRInputStream(input);
        lexer = new lyra.LyraLexer(antlrIn);
        lexer.removeErrorListeners();
        lexer.addErrorListener(getErrorListener());
        lexer.setTokenFactory(new TokenFactory());
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser = new lyra.LyraParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(getErrorListener());

        if (useLemonadeRecovery())
            parser.setErrorHandler(new LemonadeErrorHandler());
    }

    public boolean parse() {
        parseTree = parser.program();
        return errorListener.getNumberOfErrors() == 0;
    }

    private boolean fillSymbolTable() {
        symbolTable = new SymbolTable();
        ParseTreeWalker walker = new ParseTreeWalker();

        try {
            DeclarationsListener declarationsListener = new DeclarationsListener(this);
            walker.walk(declarationsListener, parseTree);

            ReferencesListener referencesListener = new ReferencesListener(this);
            walker.walk(referencesListener, parseTree);

            TypeListener typeListener = new TypeListener(this);
            walker.walk(typeListener, parseTree);
        } catch (SemanticErrorException e) {
            getErrorListener().semanticError(parser, e);
        }

        return getErrorListener().getNumberOfErrors() == 0;
    }

    public boolean analyse() {
        if (!parse()) return false;
        rewriteSugar();

        if (!fillSymbolTable()) return false;
        // Add more steps

        return true;
    }

    public boolean compile() {
        if (!analyse()) return false;

        //Add code generation
        return true;
    }

    private void rewriteSugar() {
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), parseTree);
        walker.walk(new ArrayRewriterListener(), parseTree);
    }

    public void setLemonadeRecovery(boolean lemonadeRecovery) {
        this.lemonadeRecovery = lemonadeRecovery;
    }
    public boolean useLemonadeRecovery() {
        return lemonadeRecovery;
    }

    public lyra.LyraParser.ProgramContext getParseTree() {
        return parseTree;
    }
    public ErrorListener getErrorListener() {
        return errorListener;
    }
    public lyra.LyraParser getParser() {
        return parser;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
