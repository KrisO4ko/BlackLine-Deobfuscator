package sc.krisstal;

import sc.krisstal.runtime.RuntimeWarmup;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        try (InputStream input = Launcher.class
                .getResourceAsStream("/blackline-entry.properties")) {
            if (input == null) {
                throw new IllegalStateException("blackline-entry.properties is missing");
            }
            config.load(input);
        }

        String className = required(config, "class");
        String methodName = required(config, "method");
        RuntimeWarmup.initialize(Launcher.class.getClassLoader(), Launcher.class);

        Class<?> entry = Class.forName(className, true, Launcher.class.getClassLoader());
        Method method = entry.getDeclaredMethod(methodName, String[].class);
        method.setAccessible(true);
        method.invoke(null, (Object) args);
    }

    private static String required(Properties config, String key) {
        String value = config.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing launcher property: " + key);
        }
        return value.trim();
    }
}
