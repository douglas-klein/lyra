/**
 *
 */
package lyra;

import lyra.LyraLexer;
import lyra.LyraParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.kohsuke.args4j.*;

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

    @Argument
    private List<String> files = new ArrayList<String>();

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
        LyraParser.ProgramContext tree = parser.program();

        if (showLispTree) System.out.println(tree.toStringTree(parser));

        if (showTreeDialog) tree.inspect(parser);

        return true;
    }
}
