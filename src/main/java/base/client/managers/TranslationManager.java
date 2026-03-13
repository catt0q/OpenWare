package base.client.managers;

import base.client.Client;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TranslationManager {

    private static final Map<String, String> translations = new HashMap<>();
    private static String currentLanguage = "en";
    private static final Gson gson = new Gson();
    private static final Path LANGUAGE_FILE = FabricLoader.getInstance().getConfigDir().resolve("language.txt");

    public static void init() {
        loadLanguageFromFile();
        loadLanguage(currentLanguage);
    }

    /**
     * Loads the language setting from language.txt file.
     * Creates the file with default "en" if it doesn't exist.
     */
    private static void loadLanguageFromFile() {
        try {
            if (Files.exists(LANGUAGE_FILE)) {
                String content = Files.readString(LANGUAGE_FILE, StandardCharsets.UTF_8).trim().toLowerCase();
                if (content.equals("en") || content.equals("ru")) {
                    currentLanguage = content;
                    System.out.println("[TranslationManager] Loaded language from file: " + currentLanguage);
                } else {
                    // Invalid content, reset to default
                    currentLanguage = "en";
                    saveLanguageToFile();
                }
            } else {
                // File doesn't exist, create with default
                currentLanguage = "en";
                saveLanguageToFile();
                System.out.println("[TranslationManager] Created language.txt with default: en");
            }
        } catch (IOException e) {
            System.err.println("[TranslationManager] Failed to load language file: " + e.getMessage());
            currentLanguage = "en";
        }
    }

    /**
     * Saves the current language to language.txt file.
     */
    public static void saveLanguageToFile() {
        try {
            Files.writeString(LANGUAGE_FILE, currentLanguage, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[TranslationManager] Failed to save language file: " + e.getMessage());
        }
    }

    public static void loadLanguage(String langCode) {
        currentLanguage = langCode;
        translations.clear();

        String resourcePath = "/translations/" + langCode + ".json";

        try (InputStream is = TranslationManager.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[TranslationManager] Language file not found: " + resourcePath);
                // fallback to en if requested lang not found and not already en
                if (!langCode.equals("en")) {
                    loadLanguage("en");
                }
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> loaded = gson.fromJson(reader, type);

                if (loaded != null) {
                    translations.putAll(loaded);
                    System.out.println(
                            "[TranslationManager] Loaded " + translations.size() + " translations for " + langCode);
                }
            }
        } catch (IOException e) {
            System.err.println("[TranslationManager] Failed to load language " + langCode + ": " + e.getMessage());
        }
    }

    // get translated string by key, returns key if not found
    public static String get(String key) {
        return translations.getOrDefault(key, key);
    }

    // get translated string with formatted arguments
    public static String get(String key, Object... args) {
        String template = translations.getOrDefault(key, key);
        try {
            return String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }

    // check if translation exists
    public static boolean has(String key) {
        return translations.containsKey(key);
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setLanguage(String langCode) {
        if (!langCode.equals(currentLanguage)) {
            loadLanguage(langCode);
            saveLanguageToFile();
        }
    }

    // shorthand alias
    public static String t(String key) {
        return get(key);
    }

    public static String t(String key, Object... args) {
        return get(key, args);
    }
}
