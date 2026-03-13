package base.client.lang;


import base.client.Client;
import base.client.managers.LangManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.Map;

public class Lang implements LangUpdater {

    private final String name;
    private final File file;


    public Lang(String name) {
        this.name = name;
        this.file = new File(LangManager.langDirectory, name + ".json");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
            }
        }
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    @Override
    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();

        for (Map.Entry<String, String> entry : Client.langmap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            jsonObject.addProperty(key, value);
        }


        return jsonObject;
    }

    @Override
    public void load(JsonObject object) {
        if (object == null) {
            throw new IllegalArgumentException("JsonObject cannot be null");
        }

        Client.langmap.clear();

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                String value = jsonElement.getAsString();
                Client.langmap.put(key, value);
            } else {
                throw new IllegalArgumentException("Value for key '" + key + "' is not a string");
            }
        }





    }
}
