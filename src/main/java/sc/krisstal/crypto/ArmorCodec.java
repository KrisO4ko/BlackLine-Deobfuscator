package sc.krisstal.crypto;

import java.io.IOException;

public final class ArmorCodec {
    private static final int STRIDE = 64;
    private static final int FILL = 4;
    private static final byte[] PREFIX = bytes(
            0xff, 0xfe, 0x00, 0x00, 0x00, 0x00, 0xe2, 0x80,
            0xae, 0xed, 0xa0, 0x80, 0xc0, 0x80, 0x00, 0x00,
            0x1b, 0x5b, 0x30, 0x3b, 0x33, 0x31, 0x6d, 0x07,
            0x08, 0x08, 0x08, 0xfe, 0xff, 0x00, 0x1a);
    private static final byte[] SUFFIX = bytes(
            0x00, 0x00, 0x00, 0x00, 0xe2, 0x80, 0xab, 0xef,
            0xbf, 0xbe, 0xf8, 0x3f, 0x3f, 0x00, 0x0c, 0x00,
            0x0d, 0x00, 0x0a, 0x04, 0x04, 0x04, 0x1a);

    public byte[] decode(byte[] blob) throws IOException {
        int overhead = PREFIX.length + SUFFIX.length;
        if (blob.length <= overhead + FILL + 1) {
            throw new IOException("Invalid armored block");
        }
        checkBoundary(blob, PREFIX, 0, "prefix");
        checkBoundary(blob, SUFFIX, blob.length - SUFFIX.length, "suffix");

        int dataLength = blob.length - overhead;
        int fullBlocks = dataLength / (STRIDE + FILL);
        int rest = dataLength % (STRIDE + FILL);
        if (rest != 0 && (rest <= FILL || rest > STRIDE + FILL)) {
            throw new IOException("Invalid armor block layout");
        }

        int blocks = fullBlocks + (rest == 0 ? 0 : 1);
        int sealedLength = dataLength - blocks * FILL;
        byte[] out = new byte[sealedLength];
        for (int source = PREFIX.length, target = 0; target < sealedLength; ) {
            int length = Math.min(STRIDE, sealedLength - target);
            System.arraycopy(blob, source, out, target, length);
            source += length + FILL;
            target += length;
        }
        return out;
    }

    private static void checkBoundary(byte[] blob, byte[] expected, int offset,
                                      String name) throws IOException {
        for (int i = 0; i < expected.length; i++) {
            if (blob[offset + i] != expected[i]) {
                throw new IOException("Invalid armor " + name);
            }
        }
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }
}
