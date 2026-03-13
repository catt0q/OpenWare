package com.quantum.obfuscator;

import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.util.*;

/**
 * encrypts constant strings in bytecode to make static analysis harder
 * replaces string constants with encrypted byte arrays + runtime decryption
 */
public class StringEncryptor {

    private static final SecureRandom random = new SecureRandom();
    private final boolean verbose;
    private int encryptedCount = 0;

    public StringEncryptor(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: StringEncryptor <input-dir> [verbose]");
            System.exit(1);
        }

        String inputDir = args[0];
        boolean verbose = args.length > 1 ? Boolean.parseBoolean(args[1]) : false;

        StringEncryptor encryptor = new StringEncryptor(verbose);
        encryptor.processDirectory(new File(inputDir).toPath());
    }

    public void processDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class")) {
                    processClassFile(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("string encryption complete: " + encryptedCount + " strings encrypted");
    }

    private void processClassFile(Path classFile) throws IOException {
        byte[] classBytes = Files.readAllBytes(classFile);

        try {
            byte[] modified = encryptStrings(classBytes, classFile.toString());
            if (modified != null) {
                Files.write(classFile, modified);
                if (verbose) {
                    System.out.println("encrypted strings in: " + classFile.getFileName());
                }
            }
        } catch (Exception e) {
            System.err.println("failed to process " + classFile + ": " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
        }
    }

    private byte[] encryptStrings(byte[] classBytes, String className) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };

        final boolean[] needsDecryptMethod = { false };

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            private String internalClassName;
            private boolean isMixin;

            @Override
            public void visit(int version, int access, String name, String signature, String superName,
                    String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                this.internalClassName = name;

                // ONLY encrypt strings in YOUR mod code, never in:
                // - Minecraft classes (net/minecraft/)
                // - Java/system classes (java/, javax/, sun/)
                // - Fabric classes (net/fabricmc/)
                // - Libraries (org/, com/)
                // This prevents breaking critical early-startup code
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (descriptor.contains("Mixin")) {
                    isMixin = true;
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                    String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                // skip mixins, null visitors, and non-mod classes
                if (mv == null || isMixin || !shouldEncryptClass(internalClassName)) {
                    return mv;
                }

                return new StringEncryptingMethodVisitor(Opcodes.ASM9, mv, internalClassName, needsDecryptMethod);
            }

            private boolean shouldEncryptClass(String className) {
                if (className == null)
                    return false;

                // EXCLUDE critical classes that break if encrypted:
                // - base/client/CattoWareClient (main entrypoint)
                // - base/mixin/** (all mixins)
                // - base/CattoWare* (core classes)
                // - base/a (string decryptor!)
                // - base/util/StringDecryptor (string decryptor!)
                boolean shouldEncrypt = !className.equals("base/client/CattoWareClient") &&
                        !className.equals("base/a") &&
                        !className.equals("base/util/StringDecryptor") &&
                        !className.startsWith("base/mixin/") &&
                        !className.startsWith("base/CattoWare");

                // Debug: log what we're checking
                if (className.startsWith("base/") && shouldEncrypt) {
                    System.out.println("Encrypting strings in: " + className);
                }

                return shouldEncrypt;
            }

            @Override
            public void visitEnd() {
                // No longer inject decrypt methods - we use centralized base.a class
                super.visitEnd();
            }

            private void injectFakeDecryptMethod(String className, String methodName, int variant) {
                // create fake decrypt with obvious name
                MethodVisitor mv = cv.visitMethod(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, // NOT synthetic - looks real!
                        methodName,
                        "([BI)Ljava/lang/String;",
                        null,
                        null);

                mv.visitCode();

                // generate plausible but WRONG decryption that returns garbage
                // variant determines which fake algorithm to use

                mv.visitVarInsn(Opcodes.ALOAD, 0); // data
                mv.visitInsn(Opcodes.ARRAYLENGTH);
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
                mv.visitVarInsn(Opcodes.ASTORE, 2); // result

                Label loopStart = new Label();
                Label loopEnd = new Label();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitVarInsn(Opcodes.ISTORE, 3); // i = 0

                mv.visitLabel(loopStart);
                mv.visitVarInsn(Opcodes.ILOAD, 3);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitInsn(Opcodes.ARRAYLENGTH);
                mv.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);

                // FAKE DECRYPTION - different garbage per method
                mv.visitVarInsn(Opcodes.ALOAD, 2); // result
                mv.visitVarInsn(Opcodes.ILOAD, 3); // i
                mv.visitVarInsn(Opcodes.ALOAD, 0); // data
                mv.visitVarInsn(Opcodes.ILOAD, 3); // i
                mv.visitInsn(Opcodes.BALOAD); // data[i]

                switch (variant) {
                    case 0: // "decryptString" - XOR with key only (missing position dependency)
                        mv.visitVarInsn(Opcodes.ILOAD, 1); // key
                        mv.visitInsn(Opcodes.IXOR);
                        break;
                    case 1: // "decryptA" - XOR with key THEN position (reversed order)
                        mv.visitVarInsn(Opcodes.ILOAD, 1);
                        mv.visitInsn(Opcodes.IXOR);
                        mv.visitVarInsn(Opcodes.ILOAD, 3); // i
                        mv.visitInsn(Opcodes.IXOR);
                        break;
                    case 2: // "decrypt_v2" - adds instead of XOR (completely wrong)
                        mv.visitVarInsn(Opcodes.ILOAD, 1);
                        mv.visitInsn(Opcodes.IADD);
                        mv.visitVarInsn(Opcodes.ILOAD, 3);
                        mv.visitInsn(Opcodes.ISUB);
                        break;
                    case 3: // "xorDecrypt" - uses key twice (garbage)
                        mv.visitVarInsn(Opcodes.ILOAD, 1);
                        mv.visitInsn(Opcodes.IXOR);
                        mv.visitVarInsn(Opcodes.ILOAD, 1);
                        mv.visitInsn(Opcodes.IXOR);
                        break;
                }

                mv.visitInsn(Opcodes.I2B);
                mv.visitInsn(Opcodes.BASTORE);

                mv.visitIincInsn(3, 1);
                mv.visitJumpInsn(Opcodes.GOTO, loopStart);

                mv.visitLabel(loopEnd);

                // return garbage string
                mv.visitTypeInsn(Opcodes.NEW, "java/lang/String");
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
                mv.visitInsn(Opcodes.ARETURN);

                mv.visitMaxs(4, 4);
                mv.visitEnd();
            }

            private void injectDecryptMethod(String className) {
                // create: private static String a(byte[] data, int key)
                // Single-letter name 'a' - looks like ProGuard obfuscation!
                MethodVisitor mv = cv.visitMethod(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                        "a", // obfuscated-looking name
                        "([BI)Ljava/lang/String;",
                        null,
                        null);

                mv.visitCode();

                // byte[] result = new byte[data.length];
                mv.visitVarInsn(Opcodes.ALOAD, 0); // data
                mv.visitInsn(Opcodes.ARRAYLENGTH);
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
                mv.visitVarInsn(Opcodes.ASTORE, 2); // result

                // for (int i = 0; i < data.length; i++)
                Label loopStart = new Label();
                Label loopEnd = new Label();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitVarInsn(Opcodes.ISTORE, 3); // i = 0

                mv.visitLabel(loopStart);
                mv.visitVarInsn(Opcodes.ILOAD, 3); // i
                mv.visitVarInsn(Opcodes.ALOAD, 0); // data.length
                mv.visitInsn(Opcodes.ARRAYLENGTH);
                mv.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);

                // result[i] = (byte)(data[i] ^ i ^ key)
                mv.visitVarInsn(Opcodes.ALOAD, 2); // result
                mv.visitVarInsn(Opcodes.ILOAD, 3); // i
                mv.visitVarInsn(Opcodes.ALOAD, 0); // data
                mv.visitVarInsn(Opcodes.ILOAD, 3); // i
                mv.visitInsn(Opcodes.BALOAD); // data[i]
                mv.visitVarInsn(Opcodes.ILOAD, 3); // i
                mv.visitInsn(Opcodes.IXOR); // ^ i
                mv.visitVarInsn(Opcodes.ILOAD, 1); // key
                mv.visitInsn(Opcodes.IXOR); // ^ key
                mv.visitInsn(Opcodes.I2B); // (byte)
                mv.visitInsn(Opcodes.BASTORE); // result[i] =

                // i++
                mv.visitIincInsn(3, 1);
                mv.visitJumpInsn(Opcodes.GOTO, loopStart);

                mv.visitLabel(loopEnd);

                // return new String(result);
                mv.visitTypeInsn(Opcodes.NEW, "java/lang/String");
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, 2); // result
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
                mv.visitInsn(Opcodes.ARETURN);

                mv.visitMaxs(4, 4);
                mv.visitEnd();
            }
        };

        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private class StringEncryptingMethodVisitor extends MethodVisitor {
        private final String className;
        private final boolean[] needsDecryptMethod;

        public StringEncryptingMethodVisitor(int api, MethodVisitor mv, String className,
                boolean[] needsDecryptMethod) {
            super(api, mv);
            this.className = className;
            this.needsDecryptMethod = needsDecryptMethod;
        }

        @Override
        public void visitLdcInsn(Object value) {
            // only encrypt string constants
            if (value instanceof String) {
                String original = (String) value;

                // AGGRESSIVE: only skip empty strings and critical system strings
                if (original.isEmpty() || shouldSkip(original)) {
                    super.visitLdcInsn(value);
                    return;
                }

                // encrypt the string with XOR + position-dependent key
                byte[] originalBytes = original.getBytes();
                int key = random.nextInt(256);
                byte[] encrypted = new byte[originalBytes.length];

                for (int i = 0; i < originalBytes.length; i++) {
                    encrypted[i] = (byte) (originalBytes[i] ^ i ^ key);
                }

                // flag that we need decrypt method
                needsDecryptMethod[0] = true;

                // generate bytecode: decrypt(new byte[]{encrypted...}, key)

                // create byte array - use proper int push for any size
                pushInt(mv, encrypted.length);
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);

                // fill array with encrypted bytes
                for (int i = 0; i < encrypted.length; i++) {
                    mv.visitInsn(Opcodes.DUP);
                    pushInt(mv, i);
                    // For BASTORE, push as signed byte (it will be stored correctly)
                    mv.visitIntInsn(Opcodes.BIPUSH, encrypted[i]);
                    mv.visitInsn(Opcodes.BASTORE);
                }

                // push key (0-255, but BIPUSH handles 0-127 fine, use pushInt for safety)
                pushInt(mv, key);

                // call base.a.a(byte[], int) - centralized decryptor
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "base/a", "a", "([BI)Ljava/lang/String;", false);

                encryptedCount++;
                return;
            }

            super.visitLdcInsn(value);
        }

        // Helper to push int constants correctly for any value
        private void pushInt(MethodVisitor mv, int value) {
            if (value >= -1 && value <= 5) {
                mv.visitInsn(Opcodes.ICONST_0 + value);
            } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                mv.visitIntInsn(Opcodes.BIPUSH, value);
            } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                mv.visitIntInsn(Opcodes.SIPUSH, value);
            } else {
                mv.visitLdcInsn(value);
            }
        }

        private boolean shouldSkip(String str) {
            // ONLY skip critical system/resource strings that would break the game
            return str.startsWith("fabric.mod.") || // fabric metadata keys
                    str.startsWith("mixin.") || // mixin internals
                    str.equals("main") || // entry point
                    str.equals("init") || // lifecycle
                    (str.startsWith("assets/") && (str.endsWith(".png") || str.endsWith(".json"))) || // resource paths
                    str.length() > 200; // extremely long strings (likely data)
        }
    }
}
