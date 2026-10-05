package sc.krisstal.cli;

import sc.krisstal.core.BlackLineExtractor;
import sc.krisstal.model.ExtractionResult;
import sc.krisstal.output.RecoveredJarWriter;

import java.nio.file.Files;

public final class CommandLineApplication {
    private final BlackLineExtractor extractor;
    private final RecoveredJarWriter writer;

    public CommandLineApplication() {
        this.extractor = new BlackLineExtractor();
        this.writer = new RecoveredJarWriter();
    }

    public void run(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        if (options == null) {
            return;
        }
        validate(options);

        ExtractionResult result = extractor.extract(options.input());
        writer.write(options.output(), result);

        System.out.println("Done.");
        System.out.println("Input classes: " + result.inputClassCount());
        System.out.println("Recovered classes: " + result.recoveredClasses().size());
        System.out.println("Entry: " + result.entryClass() + "#" + result.entryMethod());
        System.out.println("Output: " + options.output());
    }

    private void validate(CliOptions options) throws Exception {
        if (!Files.isRegularFile(options.input())) {
            throw new IllegalArgumentException("Input JAR does not exist: " + options.input());
        }
        if (options.input().equals(options.output())) {
            throw new IllegalArgumentException("Output must be different from input");
        }
    }
}
