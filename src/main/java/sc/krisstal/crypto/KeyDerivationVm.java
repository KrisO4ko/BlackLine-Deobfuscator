package sc.krisstal.crypto;

public final class KeyDerivationVm {
    private KeyDerivationVm() {
    }

    public static byte[] derive(int[] program, int[] registerMap, int[] seeds,
                                byte[] salt, int guard, int slot, byte[] chain) throws Exception {
        int mask = seeds[0] ^ seeds[7] ^ seeds[13] ^ seeds[31] ^ 0x5bf03635;
        int[] registers = new int[16];
        int[] memory = new int[32];
        int pc = 0;

        while (pc >= 0 && pc + 2 < program.length) {
            int op = program[pc] ^ mask;
            int a = program[pc + 1] ^ mask;
            int b = program[pc + 2] ^ mask;
            pc += 3;
            int ra = registerMap[a & 15];
            int rb = registerMap[b & 15];
            switch (op & 31) {
                case 0 -> pc = -1;
                case 1 -> { }
                case 2 -> registers[ra] = b;
                case 3 -> registers[ra] = registers[rb];
                case 4 -> registers[ra] ^= registers[rb];
                case 5 -> registers[ra] ^= b;
                case 6 -> registers[ra] += registers[rb];
                case 7 -> registers[ra] += b;
                case 8 -> registers[ra] -= registers[rb];
                case 9 -> registers[ra] *= registers[rb];
                case 10 -> registers[ra] *= b;
                case 11 -> registers[ra] &= registers[rb];
                case 12 -> registers[ra] &= b;
                case 13 -> registers[ra] |= registers[rb];
                case 14 -> registers[ra] = ~registers[ra];
                case 15 -> registers[ra] <<= registers[rb] & 31;
                case 16 -> registers[ra] <<= b & 31;
                case 17 -> registers[ra] >>>= registers[rb] & 31;
                case 18 -> registers[ra] >>>= b & 31;
                case 19 -> registers[ra] = registers[ra] * 1664525 + 1013904223;
                case 20 -> registers[ra] >>>= 9;
                case 21 -> registers[ra] = (byte) registers[ra];
                case 22 -> registers[ra] = seeds[registers[rb] & 255];
                case 23 -> registers[ra] = salt[registers[rb] & 7];
                case 24 -> registers[ra] = guard;
                case 25 -> registers[ra] = memory[registers[rb] & 31];
                case 26 -> memory[registers[ra] & 31] = (byte) registers[rb];
                case 27 -> memory[registers[ra] & 31] =
                        (byte) (memory[registers[ra] & 31] ^ registers[rb]);
                case 28 -> pc = b;
                case 29 -> { if (registers[ra] == 0) pc = b; }
                case 30 -> { if (registers[ra] != 0) pc = b; }
                default -> pc = -1;
            }
        }

        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        for (int value : memory) {
            digest.update((byte) value);
        }
        digest.update(salt);
        digest.update((byte) 0x9e);
        digest.update(chain);
        digest.update(BlackLineCrypto.intBytes(slot));
        return digest.digest();
    }
}
