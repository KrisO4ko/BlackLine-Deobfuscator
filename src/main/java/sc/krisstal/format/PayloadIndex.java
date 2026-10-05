package sc.krisstal.format;

import sc.krisstal.model.ClassRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record PayloadIndex(List<ClassRecord> classes) {
    public static PayloadIndex read(byte[] data) throws IOException {
        MetadataCursor cursor = new MetadataCursor(data);
        int count = cursor.readInt();
        if (count <= 0 || count > 4096) {
            throw new IOException("Invalid class index count: " + count);
        }

        List<ClassRecord> records = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String name = cursor.readUtf8(cursor.readInt());
            int fragments = cursor.readInt();
            int total = cursor.readInt();
            if (name.isBlank() || name.endsWith(".class") || fragments <= 0 || total <= 0) {
                throw new IOException("Invalid class record: " + name);
            }
            records.add(new ClassRecord(name, fragments, total));
        }
        return new PayloadIndex(records);
    }
}
