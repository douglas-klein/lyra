package lyra;

import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class LocalVarUsageListenerTests {

    @Test
    public void testCompileFails() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        URI samplesURI = loader.getResource("illegal/local_var_usage/").toURI();
        File samplesDir = new File(samplesURI);
        assertNotNull(samplesDir);
        for (File file : samplesDir.listFiles()) {
            Compiler compiler = new Compiler();
            compiler.getErrorListener().setVerbosity(Verbosity.QUIET);
            compiler.init(new FileReader(file));
            assertFalse(file.getName() + " compiled!", compiler.compile());
        }
    }
}
