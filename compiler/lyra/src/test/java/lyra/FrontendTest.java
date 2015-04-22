package lyra;


import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

import static org.junit.Assert.*;

/**
 * Testes para lyra.Frontend
 */
@SuppressWarnings("ConstantConditions")
public class FrontendTest {

    @Test
    public void testCompileHelloWorld() throws Exception {
        Frontend frontend = new Frontend();

        ClassLoader loader = this.getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("samples/HelloWorld.ly");
        assertTrue(frontend.compile(new InputStreamReader(stream)));
    }

    @Test
    public void testCompileFile() throws Exception {
        Frontend frontend = new Frontend();

        ClassLoader loader = this.getClass().getClassLoader();
        URI samplesURI = loader.getResource("samples/").toURI();
        File samplesDir = new File(samplesURI);
        assertNotNull(samplesDir);
        for (File file : samplesDir.listFiles()) {
            System.out.println(file.getName());
            assertTrue(file.getName() + " didn't compile properly!", frontend.compile(new FileReader(file)));
        }
    }
}