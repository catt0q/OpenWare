package base.client.effekseer.installer;

import Effekseer.swig.EffekseerBackendCore;
import Effekseer.swig.EffekseerManagerCore;
import lombok.Getter;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class LoadNatives {
    public final File nativesDir = new File("effekser_natives");
    @Getter
    private EffekseerManagerCore effekseerManagerCore;

    public void init() {
        ResourceProvider resourceProvider = Minecraft.getInstance().getResourceManager();

        if (resourceProvider == null) {
            System.err.println("ResourceProvider is null! Minecraft client not fully initialized?");
            return;
        }

        if (!nativesDir.exists() && !nativesDir.mkdirs()) {
            System.err.println("Failed to create natives directory: " + nativesDir.getAbsolutePath());
            return;
        }

        String extension = switch (Util.getPlatform()) {
            case WINDOWS -> ".dll";
            case LINUX -> ".so";
            case OSX -> ".dylib";
            default -> {
                System.err.println("Unsupported platform: " + Util.getPlatform());
                yield "";
            }
        };

        if (extension.isEmpty())
            return;

        String libraryFileName = "libEffekseerNativeForJava" + extension;
        File targetFile = new File(nativesDir, libraryFileName);

        // Извлечение библиотеки из ресурсов
        if (!targetFile.exists()) {
            try {
                extractNativeLibrary(libraryFileName, targetFile, resourceProvider);
            } catch (IOException e) {
                System.err.println("Native extraction failed:");
                e.printStackTrace();
                return;
            }
        }

        // Загрузка библиотеки
        try {
            System.load(targetFile.getAbsolutePath());
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load native library:");
            e.printStackTrace();
            return;
        }
        if (!EffekseerBackendCore.InitializeWithOpenGL()) {
            System.err.println("Effekseer OpenGL backend failed to initialize");
            return;
        }

        effekseerManagerCore = new EffekseerManagerCore();
        if (!effekseerManagerCore.Initialize(8000)) {
            System.err.println("Effekseer manager failed to initialize");
        } else {
            System.out.println("Effekseer initialized successfully with 8000 instances");
        }
    }

    private void extractNativeLibrary(String fileName, File targetFile, ResourceProvider resourceProvider)
            throws IOException {
        Identifier location = Identifier.fromNamespaceAndPath("effekser", fileName);
        System.out.println("Looking for native resource at: " + location);

        try {
            Resource resource = resourceProvider.getResourceOrThrow(location);
            try (InputStream in = resource.open();
                    OutputStream out = Files.newOutputStream(targetFile.toPath())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to extract native library: " + location, e);
        }
    }
}