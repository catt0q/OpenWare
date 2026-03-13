package base.client.feature.impl.movement;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventCollideEntity;
import base.client.event.events.impl.motion.EventPushOutOfBlocks;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;

public class AntiPush extends Module {
    public static BooleanSetting entities;
    public static BooleanSetting blocks;

    public AntiPush() {
        super("AntiPush", "Убирает отталкивание от игроков, блоков", Type.Movement);
        entities = new BooleanSetting("Entity", true, () -> true);
        blocks = new BooleanSetting("Blocks", true, () -> true);
        addSettings(entities, blocks);
    }

    @Override
    public void onDisable() {

        super.onDisable();
    }

    @Override
    public void onEnable() {


        super.onEnable();
    }

    @EventTarget
    public void onCollide(EventCollideEntity event) {
        if(!entities.isEnabled()) return;
        event.cancel();
    }
    @EventTarget
    public void onPush(EventPushOutOfBlocks event) {
        if(!blocks.isEnabled()) return;
        event.cancel();
    }

}
