package com.quantum.obfuscator;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * injects opaque predicates at SAFE points only
 * safe points = after xSTORE, after void invokes, at labels with empty stack
 * this prevents stack frame corruption
 */
public class OpaquePredicateInjector {

    private final Random random = new Random();
    private final boolean verbose;
    private int predicatesInjected = 0;

    public OpaquePredicateInjector(boolean verbose) {
        this.verbose = verbose;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: OpaquePredicateInjector <input.jar> <output.jar> [verbose]");
            System.exit(1);
        }

        boolean verbose = args.length > 2 && Boolean.parseBoolean(args[2]);
        OpaquePredicateInjector injector = new OpaquePredicateInjector(verbose);
        injector.process(new File(args[0]), new File(args[1]));
    }

    public void process(File inputJar, File outputJar) throws IOException {
        try (JarFile jar = new JarFile(inputJar);
                JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJar))) {

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                try (InputStream is = jar.getInputStream(entry)) {
                    if (entry.getName().endsWith(".class") && shouldProcess(entry.getName())) {
                        byte[] classBytes = is.readAllBytes();
                        byte[] modified = injectPredicates(classBytes);

                        JarEntry newEntry = new JarEntry(entry.getName());
                        jos.putNextEntry(newEntry);
                        jos.write(modified);
                    } else {
                        JarEntry newEntry = new JarEntry(entry.getName());
                        jos.putNextEntry(newEntry);
                        jos.write(is.readAllBytes());
                    }
                    jos.closeEntry();
                }
            }
        }

        System.out.println("opaque predicates injected: " + predicatesInjected);
    }

    private boolean shouldProcess(String name) {
        // only process our classes, not minecraft
        return name.startsWith("base/") || name.startsWith("com/quantum/");
    }

    private byte[] injectPredicates(byte[] classBytes) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                if (method.instructions.size() < 20)
                    continue;
                if (method.name.startsWith("<"))
                    continue; // skip constructors
                if ((method.access & Opcodes.ACC_ABSTRACT) != 0)
                    continue;
                if ((method.access & Opcodes.ACC_NATIVE) != 0)
                    continue;

                injectToMethod(method);
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
            if (verbose) {
                System.err.println("failed to process class: " + t.getMessage());
            }
            return classBytes;
        }
    }

    private void injectToMethod(MethodNode method) {
        // find all safe injection points (where stack is empty)
        List<AbstractInsnNode> safePoints = findSafePoints(method);

        if (safePoints.isEmpty())
            return;

        // inject at most 1 predicate per method to minimize impact
        int maxInject = Math.min(1, safePoints.size());

        // shuffle and pick random safe points
        Collections.shuffle(safePoints, random);

        for (int i = 0; i < maxInject; i++) {
            AbstractInsnNode safePoint = safePoints.get(i);
            InsnList predicate = generateOpaquePredicate();

            // insert AFTER the safe point instruction
            method.instructions.insert(safePoint, predicate);
            predicatesInjected++;
        }
    }

    private List<AbstractInsnNode> findSafePoints(MethodNode method) {
        List<AbstractInsnNode> safePoints = new ArrayList<>();

        for (AbstractInsnNode insn : method.instructions) {
            if (isSafePoint(insn)) {
                // verify next instruction isn't a label/frame (would break control flow)
                AbstractInsnNode next = insn.getNext();
                if (next != null && !(next instanceof LabelNode) && !(next instanceof FrameNode)) {
                    safePoints.add(insn);
                }
            }
        }

        return safePoints;
    }

    private boolean isSafePoint(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();

        // after any store instruction - stack just popped a value, now empty/balanced
        if (opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE)
            return true;
        // short form stores: ISTORE_0(59) through ASTORE_3(78)
        if (opcode >= 59 && opcode <= 78)
            return true;

        // after POP/POP2 - explicitly cleared stack
        if (opcode == Opcodes.POP || opcode == Opcodes.POP2)
            return true;

        // after void method calls - stack unchanged
        if (insn instanceof MethodInsnNode methodInsn) {
            if (methodInsn.desc.endsWith(")V")) {
                return true;
            }
        }

        // after PUTFIELD/PUTSTATIC - stack cleared
        if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC)
            return true;

        // after MONITORENTER/MONITOREXIT
        if (opcode == Opcodes.MONITORENTER || opcode == Opcodes.MONITOREXIT)
            return true;

        return false;
    }

    private InsnList generateOpaquePredicate() {
        InsnList insns = new InsnList();

        int type = random.nextInt(4);
        LabelNode skipLabel = new LabelNode();

        // use a local variable slot that's unlikely to conflict (slot 100+)
        int fakeLocal = 100 + random.nextInt(50);

        switch (type) {
            case 0 -> {
                // 0 == 0 is always true (simplest)
                insns.add(new InsnNode(Opcodes.ICONST_0));
                insns.add(new VarInsnNode(Opcodes.ISTORE, fakeLocal));
                insns.add(new VarInsnNode(Opcodes.ILOAD, fakeLocal));
                insns.add(new JumpInsnNode(Opcodes.IFEQ, skipLabel));
                addDeadCode(insns);
                insns.add(skipLabel);
            }
            case 1 -> {
                // 1 != 0 is always true
                insns.add(new InsnNode(Opcodes.ICONST_1));
                insns.add(new VarInsnNode(Opcodes.ISTORE, fakeLocal));
                insns.add(new VarInsnNode(Opcodes.ILOAD, fakeLocal));
                insns.add(new JumpInsnNode(Opcodes.IFNE, skipLabel));
                addDeadCode(insns);
                insns.add(skipLabel);
            }
            case 2 -> {
                // (1 * 1) == 1 is always true
                insns.add(new InsnNode(Opcodes.ICONST_1));
                insns.add(new InsnNode(Opcodes.ICONST_1));
                insns.add(new InsnNode(Opcodes.IMUL));
                insns.add(new InsnNode(Opcodes.ICONST_1));
                insns.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, skipLabel));
                addDeadCode(insns);
                insns.add(skipLabel);
            }
            case 3 -> {
                // (2 - 1) > 0 is always true
                insns.add(new InsnNode(Opcodes.ICONST_2));
                insns.add(new InsnNode(Opcodes.ICONST_1));
                insns.add(new InsnNode(Opcodes.ISUB));
                insns.add(new JumpInsnNode(Opcodes.IFGT, skipLabel));
                addDeadCode(insns);
                insns.add(skipLabel);
            }
        }

        return insns;
    }

    private void addDeadCode(InsnList insns) {
        // ONLY use ATHROW - guaranteed to terminate flow regardless of return type
        int deadType = random.nextInt(2);
        switch (deadType) {
            case 0 -> {
                // throw RuntimeException
                insns.add(new TypeInsnNode(Opcodes.NEW, "java/lang/RuntimeException"));
                insns.add(new InsnNode(Opcodes.DUP));
                insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                        "java/lang/RuntimeException", "<init>", "()V", false));
                insns.add(new InsnNode(Opcodes.ATHROW));
            }
            case 1 -> {
                // throw Error
                insns.add(new TypeInsnNode(Opcodes.NEW, "java/lang/Error"));
                insns.add(new InsnNode(Opcodes.DUP));
                insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                        "java/lang/Error", "<init>", "()V", false));
                insns.add(new InsnNode(Opcodes.ATHROW));
            }
        }
    }
}
