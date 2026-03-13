package com.quantum.obfuscator;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.jar.*;

/**
 * number obfuscation - replaces integer constants with calculated expressions
 * uses Tree API for proper instruction replacement
 */
public class NumberObfuscator {

    private static final SecureRandom random = new SecureRandom();
    private final boolean verbose;
    private int numbersObfuscated = 0;

    public NumberObfuscator(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: NumberObfuscator <input.jar> <output.jar> [verbose]");
            System.exit(1);
        }

        String inputJar = args[0];
        String outputJar = args[1];
        boolean verbose = args.length > 2 && Boolean.parseBoolean(args[2]);

        NumberObfuscator obfuscator = new NumberObfuscator(verbose);
        obfuscator.processJar(new File(inputJar), new File(outputJar));
    }

    public void processJar(File inputJar, File outputJar) throws IOException {
        try (JarFile jar = new JarFile(inputJar);
                JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJar))) {

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                try (InputStream is = jar.getInputStream(entry)) {
                    byte[] bytes = is.readAllBytes();

                    if (entry.getName().endsWith(".class") && shouldProcess(entry.getName())) {
                        try {
                            bytes = obfuscateClass(bytes);
                        } catch (Exception e) {
                            if (verbose) {
                                System.err.println("skipped: " + entry.getName() + " - " + e.getMessage());
                            }
                        }
                    }

                    JarEntry newEntry = new JarEntry(entry.getName());
                    jos.putNextEntry(newEntry);
                    jos.write(bytes);
                    jos.closeEntry();
                }
            }
        }

        System.out.println("numbers obfuscated: " + numbersObfuscated);
    }

    private boolean shouldProcess(String name) {
        // after proguard, our classes are in root or short packages
        // skip minecraft/fabric classes
        if (name.startsWith("net/minecraft/"))
            return false;
        if (name.startsWith("net/fabricmc/"))
            return false;
        if (name.startsWith("org/"))
            return false;
        if (name.startsWith("com/google/"))
            return false;
        if (name.startsWith("com/mojang/"))
            return false;
        // process everything else (our obfuscated classes)
        return true;
    }

    private byte[] obfuscateClass(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassNode classNode = new ClassNode();
        // skip existing frames - we'll recompute them
        reader.accept(classNode, ClassReader.SKIP_FRAMES);

        for (MethodNode method : classNode.methods) {
            // skip constructors and static initializers
            if (method.name.startsWith("<"))
                continue;
            if (method.instructions.size() < 10)
                continue;

            obfuscateMethod(method);
        }

        // use COMPUTE_FRAMES to generate fresh frames
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private void obfuscateMethod(MethodNode method) {
        ListIterator<AbstractInsnNode> iter = method.instructions.iterator();

        while (iter.hasNext()) {
            AbstractInsnNode insn = iter.next();
            int value = getIntValue(insn);

            if (value != Integer.MIN_VALUE && shouldObfuscate(value)) {
                InsnList replacement = generateObfuscation(value);
                if (replacement != null) {
                    // replace current instruction with obfuscated sequence
                    method.instructions.insertBefore(insn, replacement);
                    iter.remove();
                    numbersObfuscated++;
                }
            }
        }
    }

    private int getIntValue(AbstractInsnNode insn) {
        // get the integer value from various constant-loading instructions
        if (insn instanceof LdcInsnNode ldc) {
            if (ldc.cst instanceof Integer) {
                return (Integer) ldc.cst;
            }
        } else if (insn instanceof IntInsnNode intInsn) {
            if (intInsn.getOpcode() == Opcodes.BIPUSH || intInsn.getOpcode() == Opcodes.SIPUSH) {
                return intInsn.operand;
            }
        } else if (insn.getOpcode() >= Opcodes.ICONST_M1 && insn.getOpcode() <= Opcodes.ICONST_5) {
            return insn.getOpcode() - Opcodes.ICONST_0;
        }
        return Integer.MIN_VALUE; // sentinel for "no value"
    }

    private boolean shouldObfuscate(int value) {
        // skip common values and only obfuscate 30% of the time
        if (value == 0 || value == 1 || value == -1)
            return false;
        return random.nextInt(100) < 30;
    }

    private InsnList generateObfuscation(int value) {
        InsnList insns = new InsnList();
        int method = random.nextInt(5);

        switch (method) {
            case 0 -> {
                // XOR: (value ^ key) ^ key = value
                int key = random.nextInt();
                pushInt(insns, value ^ key);
                pushInt(insns, key);
                insns.add(new InsnNode(Opcodes.IXOR));
            }
            case 1 -> {
                // ADD/SUB: (value + offset) - offset = value
                int offset = random.nextInt(10000) + 1;
                pushInt(insns, value + offset);
                pushInt(insns, offset);
                insns.add(new InsnNode(Opcodes.ISUB));
            }
            case 2 -> {
                // MUL/DIV: (value * factor) / factor = value (only if divisible)
                int factor = 2 + random.nextInt(9); // 2-10
                if (value != 0 && (value * factor) / factor == value) {
                    pushInt(insns, value * factor);
                    pushInt(insns, factor);
                    insns.add(new InsnNode(Opcodes.IDIV));
                } else {
                    // fallback to XOR
                    int key = random.nextInt();
                    pushInt(insns, value ^ key);
                    pushInt(insns, key);
                    insns.add(new InsnNode(Opcodes.IXOR));
                }
            }
            case 3 -> {
                // NEG: -(-value) = value
                pushInt(insns, -value);
                insns.add(new InsnNode(Opcodes.INEG));
            }
            case 4 -> {
                // SHIFT: (value << shift) >> shift = value (for small positive values)
                if (value >= 0 && value < (1 << 20)) {
                    int shift = random.nextInt(8) + 1;
                    pushInt(insns, value << shift);
                    pushInt(insns, shift);
                    insns.add(new InsnNode(Opcodes.ISHR));
                } else {
                    // fallback to NEG
                    pushInt(insns, -value);
                    insns.add(new InsnNode(Opcodes.INEG));
                }
            }
        }

        return insns;
    }

    private void pushInt(InsnList insns, int value) {
        // use optimal instruction for the value
        if (value >= -1 && value <= 5) {
            insns.add(new InsnNode(Opcodes.ICONST_0 + value));
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            insns.add(new IntInsnNode(Opcodes.BIPUSH, value));
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            insns.add(new IntInsnNode(Opcodes.SIPUSH, value));
        } else {
            insns.add(new LdcInsnNode(value));
        }
    }
}
