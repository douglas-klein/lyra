/**
 *
 */
package lyra;

import lyra.LyraLexer;
import lyra.LyraParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.kohsuke.args4j.*;

import javax.swing.text.html.parser.Parser;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Frontend {

    @Option(name = "--gui", aliases = {"-g"}, required = false,
            usage = "Displays ANTLR parse tree dialog.")
    private boolean showTreeDialog = false;

    @Option(name = "--lisp", aliases = {"-l"}, required = false,
            usage = "Show LISP-style parse tree.")
    private boolean showLispTree = false;

    @Option(name = "--verbose-errors", aliases = {"-E"}, required = false,
            usage = "Print more information with error messages")
    private boolean verboseErrors = false;

    @Option(name = "--gui-error-context", aliases = {"-G"}, required = false,
            usage = "Show a dialog with a tree representation of the parser context for each error.")
    private boolean guiErrorContext = false;

    @Argument
    private List<String> files = new ArrayList<>();

    public static void main(String[] args) {
        new Frontend().doMain(args);
    }

    private void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            if (files.size() > 1)
                throw new CmdLineException(parser, "Only a single file can be compiled per invocation", null);
            if (files.size() < 1)
                throw new CmdLineException(parser, "Missing input file", null);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage: java Frontend [options...] file");
            System.err.println("Options:");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

        try {
            compileFile(files.get(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean compileFile(String path) throws IOException {
        return compile(new BufferedReader(new FileReader(path)));
    }

    public boolean compile(Reader input) throws IOException {
        ANTLRInputStream antlrIn = new ANTLRInputStream(input);
        LyraLexer lexer = new LyraLexer(antlrIn);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        LyraParser parser = new LyraParser(tokens);
        parser.removeErrorListeners();

        ErrorListener errorListener = new ErrorListener();
        if (verboseErrors)
            errorListener.setVerbose(true);
        parser.addErrorListener(errorListener);

        //parse
        LyraParser.ProgramContext tree = parser.program();

        if (guiErrorContext) {
            for (ParserRuleContext ctxt : errorListener.getErrorContexts()) {
                ctxt.inspect(parser);
            }
        }

        if (showLispTree)
            System.out.println(tree.toStringTree(parser));

        if (showTreeDialog)
            tree.inspect(parser);

        return parser.getNumberOfSyntaxErrors() == 0;
    }
}
