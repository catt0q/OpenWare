package base.client.feature.impl.player;

import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;

public class NoSlowBreak extends Module {

    public static BooleanSetting air = new BooleanSetting("In Air", true,()->true);
    public static BooleanSetting water = new BooleanSetting("In Water", true,()->true);
    public static BooleanSetting HC = new BooleanSetting("Hotbar Change", true,()->true);
    public NoSlowBreak() {
        super("NoSlowBreak", "Убирает задержку при ломании блока", Type.Player);
        addSettings(air, water,HC);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
}
