package base.client.feature.impl.visual;

import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.helpers.utils.TimerUtil;
public class ClientRotations extends Module {
    public static BooleanSetting visualYaw = new BooleanSetting("Visual Yaw", "Отображение визуальной ротации", true, () -> true);
    public static BooleanSetting bodyLockYaw = new BooleanSetting("Body Lock", "YES", false, () -> visualYaw.isEnabled());

    public static BooleanSetting visualPitch = new BooleanSetting("Visual Pitch", "Отображение визуальной ротации", true, () -> true);
    public ClientRotations() {
        super("ClientRotations", "Ротация визуалайз", Type.Misc);
        this.addSettings(visualYaw,bodyLockYaw,visualPitch);
    }

    public void onEnable() {
        super.onEnable();
    }
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }



}
