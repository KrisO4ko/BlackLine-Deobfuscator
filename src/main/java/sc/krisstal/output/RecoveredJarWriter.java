package sc.krisstal.output;

import sc.krisstal.Launcher;
import sc.krisstal.model.ExtractionResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class RecoveredJarWriter {
    public void write(Path output, ExtractionResult result) throws Exception {
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Manifest manifest = createManifest(result);
        byte[] launcher = readLauncher();
        byte[] runtimeWarmup = readRuntimeWarmup();
        byte[] launcherConfig = createLauncherConfig(result);

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(output), manifest)) {
            writeResources(jar, result);
            writeRecoveredClasses(jar, result.recoveredClasses());
            put(jar, "sc/krisstal/Launcher.class", launcher);
            put(jar, "sc/krisstal/runtime/RuntimeWarmup.class", runtimeWarmup);
            put(jar, "blackline-entry.properties", launcherConfig);
        }
    }

    private Manifest createManifest(ExtractionResult result) {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.MAIN_CLASS, Launcher.class.getName());
        attributes.putValue("X-BlackLine-Recovered-Entry",
                result.entryClass() + "#" + result.entryMethod());
        return manifest;
    }

    private void writeResources(JarOutputStream jar, ExtractionResult result) throws IOException {
        List<String> resources = new ArrayList<>();
        for (String name : result.originalEntries().keySet()) {
            if (name.equalsIgnoreCase("META-INF/MANIFEST.MF")
                    || name.startsWith(result.payloadFolder() + "/")
                    || result.recoveredClasses().containsKey(name)
                    || name.equals("sc/krisstal/Launcher.class")
                    || isSignature(name)) {
                continue;
            }
            resources.add(name);
        }
        resources.sort(Comparator.naturalOrder());
        for (String name : resources) {
            put(jar, name, result.originalEntries().get(name));
        }
    }

    private void writeRecoveredClasses(JarOutputStream jar,
                                       Map<String, byte[]> recovered) throws IOException {
        for (Map.Entry<String, byte[]> entry : recovered.entrySet()) {
            put(jar, entry.getKey(), entry.getValue());
        }
    }

    private byte[] createLauncherConfig(ExtractionResult result) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("class", result.entryClass().replace('/', '.'));
        properties.setProperty("method", result.entryMethod());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        properties.store(output, "Recovered BlackLine entry");
        return output.toByteArray();
    }

    private byte[] readLauncher() throws IOException {
        return readResource("/sc/krisstal/Launcher.class");
    }

    private byte[] readRuntimeWarmup() throws IOException {
        return readResource("/sc/krisstal/runtime/RuntimeWarmup.class");
    }

    private byte[] readResource(String name) throws IOException {
        try (InputStream input = RecoveredJarWriter.class.getResourceAsStream(name)) {
            if (input == null) {
                throw new IOException("Missing resource: " + name);
            }
            return input.readAllBytes();
        }
    }

    private boolean isSignature(String name) {
        String upper = name.toUpperCase();
        return upper.startsWith("META-INF/")
                && (upper.endsWith(".SF") || upper.endsWith(".RSA") || upper.endsWith(".DSA"));
    }

    private void put(JarOutputStream jar, String name, byte[] data) throws IOException {
        JarEntry entry = new JarEntry(name);
        entry.setTime(0L);
        jar.putNextEntry(entry);
        jar.write(data);
        jar.closeEntry();
    }
}
