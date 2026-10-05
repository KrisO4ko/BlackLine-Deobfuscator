package sc.krisstal.format;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class MetadataCursor {
    private final byte[] data;
    private int offset;

    public MetadataCursor(byte[] data) {
        this.data = data;
    }

    public int readInt() throws IOException {
        if (offset + 4 > data.length) {
            throw new IOException("Unexpected end of decrypted metadata");
        }
        int value = ((data[offset] & 0xff) << 24)
                | ((data[offset + 1] & 0xff) << 16)
                | ((data[offset + 2] & 0xff) << 8)
                | (data[offset + 3] & 0xff);
        offset += 4;
        return value;
    }

    public String readUtf8(int length) throws IOException {
        if (length <= 0 || offset + length > data.length) {
            throw new IOException("Invalid metadata string length: " + length);
        }
        String value = new String(data, offset, length, StandardCharsets.UTF_8);
        offset += length;
        return value;
    }
}
