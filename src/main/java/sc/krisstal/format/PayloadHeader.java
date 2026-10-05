package sc.krisstal.format;

import java.io.IOException;

public record PayloadHeader(String entryClass, String entryMethod) {
    public static PayloadHeader read(byte[] data) throws IOException {
        MetadataCursor cursor = new MetadataCursor(data);
        String entryClass = cursor.readUtf8(cursor.readInt());
        String entryMethod = cursor.readUtf8(cursor.readInt());
        return new PayloadHeader(entryClass, entryMethod);
    }
}
