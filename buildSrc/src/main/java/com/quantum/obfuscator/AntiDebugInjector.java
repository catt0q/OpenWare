package com.quantum.obfuscator;

import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * injects anti-debug initialization into fabric entrypoint
 * only runs during obfuscated builds
 */
public class AntiDebugInjector {

    private final boolean verbose;

    public AntiDebugInjector(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: AntiDebugInjector <classes-dir> [verbose]");
            System.exit(1);
        }

        String classesDir = args[0];
        boolean verbose = args.length > 1 ? Boolean.parseBoolean(args[1]) : false;

        AntiDebugInjector injector = new AntiDebugInjector(verbose);
        injector.injectAntiDebug(new File(classesDir).toPath());
    }

    public void injectAntiDebug(Path classesDir) throws IOException {
        // find fabric entrypoint (CattoWareClient or Client)
        Path entrypointPath = findEntrypoint(classesDir);

        if (entrypointPath == null) {
            System.err.println("could not find fabric entrypoint");
            return;
        }

        if (verbose) {
            System.out.println("injecting anti-debug into: " + entrypointPath.getFileName());
        }

        byte[] classBytes = Files.readAllBytes(entrypointPath);
        byte[] modified = injectInitCall(classBytes);
        Files.write(entrypointPath, modified);

        System.out.println("anti-debug protection injected");
    }

    private Path findEntrypoint(Path classesDir) throws IOException {
        // inject into main entrypoint first (base.CattoWare has real code)
        // CattoWareClient.onInitializeClient is empty
        Path[] candidates = {
                classesDir.resolve("base/CattoWare.class"),
                classesDir.resolve("base/client/Client.class"),
                classesDir.resolve("base/client/CattoWareClient.class")
        };

        for (Path p : candidates) {
            if (Files.exists(p)) {
                return p;
            }
        }

        return null;
    }

    private byte[] injectInitCall(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                    String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                // inject into onInitializeClient or onInitialize
                if (mv != null && (name.equals("onInitializeClient") || name.equals("onInitialize"))) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();

                            // call AntiDebug.initializeProtection() at start of method
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESTATIC,
                                    "com/quantum/obfuscator/AntiDebug",
                                    "initializeProtection",
                                    "()V",
                                    false);
                        }
                    };
                }

                return mv;
            }
        };

        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }
}
