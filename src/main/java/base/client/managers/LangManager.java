package base.client.managers;

import base.client.Client;
import base.client.feature.Module;
import base.client.lang.Lang;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;

public final class LangManager extends Manager<Lang> {

    public static final File langDirectory = new File(Client.instance.name, "langs");
    private static final ArrayList<Lang> loadedLangs = new ArrayList<>();

    public LangManager() {
        setContents(loadLangs());
        langDirectory.mkdirs();
    }

    private static ArrayList<Lang> loadLangs() {
        File[] files = langDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (FilenameUtils.getExtension(file.getName()).equals("json"))
                    loadedLangs.add(new Lang(FilenameUtils.removeExtension(file.getName())));
            }
        }
        return loadedLangs;
    }

    public static ArrayList<Lang> getLoadedLangs() {
        return loadedLangs;
    }

    public void load() {
        if (!langDirectory.exists()) {
            langDirectory.mkdirs();
        }
        if (langDirectory != null) {
            File[] files = langDirectory.listFiles(f -> !f.isDirectory() && FilenameUtils.getExtension(f.getName()).equals("json"));
            for (File f : files) {
                Lang lang = new Lang(FilenameUtils.removeExtension(f.getName()).replace(" ", ""));
                loadedLangs.add(lang);
            }
        }
    }

    public void create(String langName) {
        if (!langDirectory.exists()) {
            langDirectory.mkdirs();
        }
        if (langDirectory != null) {
            File[] files = langDirectory.listFiles(f -> !f.isDirectory() && FilenameUtils.getExtension(f.getName()).equals("json"));
            for (File f : files) {
                Lang lang = new Lang(FilenameUtils.removeExtension(f.getName()).replace(" ", ""));
                loadedLangs.add(lang);
            }
        }

        for (Module module : Client.instance.featureManager.getModuleList()) {
            module.clear();
        }
    }

    public boolean loadLang(String langName) {
        if (langName == null)
            return false;
        Lang lang = findLang(langName);
        if (lang == null)
            return false;
        try {
            FileReader reader = new FileReader(lang.getFile());
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(reader);
            lang.load(object);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public boolean saveLang(String langName) {
        if (langName == null)
            return false;
        Lang lang;
        if ((lang = findLang(langName)) == null) {
            Lang newLang = (lang = new Lang(langName));
            getContents().add(newLang);
        }

        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(lang.save());
        try {
            FileWriter writer = new FileWriter(lang.getFile());
            writer.write(contentPrettyPrint);
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Lang findLang(String langName) {
        if (langName == null) return null;
        for (Lang lang : getContents()) {
            if (lang.getName().equalsIgnoreCase(langName))
                return lang;
        }

        if (new File(langDirectory, langName + ".json").exists())
            return new Lang(langName);

        return null;
    }

    public boolean deleteLang(String langName) {
        if (langName == null)
            return false;
        Lang lang;
        if ((lang = findLang(langName)) != null) {
            final File f = lang.getFile();
            getContents().remove(lang);
            return f.exists() && f.delete();
        }
        return false;
    }
}