package base.util;

/**
 * String decryption utility - used by obfuscated source code
 */
public class StringDecryptor {

    // Real decrypt method
    public static String decrypt(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ i ^ key);
        }
        return new String(result);
    }

    // Honeypot decryptors - wrong algorithms
    public static String decryptA(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key); // Missing position XOR
        }
        return new String(result);
    }

    public static String decryptB(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key ^ i); // Reversed order
        }
        return new String(result);
    }

    public static String decryptC(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] + key - i); // ADD instead of XOR
        }
        return new String(result);
    }

    public static String decryptD(byte[] data, int key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key ^ key); // Double XOR = no-op
        }
        return new String(result);
    }
}
