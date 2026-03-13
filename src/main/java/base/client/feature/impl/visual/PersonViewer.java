package base.client.feature.impl.visual;

import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.NumberSetting;
import net.minecraft.client.Minecraft;

public class PersonViewer extends Module {
    public static NumberSetting viewerYaw;
    public static NumberSetting fovModifier;
    public static NumberSetting viewerPitch;

    public PersonViewer() {
        super("PersonViewer", "Повозяляет изменять положение камеры второго и третьего лица", Type.Visuals);
        fovModifier = new NumberSetting("Distance", 4, 1, 50, 1, () -> true);
        viewerYaw = new NumberSetting("Yaw", 10, -50, 50, 5, () -> true);
        viewerPitch = new NumberSetting("Pitch", 10, -50, 50, 5, () -> true);
        addSettings(fovModifier, viewerYaw, viewerPitch);
    }

    public void onEnable() {
        super.onEnable();
    }
    public void onDisable() {
        super.onDisable();
    }

}
