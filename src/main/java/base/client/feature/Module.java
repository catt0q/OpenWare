package base.client.feature;

import base.client.Client;
import base.client.event.EventManager;
import base.client.feature.impl.Type;
import base.client.feature.impl.client.ClientSounds;
import base.client.feature.impl.hud.Notifications;
import base.client.feature.settings.Configurable;
import base.client.feature.settings.Setting;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ColorSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.Helper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import com.google.gson.JsonObject;

public class Module extends Configurable implements Helper {

    public boolean enablable = true;

    public Type type;
    public boolean state;
    // public ScreenHelper screenHelper = new ScreenHelper(0, 0);
    private String label, suffix;
    private int bind;
    private int defbind;
    private String desc;

    private BooleanSetting hidemodule = new BooleanSetting("Hide Module", false, () -> true);
    private BooleanSetting hidesuffix = new BooleanSetting("Hide Suffix", false, () -> true);
    private BooleanSetting triggerclientsound = new BooleanSetting("Trigger ClientSound", true, () -> true);
    private BooleanSetting triggernotifications = new BooleanSetting("Trigger Notifications", true, () -> true);
    private BooleanSetting loadFromConfig = new BooleanSetting("Load from Config", true, () -> true);

    public Module() {
    }

    public Module(String label, String desc, Type type) {
        this.label = label;
        this.desc = desc;
        this.type = type;
        this.bind = 0;
        this.defbind = 0;

        // set loadFromConfig default based on category
        boolean defaultLoadFromConfig = !(type == Type.Visuals || type == Type.Hud || type == Type.Client);
        this.loadFromConfig = new BooleanSetting("Load from Config", defaultLoadFromConfig, () -> true);

        this.addSettings(hidemodule);
        this.addSettings(hidesuffix);
        this.addSettings(triggerclientsound);
        this.addSettings(triggernotifications);
        this.addSettings(loadFromConfig);

        this.state = false;
    }

    public Module(String label, String desc, Type type, int startbind) {
        this.label = label;
        this.desc = desc;
        this.type = type;
        this.bind = startbind;
        this.defbind = startbind;

        // set loadFromConfig default based on category
        boolean defaultLoadFromConfig = !(type == Type.Visuals || type == Type.Hud || type == Type.Client);
        this.loadFromConfig = new BooleanSetting("Load from Config", defaultLoadFromConfig, () -> true);

        this.addSettings(hidemodule);
        this.addSettings(hidesuffix);
        this.addSettings(triggerclientsound);
        this.addSettings(triggernotifications);
        this.addSettings(loadFromConfig);
        this.enablable = true;
        this.state = false;
    }

    public void clear() {
        for (Setting set : this.getSettings()) {
            if (this.getSettings() != null) {
                if (set instanceof BooleanSetting) {
                    BooleanSetting bs = (BooleanSetting) set;
                    bs.setBoolValue(bs.isDefstate());
                } else if (set instanceof ModeSetting) {
                    ModeSetting ms = (ModeSetting) set;
                    ms.setListMode(ms.getDefMode());
                } else if (set instanceof NumberSetting) {
                    NumberSetting ns = (NumberSetting) set;
                    ns.setValueNumber(ns.getDefvalue());
                } else if (set instanceof ColorSetting) {
                    ColorSetting cs = (ColorSetting) set;
                    cs.setColorValue(cs.getDefcolor());
                }
            }

        }
        this.setState(false);
        this.setBind(this.defbind);
        this.setVisible(true);
        this.setSuffixVisible(true);
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("state", getState());
        object.addProperty("keyIndex", getBind());
        JsonObject propertiesObject = new JsonObject();
        for (Setting set : this.getSettings()) {
            if (this.getSettings() != null) {
                if (set instanceof BooleanSetting) {
                    propertiesObject.addProperty(set.getName(), ((BooleanSetting) set).isEnabled());
                } else if (set instanceof ModeSetting) {
                    propertiesObject.addProperty(set.getName(), ((ModeSetting) set).getCurrentMode());
                } else if (set instanceof NumberSetting) {
                    propertiesObject.addProperty(set.getName(), ((NumberSetting) set).getValue());
                } else if (set instanceof ColorSetting) {
                    propertiesObject.addProperty(set.getName(), ((ColorSetting) set).getColorValue());
                }
            }
            object.add("Settings", propertiesObject);
        }
        return object;
    }

    public void load(JsonObject object) {
        if (object != null) {
            // only load state if loadFromConfig is enabled
            if (object.has("state") && loadFromConfig.isEnabled()) {
                this.setState(object.get("state").getAsBoolean());
            }
            if (object.has("keyIndex")) {
                this.setBind(object.get("keyIndex").getAsInt());
            }
            for (Setting set : getSettings()) {
                JsonObject propertiesObject = object.getAsJsonObject("Settings");
                if (set == null)
                    continue;
                if (propertiesObject == null)
                    continue;
                if (!propertiesObject.has(set.getName()))
                    continue;
                if (set instanceof BooleanSetting) {
                    ((BooleanSetting) set).setBoolValue(propertiesObject.get(set.getName()).getAsBoolean());
                } else if (set instanceof ModeSetting) {
                    ((ModeSetting) set).setListMode(propertiesObject.get(set.getName()).getAsString());
                } else if (set instanceof NumberSetting) {
                    ((NumberSetting) set).setValueNumber(propertiesObject.get(set.getName()).getAsFloat());
                } else if (set instanceof ColorSetting) {
                    ((ColorSetting) set).setColorValue(propertiesObject.get(set.getName()).getAsInt());
                }
            }
        }
    }

    public boolean isLoadFromConfig() {
        return loadFromConfig.isEnabled();
    }

    public Setting getSettingByLabelNoSpace(String name) {
        String nname = name.replaceAll(" ", "").toLowerCase();
        if (nname.length() < 1) {
            return null;
        }
        for (Setting setting : this.getSettings()) {
            if (setting == null) {
                continue;
            }
            if (setting.getName().length() < 1) {
                continue;
            }
            if (setting.getName().replaceAll(" ", "").toLowerCase().equals(nname)) {
                return setting;
            }
        }
        return null;
    }

    public Setting getSettingByLabel(String name) {
        for (Setting setting : this.getSettings()) {
            if (setting.getName().toLowerCase().equals(name.toLowerCase())) {
                return setting;
            }
        }
        return null;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix; // why the fuck is this even here ig its called and im too lazy so whatever
        // this.suffix = getLabel() + " - " + suffix;
    }

    public boolean isVisible() {
        return !hidemodule.isEnabled();
    }

    public void setVisible(boolean visible) {
        this.hidemodule.setBoolValue(false);
    }

    public boolean isHidden() {
        return hidemodule.isEnabled();
    }

    public void setHidden(boolean visible) {
        this.hidemodule.setBoolValue(true);
    }

    public boolean isSuffixVisible() {
        return !hidesuffix.isEnabled();
    }

    public void setSuffixVisible(boolean visible) {
        this.hidesuffix.setBoolValue(false);
    }

    public boolean isSuffixHidden() {
        return hidesuffix.isEnabled();
    }

    public void setSuffixHidden(boolean visible) {
        this.hidesuffix.setBoolValue(true);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getBind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }

    public Type getType() {
        return type;
    }

    public void onEnable() {
        // @obf-only start
        base.client.auth.TamperResponse.checkA();
        if (base.client.auth.TamperResponse.shouldDisable()) {
            this.state = false;
            return;
        }
        // @obf-only end

        EventManager.register(this);

        if (Client.instance.featureManager.getModuleByClass(ClientSounds.class).getState()
                && this.triggerclientsound.isEnabled()) {
            Client.instance.soundManager.playEnable();
        }
        if (!(getLabel().contains("ClickGui") || getLabel().contains("Client Font")
                || getLabel().contains("Notifications")) && Notifications.state.isEnabled()
                && this.triggernotifications.isEnabled()) {
            NotificationManager.publicity(getLabel(), "was enabled!", 2, NotificationType.SUCCESS);
        }
    }

    public void onDisable() {

        EventManager.unregister(this);

        if (Client.instance.featureManager.getModuleByClass(ClientSounds.class).getState()
                && this.triggerclientsound.isEnabled()) {
            Client.instance.soundManager.playDisable();
        }

        if (!(getLabel().contains("ClickGui") || getLabel().contains("Client Font")
                || getLabel().contains("Notifications")) && Notifications.state.isEnabled()
                && this.triggernotifications.isEnabled()) {
            NotificationManager.publicity(getLabel(), "was disabled!", 2, NotificationType.ERROR);
        }
    }

    public void toggle() {
        // @obf-only start
        base.client.auth.TamperResponse.checkB();
        if (base.client.auth.TamperResponse.shouldDisable()) {
            return;
        }
        // @obf-only end

        this.state = !this.state;

        if (state) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        if (state) {
            EventManager.register(this);
        } else {
            EventManager.unregister(this);
        }
        this.state = state;
    }
}
