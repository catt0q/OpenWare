package com.quantum.obfuscator;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Source-level string encryption - modifies Java source files before
 * compilation
 * Much more reliable than bytecode manipulation!
 */
public class SourceStringEncryptor {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: SourceStringEncryptor <srcDir> <outputDir> <verbose>");
            System.exit(1);
        }

        Path srcDir = Paths.get(args[0]);
        Path outputDir = Paths.get(args[1]);
        boolean verbose = Boolean.parseBoolean(args[2]);

        System.out.println("Source-level string encryption starting...");
        System.out.println("Input: " + srcDir);
        System.out.println("Output: " + outputDir);

        // Copy all files first
        copyDirectory(srcDir, outputDir);

        // Process Java files
        int filesProcessed = 0;
        int stringsEncrypted = 0;

        Files.walk(outputDir)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> shouldEncryptFile(p))
                .forEach(javaFile -> {
                    try {
                        int count = processJavaFile(javaFile, verbose);
                        if (count > 0 && verbose) {
                            System.out.println("Encrypted " + count + " strings in: " + javaFile.getFileName());
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to process: " + javaFile);
                        e.printStackTrace();
                    }
                });

        System.out.println("Source string encryption complete!");
    }

    private static boolean shouldEncryptFile(Path file) {
        String path = file.toString().replace('\\', '/');

        // WHITELIST ONLY: Only encrypt in these directories
        // These are feature implementations - safe to encrypt
        if (path.contains("/feature/impl/")) {
            System.out.println("✓ Encrypting: " + file.getFileName());
            return true;
        }

        if (path.contains("/cmd/impl/")) {
            System.out.println("✓ Encrypting: " + file.getFileName());
            return true;
        }

        // Everything else: DON'T encrypt (framework, core, UI, etc.)
        return false;
    }

    private static int processJavaFile(Path file, boolean verbose) throws IOException {
        String content = Files.readString(file);
        int count = 0;

        // Find all string literals
        Pattern stringPattern = Pattern.compile("\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"");
        Matcher matcher = stringPattern.matcher(content);

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            String literal = matcher.group(0);
            String value = literal.substring(1, literal.length() - 1); // remove quotes
            int matchStart = matcher.start();

            // Skip empty strings
            if (value.isEmpty()) {
                result.append(content, lastEnd, matcher.end());
                lastEnd = matcher.end();
                continue;
            }

            // Skip case labels - look for "case" keyword before the string
            // Pattern: case "string": or case "string" ->
            String before = content.substring(Math.max(0, matchStart - 50), matchStart).trim();
            if (before.endsWith("case")) {
                result.append(content, lastEnd, matcher.end());
                lastEnd = matcher.end();
                continue;
            }

            // Skip non-ASCII strings (they might be identifiers/labels in other languages)
            if (!value.matches("[\\x00-\\x7F]+")) {
                result.append(content, lastEnd, matcher.end());
                lastEnd = matcher.end();
                continue;
            }

            // Skip resource paths, translation keys, etc.
            if (value.contains("/") ||
                    value.startsWith("minecraft:") ||
                    value.startsWith("fabric:") ||
                    value.startsWith("key.") ||
                    value.startsWith("text.") ||
                    value.startsWith("item.") ||
                    value.startsWith("block.")) {
                result.append(content, lastEnd, matcher.end());
                lastEnd = matcher.end();
                continue;
            }

            // Encrypt the string
            byte[] encrypted = encrypt(value);
            int key = value.hashCode() & 0xFF;

            result.append(content, lastEnd, matcher.start());
            result.append("StringDecryptor.decrypt(new byte[]{");
            for (int i = 0; i < encrypted.length; i++) {
                if (i > 0)
                    result.append(",");
                result.append("(byte)").append(encrypted[i]);
            }
            result.append("}, ").append(key).append(")");

            lastEnd = matcher.end();
            count++;
        }

        result.append(content.substring(lastEnd));
        String modified = result.toString();

        // Add import if we encrypted anything
        if (count > 0) {
            modified = addImport(modified);
        }

        Files.writeString(file, modified);
        return count;
    }

    private static byte[] encrypt(String str) {
        byte[] data = str.getBytes();
        byte[] result = new byte[data.length];
        int key = str.hashCode() & 0xFF;

        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ i ^ key);
        }

        return result;
    }

    private static String addImport(String content) {
        // Add import after package declaration
        Pattern packagePattern = Pattern.compile("(package\\s+[^;]+;)");
        Matcher matcher = packagePattern.matcher(content);

        if (matcher.find()) {
            String packageDecl = matcher.group(1);
            return content.replace(packageDecl,
                    packageDecl + "\n\nimport base.util.StringDecryptor;");
        }

        // No package? Add at top
        return "import base.util.StringDecryptor;\n\n" + content;
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source)
                .forEach(src -> {
                    try {
                        Path dest = target.resolve(source.relativize(src));
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }
}
