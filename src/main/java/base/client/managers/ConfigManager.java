package base.client.managers;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.settings.config.Config;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;

public final class ConfigManager extends Manager<Config> {

    public static final File configDirectory = new File(Client.instance.name, "configs");
    private static final ArrayList<Config> loadedConfigs = new ArrayList<>();

    public ConfigManager() {
        setContents(loadConfigs());
        configDirectory.mkdirs();
    }

    private static ArrayList<Config> loadConfigs() {
        File[] files = configDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (FilenameUtils.getExtension(file.getName()).equals("json"))
                    loadedConfigs.add(new Config(FilenameUtils.removeExtension(file.getName())));
            }
        }
        return loadedConfigs;
    }

    public static ArrayList<Config> getLoadedConfigs() {
        return loadedConfigs;
    }

    public void load() {
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
        }
        if (configDirectory != null) {
            File[] files = configDirectory
                    .listFiles(f -> !f.isDirectory() && FilenameUtils.getExtension(f.getName()).equals("json"));
            for (File f : files) {
                Config config = new Config(FilenameUtils.removeExtension(f.getName()).replace(" ", ""));
                loadedConfigs.add(config);
            }
        }
    }

    public void create(String configName) {
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
        }
        if (configDirectory != null) {
            File[] files = configDirectory
                    .listFiles(f -> !f.isDirectory() && FilenameUtils.getExtension(f.getName()).equals("json"));
            for (File f : files) {
                Config config = new Config(FilenameUtils.removeExtension(f.getName()).replace(" ", ""));
                loadedConfigs.add(config);
            }
        }

        for (Module module : Client.instance.featureManager.getModuleList()) {
            module.clear();
        }
    }

    public boolean loadConfig(String configName) {
        if (configName == null)
            return false;
        Config config = findConfig(configName);
        if (config == null)
            return false;
        try {
            FileReader reader = new FileReader(config.getFile());
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(reader);
            config.load(object);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public boolean saveConfig(String configName) {
        if (configName == null)
            return false;
        Config config;
        if ((config = findConfig(configName)) == null) {
            Config newConfig = (config = new Config(configName));
            getContents().add(newConfig);
        }

        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(config.save());
        try {
            FileWriter writer = new FileWriter(config.getFile());
            writer.write(contentPrettyPrint);
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Config findConfig(String configName) {
        if (configName == null)
            return null;
        for (Config config : getContents()) {
            if (config.getName().equalsIgnoreCase(configName))
                return config;
        }

        if (new File(configDirectory, configName + ".json").exists())
            return new Config(configName);

        return null;
    }

    public boolean deleteConfig(String configName) {
        if (configName == null)
            return false;
        Config config;
        if ((config = findConfig(configName)) != null) {
            final File f = config.getFile();
            getContents().remove(config);
            return f.exists() && f.delete();
        }
        return false;
    }

    // export config to JSON string for cloud sync
    public String exportConfigAsJson(String configName) {
        if (configName == null)
            return null;
        Config config = findConfig(configName);
        if (config == null) {
            // create new config with current settings
            config = new Config(configName);
        }
        try {
            JsonObject obj = config.save();
            return new GsonBuilder().create().toJson(obj);
        } catch (Exception e) {
            return null;
        }
    }

    // import config from JSON string (from cloud)
    public boolean importConfigFromJson(String configName, String jsonData) {
        if (configName == null || jsonData == null)
            return false;
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(jsonData);

            Config config = findConfig(configName);
            if (config == null) {
                config = new Config(configName);
                getContents().add(config);
            }

            config.load(object);

            // also save locally
            saveConfig(configName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}