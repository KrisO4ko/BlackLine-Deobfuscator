package sc.krisstal.crypto;

import sc.krisstal.model.KeyInputs;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public final class BlackLineCrypto {
    private BlackLineCrypto() {
    }

    public static byte[] selfHash(byte[] salt, int guard, KeyInputs inputs,
                                  Map<String, byte[]> classEntries) throws Exception {
        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        for (Map.Entry<String, byte[]> entry : new TreeMap<>(classEntries).entrySet()) {
            byte[] name = entry.getKey().getBytes(StandardCharsets.UTF_8);
            writeInt(serialized, name.length);
            serialized.write(name);
            writeInt(serialized, entry.getValue().length);
            serialized.write(entry.getValue());
        }
        return sha256(salt, intBytes(guard), intBytes(inputs.program().length),
                intBytes(inputs.program()), intBytes(inputs.registerMap()),
                intBytes(inputs.seeds()), serialized.toByteArray());
    }

    public static byte[] chain(byte[] previous, byte[] body) throws Exception {
        return sha256(previous, body);
    }

    public static byte[] sha256(byte[]... parts) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (byte[] part : parts) {
            if (part != null) {
                digest.update(part);
            }
        }
        return digest.digest();
    }

    public static byte[] crypt(byte[] key, byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] out = new byte[data.length];
        for (int block = 0, offset = 0; offset < data.length; block++, offset += 32) {
            digest.reset();
            digest.update(key);
            digest.update(intBytes(block));
            byte[] stream = digest.digest();
            for (int i = 0; i < 32 && offset + i < data.length; i++) {
                out[offset + i] = (byte) (data[offset + i] ^ stream[i]);
            }
        }
        return out;
    }

    public static byte[] macOf(byte[] key, byte[] body, int length) throws Exception {
        int blockSize = Math.max(64, key.length);
        byte[] paddedKey = new byte[blockSize];
        System.arraycopy(key, 0, paddedKey, 0, key.length);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] inner = new byte[blockSize + body.length];
        for (int i = 0; i < blockSize; i++) {
            inner[i] = (byte) (paddedKey[i] ^ 0x36);
        }
        System.arraycopy(body, 0, inner, blockSize, body.length);
        byte[] innerHash = digest.digest(inner);

        byte[] outer = new byte[blockSize + innerHash.length];
        for (int i = 0; i < blockSize; i++) {
            outer[i] = (byte) (paddedKey[i] ^ 0x5c);
        }
        System.arraycopy(innerHash, 0, outer, blockSize, innerHash.length);
        return Arrays.copyOf(MessageDigest.getInstance("SHA-256").digest(outer), length);
    }

    public static byte[] intBytes(int value) {
        return new byte[]{
                (byte) (value >>> 24), (byte) (value >>> 16),
                (byte) (value >>> 8), (byte) value
        };
    }

    public static byte[] intBytes(int[] values) {
        byte[] result = new byte[values.length * 4];
        for (int i = 0; i < values.length; i++) {
            byte[] value = intBytes(values[i]);
            System.arraycopy(value, 0, result, i * 4, 4);
        }
        return result;
    }

    private static void writeInt(ByteArrayOutputStream out, int value) throws Exception {
        out.write(intBytes(value));
    }
}
