package com.quantum.obfuscator;

import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;

/**
 * adds fake exception handlers that never actually catch
 * confuses decompilers and makes control flow harder to follow
 */
public class FakeExceptionInjector {

    private static final SecureRandom random = new SecureRandom();
    private final boolean verbose;

    public FakeExceptionInjector(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: FakeExceptionInjector <input-dir> [verbose]");
            System.exit(1);
        }

        String inputDir = args[0];
        boolean verbose = args.length > 1 ? Boolean.parseBoolean(args[1]) : false;

        FakeExceptionInjector injector = new FakeExceptionInjector(verbose);
        injector.processDirectory(new File(inputDir).toPath());
    }

    public void processDirectory(Path directory) throws IOException {
        int[] count = { 0 };

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class")) {
                    processClassFile(file);
                    count[0]++;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("fake exception injection complete: " + count[0] + " classes processed");
    }

    private void processClassFile(Path classFile) throws IOException {
        byte[] classBytes = Files.readAllBytes(classFile);

        try {
            byte[] modified = injectFakeExceptions(classBytes);
            Files.write(classFile, modified);

            if (verbose) {
                System.out.println("injected exceptions: " + classFile.getFileName());
            }
        } catch (Exception e) {
            if (verbose) {
                System.err.println("failed to process " + classFile + ": " + e.getMessage());
            }
        }
    }

    private byte[] injectFakeExceptions(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            private boolean skipClass = false;

            @Override
            public void visit(int version, int access, String name, String signature, String superName,
                    String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);

                // ULTRA CONSERVATIVE: only allow specific safe packages
                boolean isSafePackage = name.startsWith("base/client/feature/impl/movement/") ||
                        name.startsWith("base/client/feature/impl/combat/") ||
                        name.startsWith("base/client/feature/impl/player/");

                // skip if not in safe packages
                if (!isSafePackage) {
                    skipClass = true;
                    return;
                }

                // skip ALL system/library classes
                if (name.startsWith("java/") ||
                        name.startsWith("javax/") ||
                        name.startsWith("sun/") ||
                        name.startsWith("jdk/") ||
                        name.startsWith("com/sun/") ||
                        name.startsWith("net/minecraft/") ||
                        name.startsWith("org/") ||
                        name.startsWith("com/mojang/")) {
                    skipClass = true;
                    return;
                }

                // skip ALL rendering/visual/gui classes
                if (name.contains("Render") ||
                        name.contains("render") ||
                        name.contains("Visual") ||
                        name.contains("visual") ||
                        name.contains("Gui") ||
                        name.contains("gui") ||
                        name.contains("Screen") ||
                        name.contains("screen") ||
                        name.contains("HUD") ||
                        name.contains("hud") ||
                        name.contains("Font") ||
                        name.contains("Shader") ||
                        name.contains("GL") ||
                        name.contains("Display")) {
                    skipClass = true;
                    return;
                }

                // skip effekseer (native)
                if (name.contains("Effekseer") || name.contains("swig")) {
                    skipClass = true;
                }
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                    String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                // skip if class flagged or abstract/native
                if (skipClass || mv == null || (access & Opcodes.ACC_ABSTRACT) != 0
                        || (access & Opcodes.ACC_NATIVE) != 0) {
                    return mv;
                }

                // skip constructors and static initializers
                if (name.equals("<clinit>") || name.equals("<init>")) {
                    return mv;
                }

                // only inject into 3% of methods (very low)
                if (random.nextInt(100) >= 3) {
                    return mv;
                }

                return new MethodVisitor(Opcodes.ASM9, mv) {
                    private Label methodStart = null;
                    private Label methodEnd = null;
                    private int insnCount = 0;

                    @Override
                    public void visitCode() {
                        super.visitCode();
                        methodStart = new Label();
                        methodEnd = new Label();
                        mv.visitLabel(methodStart);
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        insnCount++;
                        super.visitInsn(opcode);
                    }

                    @Override
                    public void visitEnd() {
                        if (methodStart != null && insnCount > 5 && random.nextInt(100) < 30) {
                            mv.visitLabel(methodEnd);

                            // add 1-3 fake exception handlers
                            int handlerCount = random.nextInt(3) + 1;
                            for (int i = 0; i < handlerCount; i++) {
                                addFakeHandler();
                            }
                        }
                        super.visitEnd();
                    }

                    private void addFakeHandler() {
                        Label handler = new Label();
                        Label handlerEnd = new Label();

                        // handler code (never reached)
                        mv.visitLabel(handler);
                        mv.visitInsn(Opcodes.POP); // pop exception
                        // fake recovery code
                        if (random.nextBoolean()) {
                            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J",
                                    false);
                            mv.visitInsn(Opcodes.POP2);
                        }
                        mv.visitLabel(handlerEnd);

                        // register impossible exception
                        String exceptionType = getFakeExceptionType();
                        mv.visitTryCatchBlock(methodStart, methodEnd, handler, exceptionType);
                    }

                    private String getFakeExceptionType() {
                        String[] fakeExceptions = {
                                "java/lang/NoSuchMethodError",
                                "java/lang/VerifyError",
                                "java/lang/IncompatibleClassChangeError",
                                "java/lang/AbstractMethodError",
                                "java/lang/LinkageError"
                        };
                        return fakeExceptions[random.nextInt(fakeExceptions.length)];
                    }
                };
            }
        };

        reader.accept(visitor, 0);
        return writer.toByteArray();
    }
}
