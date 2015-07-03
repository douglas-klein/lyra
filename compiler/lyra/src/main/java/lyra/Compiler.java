package lyra;

import lyra.jasmin.JasminListener;
import lyra.listeners.*;
import lyra.scopes.Scope;
import lyra.symbols.SymbolTable;
import lyra.tokens.TokenFactory;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

public class Compiler {

    private lyra.LyraParser parser;
    private ErrorListener errorListener = new ErrorListener();
    private boolean lemonadeRecovery;
    private lyra.LyraParser.ProgramContext parseTree;
    private lyra.LyraLexer lexer;
    private ParseTreeProperty<Scope> treeScopes;
    private SymbolTable symbolTable;
    private List<File> includeDirs = new ArrayList<>();
    private boolean onlyCheck;
    private File intermediateDir = new File(".");
    private File outputDir = new File(".");

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

    private boolean generateCode() {
        /* clean old files */
        File lyraDir = new File(intermediateDir, "lyra");
        try {
            if (lyraDir.exists()) {
                if (lyraDir.isDirectory())
                    FileUtils.deleteDirectory(lyraDir);
                else
                    lyraDir.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not remove old intermediary files.");
            return false;
        }


        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new JasminListener(this, intermediateDir), parseTree);
        if (getErrorListener().getNumberOfErrors() != 0)
            return false;

        try {
            System.out.println("Output written to " + createJar() + ".");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error creating runnable jar.");
            return false;
        }

        return true;
    }

    private Path createJar() throws URISyntaxException, IOException {
        ClassLoader cl = getClass().getClassLoader();
        InputStream resourceStream = cl.getResourceAsStream("lyra-runtime-1.0.jar");
        ZipInputStream runtime = new ZipInputStream(resourceStream);
        File outputFile = new File(outputDir, "lyra-program.jar");
        ZipOutputStream output = new ZipOutputStream(new FileOutputStream(outputFile));

        /* copy everything on the lyra-runtime jar. Special case for the lyra directory */
        boolean gotLyra = false;
        for (ZipEntry entry; (entry = runtime.getNextEntry()) != null; ) {
            output.putNextEntry(entry);
            if (!entry.isDirectory()) IOUtils.copy(runtime, output);
            output.closeEntry();

            if (entry.getName().equals("lyra/") && !gotLyra) {
                gotLyra = true;
                File lyraDir = new File(intermediateDir, "lyra");
                IOFileFilter filter = TrueFileFilter.INSTANCE;
                for (File file : FileUtils.listFilesAndDirs(lyraDir, filter, filter)) {
                    String entryPath = zipEntryPath(lyraDir.toPath(), file);
                    if (entryPath == null) continue;
                    entry = new ZipEntry(entryPath);
                    output.putNextEntry(entry);
                    if (!entry.isDirectory()) {
                        FileInputStream inputStream = new FileInputStream(file);
                        IOUtils.copy(inputStream, output);
                        inputStream.close();
                    }
                }
            }
        }
        output.close();
        runtime.close();

        return outputFile.toPath();
    }

    private String zipEntryPath(Path parent, File file) {
        if (file.toPath().equals(parent)) return null;

        String str = file.getName() + (file.isDirectory() ? "/" : "");
        Path path = file.toPath().getParent();
        while (!path.equals(parent)) {
            str = path.getName(path.getNameCount()-1) + "/" + str;
            path = path.getParent();
        }
        return parent.getName(parent.getNameCount()-1) + "/" + str;
    }


    public boolean compile() {
        if (!analyse()) return false;

        if (!isOnlyCheck() && !generateCode()) return false;

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

    private File createTempDir() {
        File dir;
        try {
            Path path = Files.createTempDirectory("lyra");
            dir = path.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dir.deleteOnExit();
        return dir;
    }

    public void useTempDirs() {
        File dir = createTempDir();
        setIntermediateDir(dir);
        setOutputDir(dir);
    }

    public File getIntermediateDir() {
        return intermediateDir;
    }

    public void setIntermediateDir(File intermediateDir) {
        this.intermediateDir = intermediateDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public void setOnlyCheck(boolean onlyCheck) {
        this.onlyCheck = onlyCheck;
    }

    public boolean isOnlyCheck() {
        return onlyCheck;
    }
}
