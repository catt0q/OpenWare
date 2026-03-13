package com.quantum.obfuscator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

/**
 * generates fake java source files with hundreds of random variables,
 * fake control flow, and methods that are never called
 * these compile into the JAR and make reverse engineering significantly harder
 */
public class JunkCodeGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String[] FAKE_PACKAGES = {
            "base.internal.core",
            "base.internal.util",
            "base.internal.system",
            "base.internal.network",
            "base.internal.cache"
    };

    private final int classCount;
    private final int methodsPerClass;
    private final int variablesPerMethod;
    private final Path outputDir;

    public JunkCodeGenerator(Path outputDir, int classCount, int methodsPerClass, int variablesPerMethod) {
        this.outputDir = outputDir;
        this.classCount = classCount;
        this.methodsPerClass = methodsPerClass;
        this.variablesPerMethod = variablesPerMethod;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println(
                    "usage: JunkCodeGenerator <output-dir> [class-count] [methods-per-class] [vars-per-method]");
            System.exit(1);
        }

        Path outputDir = new File(args[0]).toPath();
        int classCount = args.length > 1 ? Integer.parseInt(args[1]) : 15;
        int methodsPerClass = args.length > 2 ? Integer.parseInt(args[2]) : 25;
        int variablesPerMethod = args.length > 3 ? Integer.parseInt(args[3]) : 80;

        JunkCodeGenerator generator = new JunkCodeGenerator(outputDir, classCount, methodsPerClass, variablesPerMethod);
        generator.generate();
    }

    public void generate() throws IOException {
        System.out.println("generating " + classCount + " junk classes...");

        for (int i = 0; i < classCount; i++) {
            String pkg = FAKE_PACKAGES[random.nextInt(FAKE_PACKAGES.length)];
            String className = generateClassName();
            generateJunkClass(pkg, className);
        }

        System.out.println("junk code generation complete: " + classCount + " classes created");
    }

    private String generateClassName() {
        String[] prefixes = { "System", "Network", "Cache", "Buffer", "Handler", "Manager", "Processor", "Controller",
                "Service" };
        String[] suffixes = { "Impl", "Util", "Helper", "Core", "Engine", "Worker", "Task", "Job" };
        return prefixes[random.nextInt(prefixes.length)] + suffixes[random.nextInt(suffixes.length)]
                + random.nextInt(1000);
    }

    private void generateJunkClass(String packageName, String className) throws IOException {
        StringBuilder code = new StringBuilder();

        // package declaration
        code.append("package ").append(packageName).append(";\n\n");

        // fake imports to look legitimate
        code.append("import java.util.*;\n");
        code.append("import java.io.*;\n");
        code.append("import java.nio.*;\n\n");

        // javadoc to make it look real
        code.append("/**\n");
        code.append(" * internal system component - do not modify\n");
        code.append(" * auto-generated from build configuration\n");
        code.append(" */\n");

        // class declaration
        code.append("public class ").append(className).append(" {\n\n");

        // static initialization block with fake logic
        code.append("    static {\n");
        int initVarNum = random.nextInt(1000);
        code.append("        int init").append(initVarNum).append(" = ").append(random.nextInt())
                .append(";\n");
        code.append("        if (init").append(initVarNum).append(" > Integer.MAX_VALUE - 1000) {\n");
        code.append("            System.err.println(\"critical initialization error\");\n");
        code.append("        }\n");
        code.append("    }\n\n");

        // generate massive field list
        int fieldCount = 150 + random.nextInt(100);
        for (int i = 0; i < fieldCount; i++) {
            String fieldName = "var" + (900000000 + random.nextInt(100000000));
            String type = getRandomType();
            String modifier = random.nextBoolean() ? "private" : "private static";
            code.append("    ").append(modifier).append(" ").append(type).append(" ").append(fieldName);

            // random initialization
            if (random.nextBoolean()) {
                code.append(" = ").append(getRandomValue(type));
            }
            code.append(";\n");
        }
        code.append("\n");

        // generate methods with fake control flow (with counter to ensure unique names)
        for (int i = 0; i < methodsPerClass; i++) {
            generateJunkMethod(code, i);
        }

        code.append("}\n");

        // write file
        Path pkgDir = outputDir.resolve(packageName.replace('.', File.separatorChar));
        Files.createDirectories(pkgDir);

        Path javaFile = pkgDir.resolve(className + ".java");
        try (FileWriter writer = new FileWriter(javaFile.toFile())) {
            writer.write(code.toString());
        }
    }

    private void generateJunkMethod(StringBuilder code, int methodIndex) {
        String methodName = "process" + methodIndex + "_" + random.nextInt(10000);
        String returnType = random.nextBoolean() ? "void" : getRandomType();
        String modifier = random.nextBoolean() ? "private" : "private static";

        code.append("    /**\n");
        code.append("     * internal processing routine\n");
        code.append("     */\n");
        code.append("    ").append(modifier).append(" ").append(returnType).append(" ").append(methodName)
                .append("() {\n");

        // generate tons of local variables
        for (int i = 0; i < variablesPerMethod; i++) {
            String varName = "local" + (900000000 + random.nextInt(100000000));
            String type = getRandomType();
            code.append("        ").append(type).append(" ").append(varName).append(" = ").append(getRandomValue(type))
                    .append(";\n");
        }

        // generate fake control flow
        int controlFlowCount = 10 + random.nextInt(15);
        for (int i = 0; i < controlFlowCount; i++) {
            generateFakeControlFlow(code);
        }

        // generate fake computations
        for (int i = 0; i < 20 + random.nextInt(30); i++) {
            generateFakeComputation(code);
        }

        // fake exception handling
        if (random.nextBoolean()) {
            code.append("        try {\n");
            code.append("            int tmp").append(random.nextInt(1000)).append(" = Integer.parseInt(\"")
                    .append(random.nextInt()).append("\");\n");
            code.append("        } catch (Exception e").append(random.nextInt(100)).append(") {\n");
            code.append("            // ignore\n");
            code.append("        }\n");
        }

        // return statement if needed
        if (!returnType.equals("void")) {
            code.append("        return ").append(getRandomValue(returnType)).append(";\n");
        }

        code.append("    }\n\n");
    }

    private void generateFakeControlFlow(StringBuilder code) {
        int choice = random.nextInt(5);
        int varNum = random.nextInt(100000000);

        switch (choice) {
            case 0: // impossible if
                code.append("        if (Integer.MAX_VALUE < Integer.MIN_VALUE) {\n");
                code.append("            int unreachable").append(varNum).append(" = ").append(random.nextInt())
                        .append(";\n");
                code.append("        }\n");
                break;

            case 1: // always true but looks complex
                code.append("        if ((").append(random.nextInt()).append(" ^ ").append(random.nextInt())
                        .append(") != ").append(Integer.MAX_VALUE).append(") {\n");
                code.append("            int always").append(varNum).append(" = ").append(random.nextInt())
                        .append(";\n");
                code.append("        }\n");
                break;

            case 2: // nested conditions
                code.append("        if (").append(random.nextInt()).append(" > 0) {\n");
                code.append("            if (").append(random.nextInt()).append(" < 0) {\n");
                code.append("                int nested").append(varNum).append(" = ").append(random.nextInt())
                        .append(";\n");
                code.append("            } else {\n");
                code.append("                int other").append(varNum).append(" = ").append(random.nextInt())
                        .append(";\n");
                code.append("            }\n");
                code.append("        }\n");
                break;

            case 3: // switch with fake cases
                code.append("        switch (").append(random.nextInt(10)).append(") {\n");
                for (int i = 0; i < 5; i++) {
                    code.append("            case ").append(i).append(":\n");
                    code.append("                int case").append(varNum).append("_").append(i).append(" = ")
                            .append(random.nextInt()).append(";\n");
                    code.append("                break;\n");
                }
                code.append("            default:\n");
                code.append("                int def").append(varNum).append(" = ").append(random.nextInt())
                        .append(";\n");
                code.append("        }\n");
                break;

            case 4: // fake loop
                code.append("        for (int i").append(varNum).append(" = 0; i").append(varNum).append(" < 0; i")
                        .append(varNum).append("++) {\n");
                code.append("            int loop").append(varNum).append(" = ").append(random.nextInt()).append(";\n");
                code.append("        }\n");
                break;
        }
    }

    private void generateFakeComputation(StringBuilder code) {
        int var1 = random.nextInt(100000000);
        int var2 = random.nextInt(100000000);

        String[] ops = { "+", "-", "*", "/", "%", "^", "|", "&", "<<", ">>", ">>>" };
        String op = ops[random.nextInt(ops.length)];

        code.append("        int calc").append(var1).append(" = ").append(random.nextInt());
        code.append(" ").append(op).append(" ").append(random.nextInt(1000) + 1).append(";\n");

        // cross-variable operations
        if (random.nextBoolean()) {
            code.append("        calc").append(var1).append(" = calc").append(var1).append(" ^ ")
                    .append(random.nextInt()).append(";\n");
        }
    }

    private String getRandomType() {
        String[] types = { "int", "long", "boolean", "byte", "short", "float", "double", "String" };
        return types[random.nextInt(types.length)];
    }

    private String getRandomValue(String type) {
        switch (type) {
            case "int":
                return String.valueOf(random.nextInt());
            case "byte":
                return String.valueOf((byte) random.nextInt());
            case "short":
                return String.valueOf((short) random.nextInt());
            case "long":
                return String.valueOf(random.nextLong()) + "L";
            case "boolean":
                return String.valueOf(random.nextBoolean());
            case "float":
                return String.valueOf(random.nextFloat()) + "f";
            case "double":
                return String.valueOf(random.nextDouble()) + "d";
            case "String":
                return "\"" + UUID.randomUUID().toString().substring(0, 8) + "\"";
            default:
                return "null";
        }
    }
}
