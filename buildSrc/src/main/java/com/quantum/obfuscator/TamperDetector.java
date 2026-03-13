package com.quantum.obfuscator;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;

/**
 * tamper detection - embeds SHA-256 hashes of critical classes into the JAR
 * and injects runtime verification that checks bytecode integrity
 * if bytecode is modified (patched), triggers crash
 */
public class TamperDetector {

    private final boolean verbose;
    private final Map<String, String> classHashes = new HashMap<>();
    private int classesProtected = 0;

    // classes to protect with hash verification
    private static final String[] CRITICAL_PATTERNS = {
            "base/client/auth/",
            "base/client/crypto/",
            "base/client/managers/AuthManager",
            "base/client/Client",
            "com/quantum/obfuscator/AntiDebug"
    };

    public TamperDetector(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: TamperDetector <input.jar> <output.jar> [verbose]");
            System.exit(1);
        }

        boolean verbose = args.length > 2 && Boolean.parseBoolean(args[2]);
        TamperDetector detector = new TamperDetector(verbose);
        detector.process(new File(args[0]), new File(args[1]));
    }

    public void process(File inputJar, File outputJar) throws IOException {
        Map<String, byte[]> classes = new HashMap<>();
        Map<String, byte[]> resources = new HashMap<>();

        // first pass: compute hashes of critical classes
        try (JarFile jar = new JarFile(inputJar)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                try (InputStream is = jar.getInputStream(entry)) {
                    byte[] bytes = is.readAllBytes();

                    if (entry.getName().endsWith(".class")) {
                        classes.put(entry.getName(), bytes);

                        // compute hash for critical classes
                        if (isCritical(entry.getName())) {
                            String hash = computeHash(bytes);
                            String className = entry.getName()
                                    .replace("/", ".")
                                    .replace(".class", "");
                            classHashes.put(className, hash);
                            classesProtected++;

                            if (verbose) {
                                System.out.println("protected: " + className);
                            }
                        }
                    } else {
                        resources.put(entry.getName(), bytes);
                    }
                }
            }
        }

        // second pass: inject verification into Client entrypoint
        byte[] clientClass = classes.get("base/client/Client.class");
        if (clientClass == null) {
            clientClass = classes.get("base/client/CattoWareClient.class");
        }

        if (clientClass != null) {
            clientClass = injectVerification(clientClass);
            classes.put("base/client/Client.class", clientClass);
        }

        // generate hash registry class
        byte[] registryClass = generateHashRegistry();
        classes.put("com/quantum/obfuscator/TamperRegistry.class", registryClass);

        // write output
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJar))) {
            for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
                JarEntry jarEntry = new JarEntry(entry.getKey());
                jos.putNextEntry(jarEntry);
                jos.write(entry.getValue());
                jos.closeEntry();
            }

            for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
                JarEntry jarEntry = new JarEntry(entry.getKey());
                jos.putNextEntry(jarEntry);
                jos.write(entry.getValue());
                jos.closeEntry();
            }
        }

        System.out.println("tamper detection: " + classesProtected + " classes protected");
    }

    private boolean isCritical(String name) {
        for (String pattern : CRITICAL_PATTERNS) {
            if (name.startsWith(pattern) || name.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String computeHash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private byte[] injectVerification(byte[] classBytes) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // find onInitializeClient or constructor
            for (MethodNode method : classNode.methods) {
                if (method.name.equals("onInitializeClient") ||
                        method.name.equals("onInitialize") ||
                        method.name.equals("<clinit>")) {

                    InsnList verification = new InsnList();

                    // call TamperRegistry.verify()
                    verification.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "com/quantum/obfuscator/TamperRegistry",
                            "verify",
                            "()V",
                            false));

                    // insert at beginning
                    method.instructions.insert(verification);
                    break;
                }
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
                @Override
                protected String getCommonSuperClass(String type1, String type2) {
                    return "java/lang/Object";
                }
            };
            classNode.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            return classBytes;
        }
    }

    private byte[] generateHashRegistry() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC,
                "com/quantum/obfuscator/TamperRegistry", null, "java/lang/Object", null);

        // static map field for hashes
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                "hashes", "Ljava/util/Map;", null, null);

        // static initializer - populate hashes
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();

            // hashes = new HashMap()
            mv.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "com/quantum/obfuscator/TamperRegistry",
                    "hashes", "Ljava/util/Map;");

            // add each hash
            for (Map.Entry<String, String> entry : classHashes.entrySet()) {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "com/quantum/obfuscator/TamperRegistry",
                        "hashes", "Ljava/util/Map;");
                mv.visitLdcInsn(entry.getKey());
                mv.visitLdcInsn(entry.getValue());
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
                mv.visitInsn(Opcodes.POP);
            }

            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(3, 0);
            mv.visitEnd();
        }

        // verify method
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "verify", "()V", null, null);
            mv.visitCode();

            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label catchHandler = new Label();

            mv.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Throwable");

            mv.visitLabel(tryStart);

            // for (Map.Entry<String, String> entry : hashes.entrySet())
            mv.visitFieldInsn(Opcodes.GETSTATIC, "com/quantum/obfuscator/TamperRegistry",
                    "hashes", "Ljava/util/Map;");
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "entrySet",
                    "()Ljava/util/Set;", true);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "iterator",
                    "()Ljava/util/Iterator;", true);
            mv.visitVarInsn(Opcodes.ASTORE, 0);

            Label loopStart = new Label();
            Label loopEnd = new Label();

            mv.visitLabel(loopStart);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
            mv.visitJumpInsn(Opcodes.IFEQ, loopEnd);

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next",
                    "()Ljava/lang/Object;", true);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/util/Map$Entry");
            mv.visitVarInsn(Opcodes.ASTORE, 1);

            // String className = entry.getKey()
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map$Entry", "getKey",
                    "()Ljava/lang/Object;", true);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
            mv.visitVarInsn(Opcodes.ASTORE, 2);

            // String expectedHash = entry.getValue()
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map$Entry", "getValue",
                    "()Ljava/lang/Object;", true);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            // if (!verifyClass(className, expectedHash)) crash()
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/quantum/obfuscator/TamperRegistry",
                    "verifyClass", "(Ljava/lang/String;Ljava/lang/String;)Z", false);

            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, continueLabel);

            // tamper detected - crash
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/quantum/obfuscator/TamperRegistry",
                    "crash", "()V", false);

            mv.visitLabel(continueLabel);
            mv.visitJumpInsn(Opcodes.GOTO, loopStart);

            mv.visitLabel(loopEnd);
            mv.visitLabel(tryEnd);
            mv.visitInsn(Opcodes.RETURN);

            mv.visitLabel(catchHandler);
            mv.visitVarInsn(Opcodes.ASTORE, 0);
            // exception during verification = tamper
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/quantum/obfuscator/TamperRegistry",
                    "crash", "()V", false);
            mv.visitInsn(Opcodes.RETURN);

            mv.visitMaxs(4, 4);
            mv.visitEnd();
        }

        // verifyClass method
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                    "verifyClass", "(Ljava/lang/String;Ljava/lang/String;)Z", null, null);
            mv.visitCode();

            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label catchHandler = new Label();

            mv.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Throwable");

            mv.visitLabel(tryStart);

            // Class<?> clazz = Class.forName(className)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
                    "(Ljava/lang/String;)Ljava/lang/Class;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 2);

            // String resourcePath = "/" + className.replace('.', '/') + ".class"
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn("/");
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>",
                    "(Ljava/lang/String;)V", false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, '.');
            mv.visitIntInsn(Opcodes.BIPUSH, '/');
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "replace",
                    "(CC)Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitLdcInsn(".class");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                    "()Ljava/lang/String;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            // InputStream is = clazz.getResourceAsStream(resourcePath)
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getResourceAsStream",
                    "(Ljava/lang/String;)Ljava/io/InputStream;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 4);

            // if (is == null) return false
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            Label notNullLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNONNULL, notNullLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);

            mv.visitLabel(notNullLabel);

            // byte[] bytes = is.readAllBytes()
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/InputStream", "readAllBytes",
                    "()[B", false);
            mv.visitVarInsn(Opcodes.ASTORE, 5);

            // is.close()
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/InputStream", "close", "()V", false);

            // String actualHash = computeHash(bytes)
            mv.visitVarInsn(Opcodes.ALOAD, 5);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/quantum/obfuscator/TamperRegistry",
                    "computeHash", "([B)Ljava/lang/String;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 6);

            // return expectedHash.equals(actualHash)
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 6);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
                    "(Ljava/lang/Object;)Z", false);

            mv.visitLabel(tryEnd);
            mv.visitInsn(Opcodes.IRETURN);

            mv.visitLabel(catchHandler);
            mv.visitVarInsn(Opcodes.ASTORE, 2);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);

            mv.visitMaxs(4, 7);
            mv.visitEnd();
        }

        // computeHash method
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                    "computeHash", "([B)Ljava/lang/String;", null, null);
            mv.visitCode();

            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label catchHandler = new Label();

            mv.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Throwable");

            mv.visitLabel(tryStart);

            // MessageDigest md = MessageDigest.getInstance("SHA-256")
            mv.visitLdcInsn("SHA-256");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/security/MessageDigest", "getInstance",
                    "(Ljava/lang/String;)Ljava/security/MessageDigest;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 1);

            // byte[] hash = md.digest(data)
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/security/MessageDigest", "digest",
                    "([B)[B", false);
            mv.visitVarInsn(Opcodes.ASTORE, 2);

            // StringBuilder sb = new StringBuilder()
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            // for each byte, format as hex
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 4);

            Label hashLoopStart = new Label();
            Label hashLoopEnd = new Label();

            mv.visitLabel(hashLoopStart);
            mv.visitVarInsn(Opcodes.ILOAD, 4);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, hashLoopEnd);

            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitLdcInsn("%02x");
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            mv.visitInsn(Opcodes.DUP);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ILOAD, 4);
            mv.visitInsn(Opcodes.BALOAD);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf",
                    "(B)Ljava/lang/Byte;", false);
            mv.visitInsn(Opcodes.AASTORE);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "format",
                    "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitInsn(Opcodes.POP);

            mv.visitIincInsn(4, 1);
            mv.visitJumpInsn(Opcodes.GOTO, hashLoopStart);

            mv.visitLabel(hashLoopEnd);
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                    "()Ljava/lang/String;", false);

            mv.visitLabel(tryEnd);
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitLabel(catchHandler);
            mv.visitVarInsn(Opcodes.ASTORE, 1);
            mv.visitLdcInsn("");
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitMaxs(6, 5);
            mv.visitEnd();
        }

        // crash method
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                    "crash", "()V", null, null);
            mv.visitCode();

            // subtle corruption - don't crash immediately
            // corrupt random static fields
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/quantum/obfuscator/AntiDebug",
                    "initializeProtection", "()V", false);

            // then halt after delay in background
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/Thread");
            mv.visitInsn(Opcodes.DUP);

            // Runnable lambda
            mv.visitInvokeDynamicInsn("run", "()Ljava/lang/Runnable;",
                    new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory",
                            "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                            false),
                    Type.getType("()V"),
                    new Handle(Opcodes.H_INVOKESTATIC, "com/quantum/obfuscator/TamperRegistry",
                            "crashDelayed", "()V", false),
                    Type.getType("()V"));

            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Thread", "<init>",
                    "(Ljava/lang/Runnable;)V", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "start", "()V", false);

            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(3, 0);
            mv.visitEnd();
        }

        // crashDelayed method (for lambda)
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                    "crashDelayed", "()V", null, null);
            mv.visitCode();

            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label catchHandler = new Label();

            mv.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Throwable");

            mv.visitLabel(tryStart);

            // Thread.sleep(random 5-15 seconds)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "random", "()D", false);
            mv.visitLdcInsn(10000.0);
            mv.visitInsn(Opcodes.DMUL);
            mv.visitInsn(Opcodes.D2L);
            mv.visitLdcInsn(5000L);
            mv.visitInsn(Opcodes.LADD);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);

            mv.visitLabel(tryEnd);

            // Runtime.getRuntime().halt(1)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Runtime", "getRuntime",
                    "()Ljava/lang/Runtime;", false);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime", "halt", "(I)V", false);

            mv.visitInsn(Opcodes.RETURN);

            mv.visitLabel(catchHandler);
            mv.visitVarInsn(Opcodes.ASTORE, 0);
            mv.visitInsn(Opcodes.RETURN);

            mv.visitMaxs(4, 1);
            mv.visitEnd();
        }

        cw.visitEnd();
        return cw.toByteArray();
    }
}
