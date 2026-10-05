package sc.krisstal.runtime;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class RuntimeWarmup {
    private RuntimeWarmup() {
    }

    public static void initialize(ClassLoader loader, Class<?> anchor) {
        try {
            URI location = anchor.getProtectionDomain().getCodeSource().getLocation().toURI();
            File file = new File(location);
            if (!file.isFile()) {
                return;
            }
            try (JarFile jar = new JarFile(file)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }
                    warmClass(loader, name.substring(0, name.length() - 6).replace('/', '.'));
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void warmClass(ClassLoader loader, String className) {
        try {
            Class<?> type = Class.forName(className, false, loader);
            Field root = type.getDeclaredField("ROOT");
            if (root.getType() == byte[].class && Modifier.isStatic(root.getModifiers())) {
                root.setAccessible(true);
                root.set(null, new byte[32]);
            }
        } catch (Throwable ignored) {
        }
    }
}
