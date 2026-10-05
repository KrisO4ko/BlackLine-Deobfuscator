package sc.krisstal.core;

import sc.krisstal.model.KeyInputs;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public final class KeyInputLoader {
    public KeyInputs load(Path input, String tableName) throws Exception {
        URL url = input.toUri().toURL();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{url}, null)) {
            Class<?> table = Class.forName(tableName, true, loader);
            String[] seedNames = (String[]) invoke(table, "a");
            int[] program = (int[]) invoke(table, "p");
            int[] registerMap = (int[]) invoke(table, "g");
            int[] seeds = new int[seedNames.length];
            for (int i = 0; i < seedNames.length; i++) {
                Class<?> seed = Class.forName(seedNames[i].replace('/', '.'), true, loader);
                seeds[i] = (Integer) invoke(seed, "a");
            }
            return new KeyInputs(program, registerMap, seeds);
        }
    }

    private Object invoke(Class<?> type, String name) throws Exception {
        Method method = type.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(null);
    }
}
