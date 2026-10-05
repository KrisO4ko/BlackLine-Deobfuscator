package sc.krisstal.format;

import java.io.IOException;

public final class HexCodec {
    private HexCodec() {
    }

    public static byte[] decode(String value) throws IOException {
        String text = value.trim();
        if ((text.length() & 1) != 0) {
            throw new IOException("Odd-length BlackLine config");
        }
        byte[] result = new byte[text.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int high = Character.digit(text.charAt(i * 2), 16);
            int low = Character.digit(text.charAt(i * 2 + 1), 16);
            if (high < 0 || low < 0) {
                throw new IOException("Invalid BlackLine config hex");
            }
            result[i] = (byte) ((high << 4) | low);
        }
        return result;
    }
}
