package sc.krisstal.format;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.jar.Manifest;

public record BlackLineConfig(byte[] salt, String payloadFolder, int guard) {
    private static final String ATTRIBUTE = "Pq7z";
    private static final int SALT_LENGTH = 8;

    public static BlackLineConfig read(Manifest manifest) throws IOException {
        if (manifest == null) {
            throw new IOException("Manifest is missing; this is not a standard BlackLine JAR");
        }
        String encoded = manifest.getMainAttributes().getValue(ATTRIBUTE);
        if (encoded == null) {
            throw new IOException("BlackLine config attribute " + ATTRIBUTE + " is missing");
        }
        byte[] raw = HexCodec.decode(encoded);
        if (raw.length < SALT_LENGTH + 3) {
            throw new IOException("BlackLine config is too short");
        }

        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(raw, 0, salt, 0, SALT_LENGTH);
        int folderLength = raw[SALT_LENGTH] & 0xff;
        int guardOffset = SALT_LENGTH + 1 + folderLength;
        if (guardOffset >= raw.length) {
            throw new IOException("Invalid BlackLine folder configuration");
        }
        String folder = new String(raw, SALT_LENGTH + 1, folderLength, StandardCharsets.UTF_8);
        return new BlackLineConfig(salt, folder, raw[guardOffset] & 0xff);
    }
}
