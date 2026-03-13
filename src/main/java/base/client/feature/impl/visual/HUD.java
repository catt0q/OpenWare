package base.client.feature.impl.visual;

import base.client.event.EventTarget;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.visual.hud.HudElement;
import base.client.feature.settings.impl.*;

import java.awt.*;

public class HUD extends Module {
    // Global color settings removed: client uses locked two-color gradient.

    // ArrayList settings
    public static BooleanSetting arrayListGlow = new BooleanSetting("Glow", false, () -> true);
    public static BooleanSetting arrayListShadow = new BooleanSetting("Shadow", false, () -> true);
    public static BooleanSetting arrayListSuffix = new BooleanSetting("Suffix", true, () -> true);

    // Font settings removed: client uses Minecraft font only.

    // Main Menu settings

    public HUD() {
        super("HUD", "Отображает худ чита", Type.Visuals);
        this.addSettings(arrayListSuffix);
    }

    @EventTarget
    public void onRenderGui(EventRenderGui e) {
        for (HudElement hudElement : HudElement.getElements()) {
            hudElement.draw(e.dc(), mc.mouseHandler.xpos(), mc.mouseHandler.ypos());
        }
    }
}
