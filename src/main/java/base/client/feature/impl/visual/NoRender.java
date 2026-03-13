package base.client.feature.impl.visual;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class NoRender extends Module {

    public static BooleanSetting hurt,pumpkin,blindness,nausea,fire,water,lava;


    public NoRender() {
        super("NoRender", "Убирает опредленные элементы рендера в игре", Type.Visuals);
        hurt = new BooleanSetting("HurtCamera", true, () -> true);
        pumpkin = new BooleanSetting("Pumpkin", true, () -> true);
        blindness = new BooleanSetting("Blindness Effect", true, () -> true);
        nausea = new BooleanSetting("Nausea Effect", true, () -> true);
        fire = new BooleanSetting("Fire HUD", true, () -> true);
        water = new BooleanSetting("UnderWater Vision", true, () -> true);
        lava = new BooleanSetting("UnderLava Vision", true, () -> true);
        addSettings(hurt,pumpkin,blindness,nausea,fire,water,lava);
    }

    @EventTarget
    public void update(EventPreMotion eventMove) {
        if (mc.player!=null) {
            for(MobEffectInstance mobeffectinstance : mc.player.getActiveEffects()) {

                if(mobeffectinstance.getEffect().equals(MobEffects.BLINDNESS) && blindness.isEnabled()) {
                   mc.player.removeEffect(MobEffects.BLINDNESS);
                }
                if(mobeffectinstance.getEffect().equals(MobEffects.NAUSEA) && nausea.isEnabled()) {
                    mc.player.removeEffect(MobEffects.NAUSEA);
                }
            }
        }
    }

}
