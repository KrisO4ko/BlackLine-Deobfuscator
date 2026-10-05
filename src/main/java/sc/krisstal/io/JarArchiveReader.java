package sc.krisstal.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class JarArchiveReader {
    public JarSnapshot read(Path path) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        Manifest manifest;
        try (JarFile jar = new JarFile(path.toFile())) {
            manifest = jar.getManifest();
            jar.stream().filter(entry -> !entry.isDirectory()).forEach(entry -> {
                try (InputStream in = jar.getInputStream(entry)) {
                    entries.put(entry.getName(), in.readAllBytes());
                } catch (IOException error) {
                    throw new JarReadException(error);
                }
            });
        } catch (JarReadException error) {
            throw error.cause;
        }
        return new JarSnapshot(entries, manifest);
    }

    private static final class JarReadException extends RuntimeException {
        private final IOException cause;

        private JarReadException(IOException cause) {
            this.cause = cause;
        }
    }
}
