package base;

/**
 * Utility class for decrypting obfuscated strings
 * Loaded first to ensure availability during class initialization
 */
public class a {
    // Real decrypt method
    public static String a(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ i ^ key);
        }
        return new String(result);
    }

    // Honeypot decryptors - wrong algorithms
    private static String b(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key); // Missing position XOR
        }
        return new String(result);
    }

    private static String c(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key ^ i); // Reversed order
        }
        return new String(result);
    }

    private static String d(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] + key - i); // ADD instead of XOR
        }
        return new String(result);
    }

    private static String e(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key ^ key); // Double XOR = no-op
        }
        return new String(result);
    }
}
