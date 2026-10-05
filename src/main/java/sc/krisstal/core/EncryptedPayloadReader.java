package sc.krisstal.core;

import sc.krisstal.crypto.ArmorCodec;
import sc.krisstal.crypto.BlackLineCrypto;
import sc.krisstal.crypto.KeyDerivationVm;
import sc.krisstal.model.BlockResult;
import sc.krisstal.model.KeyInputs;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

public final class EncryptedPayloadReader {
    private static final int TAG_LENGTH = 16;

    private final List<byte[]> blocks;
    private final KeyInputs inputs;
    private final byte[] salt;
    private final int guard;
    private final ArmorCodec armorCodec;
    private int slot;
    private byte[] chain;

    public EncryptedPayloadReader(List<byte[]> blocks, KeyInputs inputs,
                                  byte[] salt, int guard, byte[] initialChain) {
        this.blocks = blocks;
        this.inputs = inputs;
        this.salt = salt;
        this.guard = guard;
        this.chain = initialChain;
        this.armorCodec = new ArmorCodec();
    }

    public byte[] next() throws Exception {
        BlockResult result = read(slot++);
        chain = result.nextChain();
        return result.body();
    }

    private BlockResult read(int index) throws Exception {
        if (index < 0 || index >= blocks.size()) {
            throw new IOException("Encrypted payload ended at slot " + index);
        }

        byte[] sealed = armorCodec.decode(blocks.get(index));
        byte[] key = KeyDerivationVm.derive(
                inputs.program(), inputs.registerMap(), inputs.seeds(), salt, guard, index, chain);
        if (sealed.length < TAG_LENGTH) {
            throw new IOException("Invalid encrypted block at slot " + index);
        }

        int cut = sealed.length - TAG_LENGTH;
        byte[] encrypted = java.util.Arrays.copyOf(sealed, cut);
        byte[] body = BlackLineCrypto.crypt(key, encrypted);
        byte[] expected = BlackLineCrypto.macOf(key, body, TAG_LENGTH);
        byte[] actual = java.util.Arrays.copyOfRange(sealed, cut, sealed.length);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new IOException("Integrity check failed at slot " + index);
        }
        return new BlockResult(body, BlackLineCrypto.chain(chain, body));
    }
}
