package sc.krisstal;

import sc.krisstal.cli.CommandLineApplication;

public final class BlackLineDeobfuscator {
    private BlackLineDeobfuscator() {
    }

    public static void main(String[] args) throws Exception {
        new CommandLineApplication().run(args);
    }
}
