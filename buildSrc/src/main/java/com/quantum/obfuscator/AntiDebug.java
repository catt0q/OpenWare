package com.quantum.obfuscator;

import java.io.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

/**
 * comprehensive anti-debug and anti-tamper protection
 * detects debuggers, agents, and bytecode modifications
 */
public class AntiDebug {

    private static volatile boolean detected = false;
    private static final Random random = new Random();
    private static Thread chaosThread;
    private static Thread crashThread;
    private static volatile boolean initialized = false;

    // bytecode hashes for tamper detection (set at build time)
    private static final Map<String, String> classHashes = new HashMap<>();

    // known debugger/agent signatures (bad)
    private static final String[] AGENT_SIGNATURES = {
            "-agentlib:", "-Xrunjdwp:", "-agentpath:",
            "-Xdebug", "-Xnoagent", "suspend=", "transport=dt_socket"
    };

    // whitelisted agent patterns (good - launcher integrations)
    private static final String[] WHITELIST_AGENTS = {
            "theseus.jar", // Modrinth launcher
            "prismlauncher", // Prism launcher
            "multimc", // MultiMC
            "atlauncher", // ATLauncher
            "gdlauncher" // GDLauncher
    };

    private static final String[] DEBUGGER_CLASSES = {
            "com.intellij.rt.debugger", "com.sun.jdi", "org.eclipse.debug",
            "com.yourkit", "jprofiler", "visualvm", "jconsole",
            "com.github.unidbg", "net.bytebuddy.agent", "org.jacoco.agent"
    };

    private static final String[] DECOMPILER_CLASSES = {
            "jadx", "procyon", "cfr", "fernflower", "jd.core", "quiltflower"
    };

    public static void initializeProtection() {
        if (initialized)
            return;
        initialized = true;

        // immediate checks
        if (performAllChecks()) {
            detected = true;
            triggerChaos();
            return;
        }

        // continuous monitoring with randomized intervals
        Thread monitor = new Thread(() -> {
            while (!detected) {
                try {
                    // randomize sleep to avoid timing analysis
                    Thread.sleep(random.nextInt(3000) + 2000);

                    if (performAllChecks()) {
                        detected = true;
                        triggerChaos();
                        break;
                    }
                } catch (InterruptedException e) {
                    // interruption is suspicious
                    detected = true;
                    triggerChaos();
                    break;
                } catch (Throwable t) {
                    // swallow other exceptions
                }
            }
        }, getRandomThreadName());

        monitor.setDaemon(true);
        monitor.setPriority(Thread.MIN_PRIORITY);
        monitor.start();
    }

    private static boolean performAllChecks() {
        return checkJvmArguments() ||
                checkSystemProperties() ||
                checkLoadedClasses() ||
                checkThreads() ||
                checkReflectionAccess() ||
                checkClassLoaderHierarchy() ||
                checkNativeAgents() ||
                checkEnvironment();
    }

    // method 1: JVM arguments
    private static boolean checkJvmArguments() {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            List<String> args = runtime.getInputArguments();
            String combined = String.join(" ", args).toLowerCase();

            // check whitelist first - skip detection if known-good launcher agent
            for (String whitelist : WHITELIST_AGENTS) {
                if (combined.contains(whitelist.toLowerCase())) {
                    return false;
                }
            }

            for (String sig : AGENT_SIGNATURES) {
                if (combined.contains(sig.toLowerCase())) {
                    return true;
                }
            }
        } catch (Throwable t) {
        }
        return false;
    }

    // method 2: system properties
    private static boolean checkSystemProperties() {
        try {
            String jdwpAddr = System.getProperty("sun.jdwp.listenerAddress");
            if (jdwpAddr != null && !jdwpAddr.isEmpty())
                return true;

            String intellijAgent = System.getProperty("intellij.debug.agent");
            if (intellijAgent != null && !intellijAgent.isEmpty())
                return true;

            // jdk.debug="release" is NORMAL, only trigger on debug values
            String jdkDebug = System.getProperty("jdk.debug");
            if (jdkDebug != null && !jdkDebug.equals("release"))
                return true;
        } catch (Throwable t) {
        }
        return false;
    }

    // method 3: loaded classes check
    private static boolean checkLoadedClasses() {
        try {
            // use reflection to get loaded classes
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            Field classesField = ClassLoader.class.getDeclaredField("classes");
            classesField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Vector<Class<?>> classes = (Vector<Class<?>>) classesField.get(cl);

            for (Class<?> clazz : classes.toArray(new Class<?>[0])) {
                String name = clazz.getName().toLowerCase();

                for (String sig : DEBUGGER_CLASSES) {
                    if (name.contains(sig.toLowerCase())) {
                        return true;
                    }
                }

                for (String sig : DECOMPILER_CLASSES) {
                    if (name.contains(sig.toLowerCase())) {
                        return true;
                    }
                }
            }
        } catch (Throwable t) {
        }
        return false;
    }

    // method 4: thread analysis
    private static boolean checkThreads() {
        try {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            long[] threadIds = threadBean.getAllThreadIds();

            String[] suspiciousNames = {
                    "JDWP", "Debugger", "Attach Listener", "Signal Dispatcher",
                    "JDI ", "Profiler", "YourKit", "JProfiler", "VisualVM"
            };

            for (long id : threadIds) {
                ThreadInfo info = threadBean.getThreadInfo(id);
                if (info != null) {
                    String name = info.getThreadName();
                    for (String sus : suspiciousNames) {
                        if (name.contains(sus)) {
                            // Attach Listener and Signal Dispatcher are normal, check more carefully
                            if (name.equals("Attach Listener") || name.equals("Signal Dispatcher")) {
                                continue; // these are normal JVM threads
                            }
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable t) {
        }
        return false;
    }

    // method 5: check if reflection is being intercepted
    private static boolean checkReflectionAccess() {
        try {
            // try to access internal VM class
            Class<?> vmClass = null;
            try {
                vmClass = Class.forName("jdk.internal.misc.VM");
            } catch (Throwable t) {
                try {
                    vmClass = Class.forName("sun.misc.VM");
                } catch (Throwable t2) {
                    return false;
                }
            }

            Method getSavedProperties = vmClass.getDeclaredMethod("getSavedProperties");
            getSavedProperties.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, String> savedProps = (Map<String, String>) getSavedProperties.invoke(null);

            for (String key : savedProps.keySet()) {
                String lowerKey = key.toLowerCase();
                if (lowerKey.contains("jdwp") || lowerKey.contains("agentlib")) {
                    return true;
                }
            }
        } catch (Throwable t) {
        }
        return false;
    }

    // method 6: classloader hierarchy check
    private static boolean checkClassLoaderHierarchy() {
        try {
            ClassLoader cl = AntiDebug.class.getClassLoader();
            while (cl != null) {
                String clName = cl.getClass().getName().toLowerCase();
                // only check for actual instrumentation, not generic "agent"
                // "agent" causes false positives with Fabric's loaders
                if (clName.contains("instrument") || clName.contains("retransform")) {
                    return true;
                }
                cl = cl.getParent();
            }
        } catch (Throwable t) {
        }
        return false;
    }

    // method 7: native agent detection via management
    private static boolean checkNativeAgents() {
        try {
            // check for native agent libraries
            String libraryPath = System.getProperty("java.library.path", "");
            String[] suspiciousLibs = { "jdwp", "debug", "agent", "profiler" };

            for (String lib : suspiciousLibs) {
                if (libraryPath.toLowerCase().contains(lib)) {
                    // could be false positive, need more signals
                }
            }

            // check boot classpath for agent jars
            String bootPath = System.getProperty("sun.boot.class.path", "");
            if (bootPath.toLowerCase().contains("agent")) {
                return true;
            }
        } catch (Throwable t) {
        }
        return false;
    }

    // method 8: environment variables
    private static boolean checkEnvironment() {
        try {
            Map<String, String> env = System.getenv();
            String[] suspiciousVars = {
                    "JAVA_TOOL_OPTIONS", "_JAVA_OPTIONS", "JDK_JAVA_OPTIONS"
            };

            for (String var : suspiciousVars) {
                String val = env.get(var);
                if (val != null) {
                    String lower = val.toLowerCase();
                    if (lower.contains("agent") || lower.contains("jdwp") || lower.contains("debug")) {
                        return true;
                    }
                }
            }
        } catch (Throwable t) {
        }
        return false;
    }

    // tamper detection - verify class bytecode hash
    public static void registerClassHash(String className, String hash) {
        classHashes.put(className, hash);
    }

    public static boolean verifyClassIntegrity(Class<?> clazz) {
        try {
            String name = clazz.getName();
            String expectedHash = classHashes.get(name);
            if (expectedHash == null)
                return true; // not registered

            // get bytecode of the class
            String resourcePath = "/" + name.replace('.', '/') + ".class";
            InputStream is = clazz.getResourceAsStream(resourcePath);
            if (is == null)
                return false;

            byte[] bytes = is.readAllBytes();
            is.close();

            String actualHash = computeHash(bytes);
            return expectedHash.equals(actualHash);
        } catch (Throwable t) {
            return false;
        }
    }

    private static String computeHash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Throwable t) {
            return "";
        }
    }

    private static void triggerChaos() {
        if (chaosThread != null)
            return;

        // memory corruption thread
        chaosThread = new Thread(() -> {
            while (true) {
                try {
                    corruptRandomClasses();
                    Thread.sleep(random.nextInt(100) + 50);
                } catch (Throwable t) {
                }
            }
        }, getRandomThreadName());
        chaosThread.setDaemon(false);
        chaosThread.setPriority(Thread.MAX_PRIORITY);
        chaosThread.start();

        // guaranteed crash after delay
        crashThread = new Thread(() -> {
            try {
                Thread.sleep(random.nextInt(10000) + 5000);
            } catch (Throwable t) {
            }

            // random crash method
            switch (random.nextInt(7)) {
                case 0 -> throw new OutOfMemoryError("heap space");
                case 1 -> throw new StackOverflowError();
                case 2 -> {
                    Object o = null;
                    o.hashCode();
                }
                case 3 -> throw new InternalError("VM error");
                case 4 -> Runtime.getRuntime().halt(random.nextInt(255) + 1);
                case 5 -> System.exit(random.nextInt(255) + 1);
                case 6 -> {
                    // infinite recursion
                    triggerChaos();
                }
            }
        }, getRandomThreadName());
        crashThread.setDaemon(false);
        crashThread.setPriority(Thread.MAX_PRIORITY);
        crashThread.start();
    }

    private static void corruptRandomClasses() {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            Field f = ClassLoader.class.getDeclaredField("classes");
            f.setAccessible(true);

            @SuppressWarnings("unchecked")
            Vector<Class<?>> classes = (Vector<Class<?>>) f.get(cl);
            Class<?>[] arr = classes.toArray(new Class<?>[0]);

            for (Class<?> clazz : arr) {
                if (random.nextInt(100) < 20) {
                    corruptClass(clazz);
                }
            }
        } catch (Throwable t) {
        }
    }

    private static void corruptClass(Class<?> clazz) {
        try {
            String name = clazz.getName();
            // skip JVM classes
            if (name.startsWith("java.") || name.startsWith("javax.") ||
                    name.startsWith("sun.") || name.startsWith("jdk.")) {
                return;
            }

            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) &&
                        !Modifier.isFinal(field.getModifiers())) {
                    try {
                        field.setAccessible(true);
                        Class<?> type = field.getType();
                        if (!type.isPrimitive()) {
                            field.set(null, null);
                        } else if (type == int.class) {
                            field.setInt(null, random.nextInt());
                        } else if (type == boolean.class) {
                            field.setBoolean(null, random.nextBoolean());
                        } else if (type == long.class) {
                            field.setLong(null, random.nextLong());
                        } else if (type == double.class) {
                            field.setDouble(null, random.nextDouble() * 1000);
                        } else if (type == float.class) {
                            field.setFloat(null, random.nextFloat() * 1000);
                        }
                    } catch (Throwable t) {
                    }
                }
            }
        } catch (Throwable t) {
        }
    }

    // generate innocent-looking thread name
    private static String getRandomThreadName() {
        String[] prefixes = {
                "pool-", "ForkJoinPool.", "Timer-", "Finalizer",
                "Reference Handler", "GC Daemon", "Common-Cleaner"
        };
        return prefixes[random.nextInt(prefixes.length)] + random.nextInt(100);
    }
}
