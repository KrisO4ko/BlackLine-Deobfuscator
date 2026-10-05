package sc.krisstal.io;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Manifest;

public final class JarSnapshot {
    private final Map<String, byte[]> entries;
    private final Manifest manifest;

    public JarSnapshot(Map<String, byte[]> entries, Manifest manifest) {
        this.entries = new LinkedHashMap<>(entries);
        this.manifest = manifest;
    }

    public Map<String, byte[]> entries() {
        return entries;
    }

    public Manifest manifest() {
        return manifest;
    }

    public Map<String, byte[]> classEntries() {
        Map<String, byte[]> classes = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (entry.getKey().endsWith(".class")) {
                classes.put(entry.getKey(), entry.getValue());
            }
        }
        return classes;
    }
}
