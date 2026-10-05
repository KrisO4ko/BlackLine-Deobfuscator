package sc.krisstal.cli;

import java.nio.file.Path;

public record CliOptions(Path input, Path output) {
    public static CliOptions parse(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage: java -jar blackline-deobfuscator.jar <input.jar> [output.jar]");
            System.out.println("Default output: <input-name>-deobfuscated.jar");
            return null;
        }

        Path input = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = args.length == 2
                ? Path.of(args[1]).toAbsolutePath().normalize()
                : defaultOutput(input);
        return new CliOptions(input, output);
    }

    private static Path defaultOutput(Path input) {
        String name = input.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        return input.resolveSibling(base + "-deobfuscated.jar");
    }
}
