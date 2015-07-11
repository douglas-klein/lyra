/**
 *
 */
package lyra;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import lyra.LyraParser;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class CommandlineUI {

    /**
     * true only when this instance was created from the static main, making it the application
     * entry point.
     *
     * When the CommandlineUI is the application entry point it may decide when to close the application
     * with a System.exit() call.
     */
    private boolean applicationEntryPoint = false;

    @Option(name = "--gui", aliases = {"-g"}, required = false, usage = "Displays ANTLR parse tree dialog.")
    private boolean showTreeDialog = false;

    @Option(name = "--lisp", aliases = {"-l"}, required = false, usage = "Show LISP-style parse tree.")
    private boolean showLispTree = false;

    @Option(name = "--verbose-errors", aliases = {"-E"}, required = false, usage = "Print more information with error messages")
    private boolean verboseErrors = false;

    @Option(name = "--quiet", aliases = {"-q"}, required = false, usage = "Do not output error information, just exit with non-zero status. This overrides --verbose-errors")
    private boolean quiet = false;

    @Option(name = "--gui-error-context", aliases = {"-G"}, required = false, usage = "Show a dialog with a tree representation of the parser context for each error.")
    private boolean guiErrorContext = false;

    @Option(name = "--lemonade-recovery", aliases = {"-L"}, required = false, usage = "Recover syntatic errors by ignoring the predicted symbol and resuming parsing from the offeding token")
    private  boolean lemonadeRecovery = false;

    @Option(name = "--int-dir", aliases = {"-i"}, required = false, usage = "Puts all intermediate files (.j and .class) into the given directory. The directory is created if needed.")
    private String intermediaryDirPath = null;

    @Option(name = "--out-dir", aliases = {"-o"}, required = false, usage = "Puts the generated jar file on the given directory. The directory is created if needed.")
    private String outputDirPath = null;

    @Option(name = "--out-filename", aliases = {"-O"}, required = false, usage = "Uses the given filename instead of <sourcename>.jar as the output file name. This affects only the file name, --out-dir is still considered.")
    private String outputFilename = null;

    @Option(name = "--only-check", aliases = {"-C"}, required = false, usage = "Do not generate code, only checks the semantic validity of the input")
    private boolean onlyCheck = false;

    @Argument
    private List<String> files = new ArrayList<>();

    public static void main(String[] args) {
        CommandlineUI frontend = new CommandlineUI();
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
            System.err.println("Usage: java CommandlineUI [options...] file");
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
        File file = new File(path);
        return compile(new BufferedReader(new FileReader(file)), file);
    }

    public boolean compile(Reader input, File fileOrParentDir) throws IOException {
        Compiler compiler = new Compiler();
        if (verboseErrors)
            compiler.getErrorListener().setVerbosity(Verbosity.VERBOSE);
        if (quiet)
            compiler.getErrorListener().setVerbosity(Verbosity.QUIET);
        if (onlyCheck) {
            compiler.setOnlyCheck(true);
        } else {
            if (intermediaryDirPath != null)
                compiler.setIntermediateDir(createDir(intermediaryDirPath));
            if (outputDirPath != null)
                compiler.setOutputDir(createDir(intermediaryDirPath));
            if (outputFilename != null) {
                compiler.setOutputFilename(outputFilename);
            } else {
                if (!fileOrParentDir.isDirectory())
                    compiler.setOutputFilename(fileOrParentDir.getName().split("\\.")[0] + ".jar");
            }
        }

        compiler.setLemonadeRecovery(lemonadeRecovery);
        compiler.init(input, fileOrParentDir);

        boolean ok = compiler.compile();
        notifyUserInterfaceOpen();

        if (guiErrorContext)
            showErrorInspections(compiler.getParser(), compiler.getErrorListener());

        if (showLispTree)
            System.out.println(compiler.getParseTree().toStringTree(compiler.getParser()));

        if (showTreeDialog)
            showTreeInspection(compiler.getParser(), compiler.getParseTree());

        if (compiler.getParser().getNumberOfSyntaxErrors() > 0)
            System.err.println("*** Errors ***");

        notifyUserInterfaceClosed();
        return ok;
    }

    private File createDir(String path) {
        Path pathObj = FileSystems.getDefault().getPath(path);
        try {
            pathObj = Files.createDirectories(pathObj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pathObj.toFile();
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
