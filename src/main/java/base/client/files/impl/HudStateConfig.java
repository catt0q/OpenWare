package base.client.files.impl;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.settings.Setting;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ColorSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.files.FileManager;
import com.google.gson.*;

import java.io.*;

/**
 * separate config file for modules with loadFromConfig=false
 * these modules (visuals/hud/client) have their state preserved across sessions
 * independently of the main config system
 */
public class HudStateConfig extends FileManager.CustomFile {

    public HudStateConfig(String name, boolean loadOnStart) {
        super(name, loadOnStart);
    }

    @Override
    public void loadFile() {
        try {
            File file = getFile();
            if (!file.exists())
                return;

            FileReader reader = new FileReader(file);
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(reader);
            reader.close();

            if (object == null || !object.has("Modules"))
                return;

            JsonObject modulesObject = object.getAsJsonObject("Modules");
            for (Module module : Client.instance.featureManager.getModuleList()) {
                // only load modules with loadFromConfig disabled
                if (module.isLoadFromConfig())
                    continue;

                JsonObject moduleObj = modulesObject.getAsJsonObject(module.getLabel());
                if (moduleObj == null)
                    continue;

                if (moduleObj.has("state")) {
                    module.setState(moduleObj.get("state").getAsBoolean());
                }
                if (moduleObj.has("keyIndex")) {
                    module.setBind(moduleObj.get("keyIndex").getAsInt());
                }

                // load settings
                JsonObject settingsObj = moduleObj.getAsJsonObject("Settings");
                if (settingsObj == null)
                    continue;

                for (Setting set : module.getSettings()) {
                    if (set == null || !settingsObj.has(set.getName()))
                        continue;

                    if (set instanceof BooleanSetting) {
                        ((BooleanSetting) set).setBoolValue(settingsObj.get(set.getName()).getAsBoolean());
                    } else if (set instanceof ModeSetting) {
                        ((ModeSetting) set).setListMode(settingsObj.get(set.getName()).getAsString());
                    } else if (set instanceof NumberSetting) {
                        ((NumberSetting) set).setValueNumber(settingsObj.get(set.getName()).getAsFloat());
                    } else if (set instanceof ColorSetting) {
                        ((ColorSetting) set).setColorValue(settingsObj.get(set.getName()).getAsInt());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveFile() {
        try {
            JsonObject object = new JsonObject();
            JsonObject modulesObject = new JsonObject();

            for (Module module : Client.instance.featureManager.getModuleList()) {
                // only save modules with loadFromConfig disabled
                if (module.isLoadFromConfig())
                    continue;

                JsonObject moduleObj = new JsonObject();
                moduleObj.addProperty("state", module.getState());
                moduleObj.addProperty("keyIndex", module.getBind());

                JsonObject settingsObj = new JsonObject();
                for (Setting set : module.getSettings()) {
                    if (set instanceof BooleanSetting) {
                        settingsObj.addProperty(set.getName(), ((BooleanSetting) set).isEnabled());
                    } else if (set instanceof ModeSetting) {
                        settingsObj.addProperty(set.getName(), ((ModeSetting) set).getCurrentMode());
                    } else if (set instanceof NumberSetting) {
                        settingsObj.addProperty(set.getName(), ((NumberSetting) set).getValue());
                    } else if (set instanceof ColorSetting) {
                        settingsObj.addProperty(set.getName(), ((ColorSetting) set).getColorValue());
                    }
                }
                moduleObj.add("Settings", settingsObj);

                modulesObject.add(module.getLabel(), moduleObj);
            }

            object.add("Modules", modulesObject);

            String json = new GsonBuilder().setPrettyPrinting().create().toJson(object);
            FileWriter writer = new FileWriter(getFile());
            writer.write(json);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
