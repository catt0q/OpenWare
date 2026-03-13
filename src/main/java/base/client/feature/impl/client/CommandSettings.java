package base.client.feature.impl.client;

import base.client.feature.impl.LockedModule;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;

public class CommandSettings extends LockedModule {

    public static BooleanSetting FullCheck = new BooleanSetting("FullCheck","Покажет всю инфу в .check игнорируя видимость",
            false, () -> true);



    public CommandSettings() {
        super("CommandSettings", "Настройки команд", Type.Client);
        this.addSettings(FullCheck);
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

}
