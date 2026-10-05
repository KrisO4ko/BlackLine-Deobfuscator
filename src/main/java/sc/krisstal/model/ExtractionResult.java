package sc.krisstal.model;

import java.util.Map;

public record ExtractionResult(
        Map<String, byte[]> originalEntries,
        Map<String, byte[]> recoveredClasses,
        String entryClass,
        String entryMethod,
        String payloadFolder,
        int inputClassCount
) {
}
