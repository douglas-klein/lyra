package lyra;


import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * Testes para lyra.Compiler
 */
@SuppressWarnings("ConstantConditions")
public class CompilerTests {

    @Test
    public void testCompileHelloWorld() throws Exception {
        Compiler compiler = new Compiler();
        compiler.setOnlyCheck(true);
        compiler.useTempDir();

        ClassLoader loader = this.getClass().getClassLoader();
        compiler.init(new File(loader.getResource("samples/HelloWorld.ly").toURI()));
        assertTrue(compiler.compile());
    }

    @Test
    public void testCompileFile() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        URI samplesURI = loader.getResource("samples/").toURI();
        File samplesDir = new File(samplesURI);
        assertNotNull(samplesDir);
        for (File file : samplesDir.listFiles()) {
            Compiler compiler = new Compiler();
            compiler.setOnlyCheck(true);
            compiler.useTempDir();
            compiler.init(file);
            assertTrue(file.getName() + " didn't compile properly!", compiler.compile());
        }
    }

    @Test
    public void testDoNotCompileSyntacticErrors() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        URI samplesURI = loader.getResource("syntactic_errors").toURI();
        File samplesDir = new File(samplesURI);
        assertNotNull(samplesDir);
        for (File file : samplesDir.listFiles()) {
            Compiler compiler = new Compiler();
            compiler.setOnlyCheck(true);
            compiler.useTempDir();
            compiler.getErrorListener().setVerbosity(Verbosity.QUIET);
            compiler.init(file);
            assertFalse(file.getName() + " compiled without error!", compiler.compile());
        }
    }

    @Test
    public void testDoNotCompileSelfSemanticErrors() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        URI samplesURI = loader.getResource("semantic_errors").toURI();
        File samplesDir = new File(samplesURI);
        assertNotNull(samplesDir);
        for (File file : samplesDir.listFiles()) {
            System.err.println(file.getName());
            Compiler compiler = new Compiler();
            compiler.setOnlyCheck(true);
            compiler.useTempDir();
            compiler.getErrorListener().setVerbosity(Verbosity.QUIET);
            compiler.init(file);
            assertFalse(file.getName() + " compiled without error!", compiler.compile());
        }
    }
}