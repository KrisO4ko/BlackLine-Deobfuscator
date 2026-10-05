package sc.krisstal.core;

import sc.krisstal.crypto.BlackLineCrypto;
import sc.krisstal.format.BlackLineConfig;
import sc.krisstal.format.PayloadHeader;
import sc.krisstal.format.PayloadIndex;
import sc.krisstal.io.JarArchiveReader;
import sc.krisstal.io.JarSnapshot;
import sc.krisstal.model.ClassRecord;
import sc.krisstal.model.ExtractionResult;
import sc.krisstal.model.KeyInputs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BlackLineExtractor {
    private final JarArchiveReader archiveReader;
    private final KeyInputLoader keyInputLoader;

    public BlackLineExtractor() {
        this.archiveReader = new JarArchiveReader();
        this.keyInputLoader = new KeyInputLoader();
    }

    public ExtractionResult extract(Path input) throws Exception {
        JarSnapshot snapshot = archiveReader.read(input);
        BlackLineConfig config = BlackLineConfig.read(snapshot.manifest());
        Map<String, byte[]> classEntries = snapshot.classEntries();
        if (classEntries.isEmpty()) {
            throw new IOException("No class entries found");
        }

        String tableName = tableName(config.salt());
        if (!classEntries.containsKey(tableName + ".class")) {
            throw new IOException("Key table " + tableName + ".class is missing");
        }

        KeyInputs inputs = keyInputLoader.load(input, tableName);
        byte[] root = BlackLineCrypto.selfHash(config.salt(), config.guard(), inputs, classEntries);
        byte[] chain = BlackLineCrypto.sha256(new byte[]{0x63, 0x30}, root);
        List<byte[]> payloadBlocks = payloadBlocks(snapshot.entries(), config.payloadFolder());
        if (payloadBlocks.size() < 3) {
            throw new IOException("Encrypted BlackLine payload is missing or this is mod mode");
        }

        EncryptedPayloadReader reader = new EncryptedPayloadReader(
                payloadBlocks, inputs, config.salt(), config.guard(), chain);
        PayloadHeader header = PayloadHeader.read(reader.next());
        PayloadIndex index = PayloadIndex.read(reader.next());
        Map<String, byte[]> recovered = recoverClasses(reader, index.classes());

        return new ExtractionResult(snapshot.entries(), recovered, header.entryClass(),
                header.entryMethod(), config.payloadFolder(), classEntries.size());
    }

    private Map<String, byte[]> recoverClasses(EncryptedPayloadReader reader,
                                               List<ClassRecord> records) throws Exception {
        Map<String, byte[]> classes = new LinkedHashMap<>();
        for (ClassRecord record : records) {
            ByteArrayOutputStream body = new ByteArrayOutputStream(record.totalSize());
            for (int i = 0; i < record.fragmentCount(); i++) {
                body.write(reader.next());
            }
            byte[] classBytes = body.toByteArray();
            if (classBytes.length != record.totalSize()) {
                throw new IOException("Class size mismatch for " + record.internalName());
            }
            classes.put(record.internalName() + ".class", classBytes);
        }
        return classes;
    }

    private List<byte[]> payloadBlocks(Map<String, byte[]> entries, String folder) {
        String prefix = folder + "/";
        List<String> names = new ArrayList<>();
        for (String name : entries.keySet()) {
            if (name.startsWith(prefix) && name.length() > prefix.length()) {
                names.add(name);
            }
        }
        names.sort(String::compareTo);

        List<byte[]> blocks = new ArrayList<>(names.size());
        for (String name : names) {
            blocks.add(entries.get(name));
        }
        return blocks;
    }

    private String tableName(byte[] salt) {
        return "q" + Integer.toHexString(((salt[0] & 0xff) << 8) | (salt[1] & 0xff));
    }
}
