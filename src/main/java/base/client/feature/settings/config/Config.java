package base.client.feature.settings.config;

import base.client.Client;
import base.client.feature.Module;
import base.client.helpers.utils.KeyUtil;
import base.client.macro.Macro;
import base.client.managers.ConfigManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.Map;
import java.util.Set;

public final class Config implements ConfigUpdater {

    private final String name;
    private final File file;

    public Config(String name) {
        this.name = name;
        this.file = new File(ConfigManager.configDirectory, name + ".json");

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
        JsonObject modulesObject = new JsonObject();
        JsonObject panelObject = new JsonObject();
        JsonObject macroObject = new JsonObject();

        for (Module module : Client.instance.featureManager.getModuleList()) {
            modulesObject.add(module.getLabel(), module.save());
        }
        for (Macro macro : Client.instance.macroManager.macros) {
            if (macro != null) {
                macroObject.addProperty(KeyUtil.KeyToString(macro.getKey()), macro.getValue());

            }

        }
        jsonObject.add("Modules", modulesObject);
        jsonObject.add("Keys", macroObject);
        return jsonObject;
    }

    @Override
    public void load(JsonObject object) {
        if (object.has("Modules")) {
            JsonObject modulesObject = object.getAsJsonObject("Modules");
            for (Module module : Client.instance.featureManager.getModuleList()) {
                // only reset state if loadFromConfig is enabled for this module
                if (module.isLoadFromConfig()) {
                    module.setState(false);
                }
                module.load(modulesObject.getAsJsonObject(module.getLabel()));
            }

        }
        if (object.has("Keys")) {
            JsonObject macrosObject = object.getAsJsonObject("Keys");
            Set<Map.Entry<String, JsonElement>> entries = macrosObject.entrySet();
            Client.instance.macroManager.macros.clear();
            for (Map.Entry<String, JsonElement> macro : entries) {
                Client.instance.macroManager.macros
                        .add(new Macro(KeyUtil.StringToKey(macro.getKey()).getValue(), macro.getValue().getAsString()));

            }
        }

    }
}