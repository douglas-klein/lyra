package lyra;

import lyra.listeners.*;
import lyra.scopes.Scope;
import lyra.symbols.SymbolTable;
import lyra.tokens.TokenFactory;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Compiler {

    private lyra.LyraParser parser;
    private ErrorListener errorListener = new ErrorListener();
    private boolean lemonadeRecovery;
    private lyra.LyraParser.ProgramContext parseTree;
    private lyra.LyraLexer lexer;
    private ParseTreeProperty<Scope> treeScopes;
    private SymbolTable symbolTable;
    private List<File> includeDirs = new ArrayList<>();

    public void init(File file) throws IOException {
        init(new FileReader(file), file);
    }

    public void init(Reader input) throws IOException {
        init(input, null);
    }

    public void init(Reader input, File fileOrFileDir) throws IOException {
        if (fileOrFileDir != null) {
            if (!fileOrFileDir.isDirectory())
                fileOrFileDir = fileOrFileDir.getParentFile();
            if (fileOrFileDir == null)
                fileOrFileDir = new File(".");
            includeDirs.add(0, fileOrFileDir);
        }

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

        } catch (SemanticErrorException e) {
            getErrorListener().semanticError(parser, e);
        }

        return getErrorListener().getNumberOfErrors() == 0;
    }

    public boolean analyse() {
        if (!parse()) return false;
        if (!processImports()) return false;
        rewriteSugar();

        if (!fillSymbolTable()) return false;

        try {
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new LocalVarUsageListener(this), parseTree);
            walker.walk(new TypeListener(this), parseTree);
            walker.walk(new AssertListener(this), parseTree);
        } catch (SemanticErrorException e) {
            getErrorListener().semanticError(parser, e);
        }

        return getErrorListener().getNumberOfErrors() == 0;
    }

    private boolean processImports() {
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ImportRewriterListener(this), parseTree);

        return getErrorListener().getNumberOfErrors() == 0;
    }

    public boolean compile() {
        if (!analyse()) return false;

        //Add code generation
        return true;
    }

    public void rewriteSugar() {
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

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public File resolveInclude(String fileName) {
        for (File dir : includeDirs) {
            if (dir == null) System.err.println("null dir");
            File[] files = dir.listFiles(f -> f.getName().equals(fileName));
            if (files.length > 0)
                return files[0];
        }
        return null;
    }
}
