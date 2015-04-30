/**
 *
 */
package lyra;

import lyra.LyraLexer;
import lyra.LyraParser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class Frontend {

    /**
     * true only when this instance was created from the static main, making it the application
     * entry point.
     *
     * When the Frontend is the application entry point it may decide when to close the application
     * with a System.exit() call.
     */
    private boolean applicationEntryPoint = false;

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

    @Option(name = "--lemonade-recovery", aliases = {"-L"}, required = false,
            usage = "Recover syntatic errors by ignoring the predicted symbol and resuming " +
                    "parsing from the offeding token")
    private  boolean lemonadeRecovery = false;

    @Argument
    private List<String> files = new ArrayList<>();

    public static void main(String[] args) {
        Frontend frontend = new Frontend();
        frontend.applicationEntryPoint = true;
        frontend.doMain(args);
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


    private int openUserInterfaces = 0;
    private void notifyUserInterfaceOpen() {
        ++openUserInterfaces;
    }
    private void notifyUserInterfaceClosed() {
        if (openUserInterfaces > 0)
            --openUserInterfaces;
        if (openUserInterfaces == 0 && isApplicationEntryPoint())
            System.exit(0);
    }

    public boolean compileFile(String path) throws IOException {
        return compile(new BufferedReader(new FileReader(path)));
    }

    public boolean compile(Reader input) throws IOException {
        notifyUserInterfaceOpen();
        ANTLRInputStream antlrIn = new ANTLRInputStream(input);
        LyraLexer lexer = new LyraLexer(antlrIn);
        lexer.setTokenFactory(new TokenFactory());
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        LyraParser parser = new LyraParser(tokens);
        parser.removeErrorListeners();

        ErrorListener errorListener = new ErrorListener();
        if (verboseErrors) errorListener.setVerbose(true);
        parser.addErrorListener(errorListener);

        if (lemonadeRecovery) parser.setErrorHandler(new LemonadeErrorHandler());

        //parse
        LyraParser.ProgramContext tree = parser.program();

        if (guiErrorContext) showErrorInspections(parser, errorListener);

        if (showLispTree) System.out.println(tree.toStringTree(parser));

        if (showTreeDialog) showTreeInspection(parser, tree);

        notifyUserInterfaceClosed();

        if (parser.getNumberOfSyntaxErrors() > 0)
            System.err.println("*** Errors ***");

        return parser.getNumberOfSyntaxErrors() == 0;
    }

    private void showTreeInspection(LyraParser parser, LyraParser.ProgramContext tree) {
        final JDialog dialog = getInspectDialog(parser, tree);
        if (dialog != null) {
            notifyUserInterfaceOpen();
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent windowEvent) {
                    super.windowClosed(windowEvent);
                    dialog.dispose();
                    notifyUserInterfaceClosed();
                }
            });
        }
    }

    private static JDialog getInspectDialog(LyraParser parser, RuleContext tree) {
        JDialog dialog = null;
        try {
            dialog = tree.inspect(parser).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return dialog;
    }

    private void showErrorInspections(LyraParser parser, ErrorListener errorListener) {
        notifyUserInterfaceOpen();
        Iterator<ParserRuleContext> iterator = errorListener.getErrorContexts().iterator();
        showNextErrorInspection(parser, iterator);
    }

    private void showNextErrorInspection(LyraParser parser, Iterator<ParserRuleContext> iterator) {
        if (!iterator.hasNext()) {
            notifyUserInterfaceClosed();
            return;
        }
        final JDialog dialog = getInspectDialog(parser, iterator.next());
        if (dialog == null) {
            notifyUserInterfaceClosed();
            return;
        }
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                super.windowClosed(windowEvent);
                new java.util.Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        showNextErrorInspection(parser, iterator);
                    }
                }, 100);
                dialog.dispose();
            }
        });
    }

    public boolean isApplicationEntryPoint() {
        return applicationEntryPoint;
    }
}
