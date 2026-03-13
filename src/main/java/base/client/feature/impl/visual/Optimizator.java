package base.client.feature.impl.visual;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.helpers.utils.TimerUtil;
public class Optimizator extends Module {
    public static BooleanSetting GlowCancel = new BooleanSetting("Glow Cancel", true, () -> true);
public Optimizator() {
 super("Optimizator", "Оптимизация", Type.Misc);
    this.addSettings(GlowCancel);
}

public void onEnable() {
     super.onEnable();
}
public void onDisable() {
    super.onDisable();
}






}