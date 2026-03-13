package base.client.effekseer.installer;

import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerTextureType;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Loader {
    public static EffekseerEffectCore loadEffect(String effectName, float magnification, ResourceProvider resourceProvider) {
        // Правильный путь к эффекту
    /*    Identifier effectLocation = Identifier.fromNamespaceAndPath("effekser", "particles/" + effectName);
        EffekseerEffectCore effectCore = new EffekseerEffectCore();

        try {
            Resource resource = resourceProvider.getResourceOrThrow(effectLocation);
            try (InputStream is = resource.open()) {
                byte[] bytes = is.readAllBytes();
                if (!effectCore.Load(bytes, bytes.length, magnification)) {
                    System.out.println("Failed to load effect: " + effectLocation);
                    return null;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading effect file: " + e.getMessage());
            return null;
        }

        // Загрузка текстур
        EffekseerTextureType[] textureTypes = {
                EffekseerTextureType.Color,
                EffekseerTextureType.Normal,
                EffekseerTextureType.Distortion
        };

        for (EffekseerTextureType type : textureTypes) {
            final EffekseerTextureType textureType = type;
            for (int idx = 0; idx < effectCore.GetTextureCount(textureType); idx++) {
                final int textureIndex = idx;
                String texturePath = effectCore.GetTexturePath(textureIndex, textureType);
                Identifier textureLocation = resolveRelativePath(effectLocation, texturePath);
                loadResourceBytes(textureLocation, resourceProvider).ifPresent(bytes ->
                        effectCore.LoadTexture(bytes, bytes.length, textureIndex, textureType)
                );
            }
        }

        // Загрузка моделей
        for (int idx = 0; idx < effectCore.GetModelCount(); idx++) {
            final int modelIndex = idx;
            String modelPath = effectCore.GetModelPath(modelIndex);
            Identifier modelLocation = resolveRelativePath(effectLocation, modelPath);
            loadResourceBytes(modelLocation, resourceProvider).ifPresent(bytes ->
                    effectCore.LoadModel(bytes, bytes.length, modelIndex)
            );
        }

        // Загрузка материалов
        for (int idx = 0; idx < effectCore.GetMaterialCount(); idx++) {
            final int materialIndex = idx;
            String materialPath = effectCore.GetMaterialPath(materialIndex);
            Identifier materialLocation = resolveRelativePath(effectLocation, materialPath);
            loadResourceBytes(materialLocation, resourceProvider).ifPresent(bytes ->
                    effectCore.LoadMaterial(bytes, bytes.length, materialIndex)
            );
        }
        return effectCore;*/


        Identifier effectLocation = Identifier.fromNamespaceAndPath("effekser", "particles/" + effectName);

        try {
            Resource resource = resourceProvider.getResourceOrThrow(effectLocation);
            try (InputStream is = resource.open()) {
                byte[] bytes = is.readAllBytes();
                EffekseerEffectCore effectCore = new EffekseerEffectCore();

                if (!effectCore.Load(bytes, bytes.length, magnification)) {
                    LOGGER.error("Failed to load effect: {}", effectLocation);
                    return null;
                }




            // Загрузка текстур
            EffekseerTextureType[] textureTypes = {
                    EffekseerTextureType.Color,
                    EffekseerTextureType.Normal,
                    EffekseerTextureType.Distortion
            };

            for (EffekseerTextureType type : textureTypes) {
                final EffekseerTextureType textureType = type;
                for (int idx = 0; idx < effectCore.GetTextureCount(textureType); idx++) {
                    final int textureIndex = idx;
                    String texturePath = effectCore.GetTexturePath(textureIndex, textureType);
                    Identifier textureLocation = resolveRelativePath(effectLocation, texturePath);
                    loadResourceBytes(textureLocation, resourceProvider).ifPresent(bytes1 ->
                            effectCore.LoadTexture(bytes1, bytes1.length, textureIndex, textureType)
                    );
                }
            }

            // Загрузка моделей
            for (int idx = 0; idx < effectCore.GetModelCount(); idx++) {
                final int modelIndex = idx;
                String modelPath = effectCore.GetModelPath(modelIndex);
                Identifier modelLocation = resolveRelativePath(effectLocation, modelPath);
                loadResourceBytes(modelLocation, resourceProvider).ifPresent(bytes1 ->
                        effectCore.LoadModel(bytes1, bytes1.length, modelIndex)
                );
            }

            // Загрузка материалов
            for (int idx = 0; idx < effectCore.GetMaterialCount(); idx++) {
                final int materialIndex = idx;
                String materialPath = effectCore.GetMaterialPath(materialIndex);
                Identifier materialLocation = resolveRelativePath(effectLocation, materialPath);
                loadResourceBytes(materialLocation, resourceProvider).ifPresent(bytes1 ->
                        effectCore.LoadMaterial(bytes1, bytes1.length, materialIndex)
                );
            }


                // Загрузка зависимостей...
                return effectCore;
            }
        } catch (Exception e) {
            LOGGER.error("Error loading effect {}: {}", effectLocation, e.getMessage());
            return null;
        }
    }

    private static Optional<byte[]> loadResourceBytes(Identifier location, ResourceProvider resourceProvider) {
        try {
            Resource resource = resourceProvider.getResourceOrThrow(location);
            try (InputStream is = resource.open()) {
                return Optional.of(is.readAllBytes());
            }
        } catch (IOException e) {
            System.out.println("Error loading resource " + location + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private static Identifier resolveRelativePath(Identifier baseLocation, String relativePath) {
        String namespace = baseLocation.getNamespace();

        // Получаем родительский путь основного ресурса
        String basePath = baseLocation.getPath();
        int lastSlash = basePath.lastIndexOf('/');
        String parentPath = lastSlash > 0 ? basePath.substring(0, lastSlash) : "";

        // Собираем новый путь
        String newPath = parentPath.isEmpty()
                ? relativePath
                : parentPath + "/" + relativePath;

        return Identifier.fromNamespaceAndPath(namespace, newPath);
    }
}