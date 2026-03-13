package com.quantum.obfuscator;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/**
 * replaces direct method calls with reflection-based invocations
 * makes static analysis much harder
 * target: critical methods like auth checks, hwid, etc
 */
public class ReflectionObfuscator {

    private final Random random = new Random();
    private final boolean verbose;
    private int methodsReflected = 0;

    // methods to replace with reflection (class -> method -> descriptor)
    private static final Map<String, Set<String>> TARGET_METHODS = new HashMap<>();

    static {
        // auth-related methods
        Set<String> authMethods = new HashSet<>();
        authMethods.add("login");
        authMethods.add("authenticate");
        authMethods.add("verify");
        authMethods.add("checkLicense");
        authMethods.add("validateToken");
        authMethods.add("getHWID");
        authMethods.add("computeHWID");
        TARGET_METHODS.put("base/client/auth/", authMethods);

        // crypto methods
        Set<String> cryptoMethods = new HashSet<>();
        cryptoMethods.add("encrypt");
        cryptoMethods.add("decrypt");
        cryptoMethods.add("hash");
        cryptoMethods.add("sign");
        cryptoMethods.add("verify");
        TARGET_METHODS.put("base/client/crypto/", cryptoMethods);
    }

    public ReflectionObfuscator(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: ReflectionObfuscator <input.jar> <output.jar> [verbose]");
            System.exit(1);
        }

        boolean verbose = args.length > 2 && Boolean.parseBoolean(args[2]);
        ReflectionObfuscator obfuscator = new ReflectionObfuscator(verbose);
        obfuscator.process(new File(args[0]), new File(args[1]));
    }

    public void process(File inputJar, File outputJar) throws IOException {
        // first pass: inject helper class
        Map<String, byte[]> classes = new HashMap<>();

        try (JarFile jar = new JarFile(inputJar)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                try (InputStream is = jar.getInputStream(entry)) {
                    byte[] bytes = is.readAllBytes();

                    if (entry.getName().endsWith(".class") && shouldProcess(entry.getName())) {
                        bytes = obfuscateClass(bytes);
                    }

                    classes.put(entry.getName(), bytes);
                }
            }
        }

        // add helper class
        classes.put("com/quantum/obfuscator/ReflectionHelper.class", generateHelperClass());

        // write output
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJar))) {
            for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
                JarEntry jarEntry = new JarEntry(entry.getKey());
                jos.putNextEntry(jarEntry);
                jos.write(entry.getValue());
                jos.closeEntry();
            }
        }

        System.out.println("methods converted to reflection: " + methodsReflected);
    }

    private boolean shouldProcess(String name) {
        return name.startsWith("base/");
    }

    private byte[] obfuscateClass(byte[] classBytes) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                obfuscateMethod(method);
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

    private void obfuscateMethod(MethodNode method) {
        InsnList instructions = method.instructions;

        for (int i = 0; i < instructions.size(); i++) {
            AbstractInsnNode insn = instructions.get(i);

            if (insn instanceof MethodInsnNode methodInsn) {
                if (shouldReflect(methodInsn)) {
                    InsnList replacement = generateReflectionCall(methodInsn);
                    if (replacement != null) {
                        instructions.insertBefore(insn, replacement);
                        instructions.remove(insn);
                        methodsReflected++;
                    }
                }
            }
        }
    }

    private boolean shouldReflect(MethodInsnNode methodInsn) {
        String owner = methodInsn.owner;
        String name = methodInsn.name;

        // skip constructors and static initializers
        if (name.startsWith("<"))
            return false;

        // ONLY reflect methods we explicitly target - no random reflection
        // random reflection breaks after ProGuard renames classes
        for (Map.Entry<String, Set<String>> entry : TARGET_METHODS.entrySet()) {
            if (owner.startsWith(entry.getKey())) {
                if (entry.getValue().contains(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    private InsnList generateReflectionCall(MethodInsnNode original) {
        InsnList insns = new InsnList();

        boolean isStatic = original.getOpcode() == Opcodes.INVOKESTATIC;
        Type[] argTypes = Type.getArgumentTypes(original.desc);
        Type returnType = Type.getReturnType(original.desc);

        // for simplicity, only handle static methods with basic types
        if (!isStatic)
            return null;
        if (argTypes.length > 3)
            return null;

        // encode class and method name
        String encodedClass = encodeString(original.owner.replace('/', '.'));
        String encodedMethod = encodeString(original.name);

        // store arguments in local array
        int arrayVar = 10 + random.nextInt(5); // use high local var index

        // create Object[] for parameters
        insns.add(new IntInsnNode(Opcodes.BIPUSH, argTypes.length));
        insns.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));

        // we need to save current stack values into the array
        // this is complex because we need to reverse the order
        // for now, use the helper method approach

        // push encoded strings
        insns.add(new LdcInsnNode(encodedClass));
        insns.add(new LdcInsnNode(encodedMethod));
        insns.add(new LdcInsnNode(original.desc));

        // call helper
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "com/quantum/obfuscator/ReflectionHelper",
                "invokeEncoded",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",
                false));

        // cast result if needed
        if (returnType.getSort() == Type.VOID) {
            insns.add(new InsnNode(Opcodes.POP));
        } else if (returnType.getSort() == Type.OBJECT || returnType.getSort() == Type.ARRAY) {
            insns.add(new TypeInsnNode(Opcodes.CHECKCAST, returnType.getInternalName()));
        } else {
            // primitive - need to unbox
            String wrapperType = getWrapperType(returnType);
            insns.add(new TypeInsnNode(Opcodes.CHECKCAST, wrapperType));
            insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                    wrapperType, getUnboxMethod(returnType), "()" + returnType.getDescriptor(), false));
        }

        return insns;
    }

    private String encodeString(String s) {
        // simple XOR encoding
        StringBuilder sb = new StringBuilder();
        int key = 0x42;
        for (char c : s.toCharArray()) {
            sb.append((char) (c ^ key));
        }
        return sb.toString();
    }

    private String getWrapperType(Type type) {
        return switch (type.getSort()) {
            case Type.BOOLEAN -> "java/lang/Boolean";
            case Type.BYTE -> "java/lang/Byte";
            case Type.CHAR -> "java/lang/Character";
            case Type.SHORT -> "java/lang/Short";
            case Type.INT -> "java/lang/Integer";
            case Type.LONG -> "java/lang/Long";
            case Type.FLOAT -> "java/lang/Float";
            case Type.DOUBLE -> "java/lang/Double";
            default -> "java/lang/Object";
        };
    }

    private String getUnboxMethod(Type type) {
        return switch (type.getSort()) {
            case Type.BOOLEAN -> "booleanValue";
            case Type.BYTE -> "byteValue";
            case Type.CHAR -> "charValue";
            case Type.SHORT -> "shortValue";
            case Type.INT -> "intValue";
            case Type.LONG -> "longValue";
            case Type.FLOAT -> "floatValue";
            case Type.DOUBLE -> "doubleValue";
            default -> "toString";
        };
    }

    private byte[] generateHelperClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC,
                "com/quantum/obfuscator/ReflectionHelper", null, "java/lang/Object", null);

        // decode method
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                    "decode", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
            mv.visitCode();

            // StringBuilder sb = new StringBuilder()
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ASTORE, 1);

            // for each char, XOR with key and append
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 2); // i = 0

            Label loopStart = new Label();
            Label loopEnd = new Label();

            mv.visitLabel(loopStart);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);

            // sb.append((char)(s.charAt(i) ^ 0x42))
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
            mv.visitIntInsn(Opcodes.BIPUSH, 0x42);
            mv.visitInsn(Opcodes.IXOR);
            mv.visitInsn(Opcodes.I2C);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(C)Ljava/lang/StringBuilder;", false);
            mv.visitInsn(Opcodes.POP);

            // i++
            mv.visitIincInsn(2, 1);
            mv.visitJumpInsn(Opcodes.GOTO, loopStart);

            mv.visitLabel(loopEnd);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                    "()Ljava/lang/String;", false);
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitMaxs(4, 3);
            mv.visitEnd();
        }

        // invokeEncoded method
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "invokeEncoded",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",
                    null, null);
            mv.visitCode();

            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label catchHandler = new Label();

            mv.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Throwable");

            mv.visitLabel(tryStart);

            // String className = decode(encodedClass)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/quantum/obfuscator/ReflectionHelper",
                    "decode", "(Ljava/lang/String;)Ljava/lang/String;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            // String methodName = decode(encodedMethod)
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/quantum/obfuscator/ReflectionHelper",
                    "decode", "(Ljava/lang/String;)Ljava/lang/String;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 4);

            // Class<?> clazz = Class.forName(className)
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
                    "(Ljava/lang/String;)Ljava/lang/Class;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 5);

            // Method method = clazz.getDeclaredMethod(methodName)
            mv.visitVarInsn(Opcodes.ALOAD, 5);
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 6);

            // method.setAccessible(true)
            mv.visitVarInsn(Opcodes.ALOAD, 6);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible",
                    "(Z)V", false);

            // return method.invoke(null)
            mv.visitVarInsn(Opcodes.ALOAD, 6);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);

            mv.visitLabel(tryEnd);
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitLabel(catchHandler);
            mv.visitVarInsn(Opcodes.ASTORE, 3);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitMaxs(5, 7);
            mv.visitEnd();
        }

        cw.visitEnd();
        return cw.toByteArray();
    }
}
