package com.quantum.obfuscator;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.util.*;

/**
 * dead code injector that adds useless index fields and constant mutations
 * runs before proguard to create noise in decompiled output
 */
public class DeadCodeInjector {

    private static final SecureRandom random = new SecureRandom();
    private static final int MIN_INDEX = 9_000_000;
    private static final int MAX_INDEX = 1_000_000_000;

    private final int fieldsPerClass;
    private final int mutationDensity;
    private final boolean verbose;

    public DeadCodeInjector(int fieldsPerClass, int mutationDensity, boolean verbose) {
        this.fieldsPerClass = fieldsPerClass;
        this.mutationDensity = mutationDensity;
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: DeadCodeInjector <input-dir> [fields-per-class] [mutation-density] [verbose]");
            System.exit(1);
        }

        String inputDir = args[0];
        int fieldsPerClass = args.length > 1 ? Integer.parseInt(args[1]) : 40;
        int mutationDensity = args.length > 2 ? Integer.parseInt(args[2]) : 7;
        boolean verbose = args.length > 3 ? Boolean.parseBoolean(args[3]) : false;

        DeadCodeInjector injector = new DeadCodeInjector(fieldsPerClass, mutationDensity, verbose);
        injector.processDirectory(new File(inputDir).toPath());
    }

    public void processDirectory(Path directory) throws IOException {
        int[] classCount = { 0 };

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class")) {
                    processClassFile(file);
                    classCount[0]++;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("dead code injection complete: " + classCount[0] + " classes processed");
    }

    private void processClassFile(Path classFile) throws IOException {
        byte[] classBytes = Files.readAllBytes(classFile);

        try {
            byte[] modified = injectDeadCode(classBytes, classFile.toString());
            Files.write(classFile, modified);

            if (verbose) {
                System.out.println("injected: " + classFile.getFileName());
            }
        } catch (Exception e) {
            System.err.println("failed to process " + classFile + ": " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
        }
    }

    private byte[] injectDeadCode(byte[] classBytes, String className) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };

        List<String> injectedFields = new ArrayList<>();

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            private String internalClassName;
            private boolean isInterface;
            private boolean isMixin;

            @Override
            public void visit(int version, int access, String name, String signature, String superName,
                    String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                this.internalClassName = name;
                this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                // detect mixin classes - don't inject into them
                if (descriptor.contains("Mixin")) {
                    isMixin = true;
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public void visitEnd() {
                // inject fields only if not interface and not mixin
                if (!isInterface && !isMixin) {
                    for (int i = 0; i < fieldsPerClass; i++) {
                        String fieldName = generateFieldName();
                        int initialValue = random.nextInt();

                        FieldVisitor fv = super.visitField(
                                Opcodes.ACC_PRIVATE,
                                fieldName,
                                "I", // int type
                                null,
                                initialValue);
                        if (fv != null) {
                            fv.visitEnd();
                            injectedFields.add(fieldName);
                        }
                    }
                }
                super.visitEnd();
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                    String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                // don't inject into interfaces, abstract methods, native methods, or mixin
                // classes
                if (mv == null || isInterface || isMixin || (access & Opcodes.ACC_ABSTRACT) != 0
                        || (access & Opcodes.ACC_NATIVE) != 0) {
                    return mv;
                }

                // don't inject if no fields were added
                if (injectedFields.isEmpty()) {
                    return mv;
                }

                return new MutationInjectorAdapter(Opcodes.ASM9, mv, access, name, descriptor, internalClassName,
                        injectedFields);
            }
        };

        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private String generateFieldName() {
        int indexNum = random.nextInt(MAX_INDEX - MIN_INDEX + 1) + MIN_INDEX;
        return "index" + indexNum;
    }

    private class MutationInjectorAdapter extends AdviceAdapter {
        private final String owner;
        private final List<String> fields;
        private int instructionCount = 0;

        protected MutationInjectorAdapter(int api, MethodVisitor mv, int access, String name, String descriptor,
                String owner, List<String> fields) {
            super(api, mv, access, name, descriptor);
            this.owner = owner;
            this.fields = new ArrayList<>(fields);
        }

        @Override
        public void visitInsn(int opcode) {
            // inject before certain instructions
            if (shouldInjectMutation()) {
                injectRandomMutation();
            }
            super.visitInsn(opcode);
            instructionCount++;
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            if (shouldInjectMutation()) {
                injectRandomMutation();
            }
            super.visitIntInsn(opcode, operand);
            instructionCount++;
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if (shouldInjectMutation()) {
                injectRandomMutation();
            }
            super.visitVarInsn(opcode, var);
            instructionCount++;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            if (shouldInjectMutation()) {
                injectRandomMutation();
            }
            super.visitFieldInsn(opcode, owner, name, descriptor);
            instructionCount++;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (shouldInjectMutation()) {
                injectRandomMutation();
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            instructionCount++;
        }

        private boolean shouldInjectMutation() {
            return instructionCount > 0 && instructionCount % mutationDensity == 0;
        }

        private void injectRandomMutation() {
            if (fields.isEmpty()) {
                return;
            }

            int mutationType = random.nextInt(100);

            if (mutationType < 60) {
                // 60% - simple assignment
                injectAssignment();
            } else if (mutationType < 90) {
                // 30% - increment/decrement
                injectIncrement();
            } else {
                // 10% - cross-field operation
                injectCrossFieldOperation();
            }
        }

        private void injectAssignment() {
            String field = fields.get(random.nextInt(fields.size()));

            mv.visitVarInsn(Opcodes.ALOAD, 0); // load this
            mv.visitLdcInsn(random.nextInt()); // load random value
            mv.visitFieldInsn(Opcodes.PUTFIELD, owner, field, "I");
        }

        private void injectIncrement() {
            String field = fields.get(random.nextInt(fields.size()));

            mv.visitVarInsn(Opcodes.ALOAD, 0); // load this
            mv.visitInsn(Opcodes.DUP); // duplicate this for later putfield
            mv.visitFieldInsn(Opcodes.GETFIELD, owner, field, "I"); // get field value

            int operation = random.nextInt(4);
            switch (operation) {
                case 0: // increment
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    break;
                case 1: // add random
                    mv.visitLdcInsn(random.nextInt(1000));
                    mv.visitInsn(Opcodes.IADD);
                    break;
                case 2: // xor
                    mv.visitLdcInsn(random.nextInt());
                    mv.visitInsn(Opcodes.IXOR);
                    break;
                case 3: // multiply by small number
                    mv.visitLdcInsn(random.nextInt(10) + 1);
                    mv.visitInsn(Opcodes.IMUL);
                    break;
            }

            mv.visitFieldInsn(Opcodes.PUTFIELD, owner, field, "I");
        }

        private void injectCrossFieldOperation() {
            if (fields.size() < 2) {
                injectAssignment();
                return;
            }

            String field1 = fields.get(random.nextInt(fields.size()));
            String field2 = fields.get(random.nextInt(fields.size()));

            // field1 = field1 op field2
            mv.visitVarInsn(Opcodes.ALOAD, 0); // load this
            mv.visitInsn(Opcodes.DUP); // duplicate this
            mv.visitFieldInsn(Opcodes.GETFIELD, owner, field1, "I"); // get field1
            mv.visitVarInsn(Opcodes.ALOAD, 0); // load this again
            mv.visitFieldInsn(Opcodes.GETFIELD, owner, field2, "I"); // get field2

            int operation = random.nextInt(4);
            switch (operation) {
                case 0:
                    mv.visitInsn(Opcodes.IADD);
                    break; // add
                case 1:
                    mv.visitInsn(Opcodes.IXOR);
                    break; // xor
                case 2:
                    mv.visitInsn(Opcodes.ISUB);
                    break; // subtract
                case 3:
                    mv.visitInsn(Opcodes.IOR);
                    break; // or
            }

            mv.visitFieldInsn(Opcodes.PUTFIELD, owner, field1, "I");
        }
    }
}
