package base.client.feature.impl.player;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.impl.Type;
import net.minecraft.client.gui.screens.DeathScreen;
import base.client.feature.Module;
public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Авто возрождение", Type.Player);
    }



    @Override
    public void onEnable() {
   super.onEnable();
    }
    @Override
    public void onDisable() {
     super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {
        if (mc.player.getHealth() < 0 || !mc.player.isAlive() || mc.screen instanceof DeathScreen) {
            mc.player.respawn();
            mc.setScreen(null);

        }
    }

}
