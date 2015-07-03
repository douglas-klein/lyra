package lyra.jasmin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alexis on 7/1/15.
 */
public class JasminLauncher {

    private Path outputDir = null;

    public boolean assemble(Path jasminFile) {
        String separator = System.getProperty("file.separator");
        String path = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        String jarPath = extractJar();
        if (jarPath == null)
            return false;

        ProcessBuilder processBuilder = new ProcessBuilder(path, "-jar", jarPath,
                jasminFile.toAbsolutePath().toString());
        if (getOutputDir() != null) {
            File file = getOrCreateOutputDir();
            if (file == null) return false;
            processBuilder.directory(file);
        }
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        Process process;
        try {
            process = processBuilder.start();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (jarPath != null) {
                try {
                    Files.delete(Paths.get(jarPath));
                } catch (IOException e) {
                }
            }
        }

        return process.exitValue() == 0;
    }

    private File getOrCreateOutputDir() {
        File file = getOutputDir().toFile();
        if (!file.exists()) {
            try {
                Files.createDirectories(getOutputDir());
                file = getOutputDir().toFile();
            } catch (IOException e) {
                System.err.println("Could not create intermediary files directory \""
                        + file + "\".");
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    /**
     * Extracts the jasmin.jar resource to the system temp directory so that it may be referenced
     * as a regular file.
     * @return absolute path to the extracted jar with native separators.
     */
    private String extractJar() {
        InputStream resource;
        ClassLoader classLoader = this.getClass().getClassLoader();
        resource = classLoader.getResourceAsStream("jasmin.jar");
        Path path;
        try {
            path = Files.createTempFile("jasmin", ".jar");
            Files.copy(resource, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return path.toAbsolutePath().toString();
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }
}
