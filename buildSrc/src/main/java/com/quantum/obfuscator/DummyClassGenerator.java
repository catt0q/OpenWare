package com.quantum.obfuscator;

import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * generates thousands of fake classes to hide real code
 * bloats JAR and overwhelms static analysis tools
 */
public class DummyClassGenerator {

    private static final Random random = new Random();
    private final boolean verbose;

    public DummyClassGenerator(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: DummyClassGenerator <output-dir> <count> [verbose]");
            System.exit(1);
        }

        String outputDir = args[0];
        int count = Integer.parseInt(args[1]);
        boolean verbose = args.length > 2 ? Boolean.parseBoolean(args[2]) : false;

        DummyClassGenerator generator = new DummyClassGenerator(verbose);
        generator.generateDummyClasses(new File(outputDir).toPath(), count);
    }

    public void generateDummyClasses(Path outputDir, int count) throws IOException {
        System.out.println("generating " + count + " dummy classes...");

        for (int i = 0; i < count; i++) {
            byte[] classBytes = generateDummyClass(i);

            // use obfuscated-style names
            String className = generateClassName(i);
            Path classFile = outputDir.resolve(className + ".class");

            Files.write(classFile, classBytes);

            if (verbose && i % 1000 == 0) {
                System.out.println("generated " + i + " classes...");
            }
        }

        System.out.println("dummy class generation complete: " + count + " fake classes created");
    }

    private String generateClassName(int index) {
        // generate names that blend in with ProGuard obfuscation
        // aa, ab, ac, ... aaa, aab, ...
        StringBuilder name = new StringBuilder();
        int num = index;

        do {
            name.insert(0, (char) ('a' + (num % 26)));
            num /= 26;
        } while (num > 0);

        // add "z" prefix so they sort to end and don't interfere with real obfuscated
        // classes
        return "z" + name.toString();
    }

    private byte[] generateDummyClass(int index) {
        String className = generateClassName(index);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };

        cw.visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER | Opcodes.ACC_SYNTHETIC,
                className,
                null,
                "java/lang/Object",
                null);

        // add some fake fields
        int fieldCount = random.nextInt(5) + 1;
        for (int i = 0; i < fieldCount; i++) {
            cw.visitField(
                    Opcodes.ACC_PRIVATE,
                    "f" + i,
                    randomFieldDescriptor(),
                    null,
                    null).visitEnd();
        }

        // add constructor
        MethodVisitor constructor = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        // initialize fields with random values
        for (int i = 0; i < fieldCount; i++) {
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            generateRandomConstant(constructor);
            constructor.visitFieldInsn(Opcodes.PUTFIELD, className, "f" + i, randomFieldDescriptor());
        }

        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(2, 1);
        constructor.visitEnd();

        // add some fake methods
        int methodCount = random.nextInt(8) + 2;
        for (int i = 0; i < methodCount; i++) {
            generateDummyMethod(cw, className, i);
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private void generateDummyMethod(ClassWriter cw, String className, int index) {
        String methodName = "m" + index;
        String descriptor = randomMethodDescriptor();

        MethodVisitor mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                methodName,
                descriptor,
                null,
                null);

        mv.visitCode();

        // generate fake logic
        int complexity = random.nextInt(20) + 5;
        for (int i = 0; i < complexity; i++) {
            switch (random.nextInt(5)) {
                case 0: // fake arithmetic
                    generateRandomConstant(mv);
                    generateRandomConstant(mv);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.POP);
                    break;
                case 1: // fake field access
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.GETFIELD, className, "f0", "I");
                    mv.visitInsn(Opcodes.POP);
                    break;
                case 2: // fake conditional
                    Label skip = new Label();
                    generateRandomConstant(mv);
                    mv.visitJumpInsn(Opcodes.IFEQ, skip);
                    generateRandomConstant(mv);
                    mv.visitInsn(Opcodes.POP);
                    mv.visitLabel(skip);
                    break;
                case 3: // fake loop
                    Label loopStart = new Label();
                    Label loopEnd = new Label();
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitVarInsn(Opcodes.ISTORE, 1);
                    mv.visitLabel(loopStart);
                    mv.visitVarInsn(Opcodes.ILOAD, 1);
                    mv.visitIntInsn(Opcodes.BIPUSH, 3);
                    mv.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);
                    mv.visitIincInsn(1, 1);
                    mv.visitJumpInsn(Opcodes.GOTO, loopStart);
                    mv.visitLabel(loopEnd);
                    break;
                case 4: // fake string op
                    mv.visitLdcInsn("dummy_" + random.nextInt(1000));
                    mv.visitInsn(Opcodes.POP);
                    break;
            }
        }

        // return appropriate value
        if (descriptor.endsWith("V")) {
            mv.visitInsn(Opcodes.RETURN);
        } else if (descriptor.endsWith("I")) {
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
        } else {
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
        }

        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    private String randomFieldDescriptor() {
        String[] types = { "I", "Z", "D", "F", "Ljava/lang/String;", "Ljava/lang/Object;" };
        return types[random.nextInt(types.length)];
    }

    private String randomMethodDescriptor() {
        String[] descriptors = {
                "()V", "()I", "()Z", "()Ljava/lang/String;",
                "(I)V", "(I)I", "(Z)Z", "(Ljava/lang/String;)V",
                "(II)I", "(Ljava/lang/String;I)V"
        };
        return descriptors[random.nextInt(descriptors.length)];
    }

    private void generateRandomConstant(MethodVisitor mv) {
        int value = random.nextInt(256);
        if (value <= 5) {
            mv.visitInsn(Opcodes.ICONST_0 + value);
        } else {
            mv.visitIntInsn(Opcodes.BIPUSH, value);
        }
    }
}
