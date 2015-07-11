package lyra;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Assert that the compiler is working by compiliing sample programs, running
 * them and comparing their actual output with the one in a sidecar txt file
 */
public class JasminTests {

    @Test
    public void testGenerateCode() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        File samples = new File(loader.getResource("jasmin_tests").toURI());
        for (File file : FileUtils.listFiles(samples, new SuffixFileFilter("ly"), null)) {
            File dir = Files.createTempDirectory("lyraJasminTests").toFile();
            dir.deleteOnExit();
            Compiler compiler = new Compiler();
            compiler.setIntermediateDir(dir);
            compiler.setOutputDir(dir);
            compiler.init(file);
            assertTrue(compiler.compile());

            List<String> expected = getExpectedOutput(samples, file);
            File program = new File(compiler.getOutputDir(), "lyra-program.jar");
            assertTrue(program.exists());
            List<String> actual = runProgram(program, false);
            assertNotNull(actual);

            System.out.println("----");
            actual.forEach(l -> System.out.println(l));
            System.out.println("----");
            assertEquals(expected, actual);
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    public void testGenerateCodeWithoutOutput() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        File samples = new File(loader.getResource("samples").toURI());
        for (File file : FileUtils.listFiles(samples, new SuffixFileFilter("ly"), null)) {
            if (file.getName().startsWith("Imported")) continue;
            File dir = Files.createTempDirectory("lyraJasminTests").toFile();
            dir.deleteOnExit();
            Compiler compiler = new Compiler();
            compiler.setIntermediateDir(dir);
            compiler.setOutputDir(dir);
            compiler.init(file);
            assertTrue(compiler.compile());

            File program = new File(compiler.getOutputDir(), "lyra-program.jar");
            assertTrue(program.exists());
            List<String> actual = runProgram(program, true);
            assertNotNull(actual);
        }

    }

    private List<String> runProgram(File program, boolean requireZeroExit) throws IOException, InterruptedException {
        String separator = System.getProperty("file.separator");
        String java = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        ProcessBuilder builder = new ProcessBuilder(java, "-jar", program.getAbsolutePath());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        List<String> lines = IOUtils.readLines(process.getInputStream());
        process.waitFor();
        if (requireZeroExit && process.exitValue() != 0)
            return null;
        return lines;
    }

    private List<String> getExpectedOutput(File samples, File file) throws IOException {
        Pattern pattern = Pattern.compile("(.*)\\.[^.]*$");
        Matcher matcher = pattern.matcher(file.getName());
        assertTrue(matcher.matches());
        String baseName = matcher.group(1);

        File outputFile = new File(samples, baseName + ".txt");
        assertTrue(outputFile.exists());

        return Files.readAllLines(outputFile.toPath());
    }
}
