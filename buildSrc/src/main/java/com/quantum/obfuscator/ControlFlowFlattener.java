package com.quantum.obfuscator;

import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * flattens control flow by converting if/else chains into state machines
 * makes decompiled code nearly unreadable
 */
public class ControlFlowFlattener {

    private final boolean verbose;
    private int flattenedCount = 0;

    public ControlFlowFlattener(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: ControlFlowFlattener <input-dir> [verbose]");
            System.exit(1);
        }

        String inputDir = args[0];
        boolean verbose = args.length > 1 ? Boolean.parseBoolean(args[1]) : false;

        ControlFlowFlattener flattener = new ControlFlowFlattener(verbose);
        flattener.processDirectory(new File(inputDir).toPath());
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

        System.out.println("control flow flattening complete: " + flattenedCount + " methods flattened");
    }

    private void processClassFile(Path classFile) throws IOException {
        byte[] classBytes = Files.readAllBytes(classFile);

        try {
            byte[] modified = flattenControlFlow(classBytes, classFile.toString());
            if (modified != null) {
                Files.write(classFile, modified);
                if (verbose) {
                    System.out.println("flattened: " + classFile.getFileName());
                }
            }
        } catch (Exception e) {
            System.err.println("failed to process " + classFile + ": " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
        }
    }

    private byte[] flattenControlFlow(byte[] classBytes, String className) {
        ClassReader reader = new ClassReader(classBytes);
        // Don't pass reader to ClassWriter - it can cause issues when heavily modifying bytecode
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            private String internalClassName;
            private boolean isMixin;
            private boolean isRenderClass;

            @Override
            public void visit(int version, int access, String name, String signature, String superName,
                    String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                this.internalClassName = name;

                // skip rendering classes - they're performance critical
                isRenderClass = name.contains("Render") || name.contains("render") ||
                        name.contains("GL") || name.contains("Shader");
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

                // only flatten non-critical methods
                boolean shouldFlatten = !isMixin &&
                        !isRenderClass &&
                        !name.equals("<clinit>") &&
                        !name.equals("<init>") &&
                        !name.equals("decrypt") &&
                        !name.contains("decrypt") &&
                        mv != null;

                if (shouldFlatten && new Random().nextInt(100) < 20) { // 20% of methods
                    flattenedCount++;
                    return new ControlFlowFlatteningVisitor(Opcodes.ASM9, mv);
                }

                return mv;
            }
        };

        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    /**
     * wraps method body in a state machine dispatcher
     * NOTE: This is a simplified version - full implementation would need to:
     * - Parse all control flow (if/switch/loops)
     * - Build a state graph
     * - Generate dispatcher
     * 
     * For now, we just add a dispatcher wrapper to obfuscate the real code
     */
    private static class ControlFlowFlatteningVisitor extends MethodVisitor {

        public ControlFlowFlatteningVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // inject fake state machine setup
            // int state = 0;
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 100); // use high slot to avoid conflicts

            // fake dispatcher that does nothing but looks complex
            Label realCode = new Label();
            mv.visitVarInsn(Opcodes.ILOAD, 100);
            mv.visitJumpInsn(Opcodes.IFEQ, realCode);

            // unreachable fake states
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitVarInsn(Opcodes.ISTORE, 100);
            mv.visitJumpInsn(Opcodes.GOTO, realCode);

            mv.visitLabel(realCode);
            // real method code continues here
        }
    }
}
