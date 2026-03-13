package base.client.feature.impl.visual;

import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerManagerCore;
import base.client.Client;
import base.client.effekseer.api.EffekseerManager;
import base.client.effekseer.installer.Loader;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.event.EventTarget;
import base.client.feature.impl.Type;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.world.phys.Vec3;

public class KillEffect extends Module {
    private EffekseerManager manager;
    public KillEffect() {
        super("KillEffect", "Убивать", Type.Visuals);
    }

    EffekseerEffectCore effekseerEffectCore = null;
    private int effectHandle = -1;

    @EventTarget
    public void onUpdate(EventPreMotion event) {
        this.manager = new EffekseerManager(Client.instance.loadNatives.getEffekseerManagerCore());
        if (effectHandle == -1) {
            // Пересоздаем эффект если он не существует
            EffekseerEffectCore effect = Loader.loadEffect("magic.efkefc", 1f, mc.getResourceManager());
            if (effect != null) {
                effectHandle = manager.getImpl().Play(effect);
            }
        }

        if (effectHandle != -1) {
            // Обновляем позицию эффекта каждый тик
            Vec3 pos = mc.player.position();
            manager.getImpl().SetEffectPosition(effectHandle,
                    (float) pos.x,
                    (float) pos.y + 1f, // Немного выше игрока
                    (float) pos.z
            );
        }
    }

    @Override
    public void onDisable() {
        if (effectHandle != -1) {
            manager.getImpl().Stop(effectHandle);
            effectHandle = -1;
        }
    }
    @Override
    public void onEnable() {

     /*   if(effect.is("Ember")) {
            effekseerEffectCore = Loader.loadEffect("Ember.efkefc", 0.3f);
        } else if(effect.is("Sacred")) {
            effekseerEffectCore = Loader.loadEffect("Sacred.efkefc", 0.3f);
        }
        lastEffect = effect.get();*/

        effekseerEffectCore = Loader.loadEffect("magic.efkefc", 100f,mc.getResourceManager());


        EffekseerManagerCore effekseerManagerCore = Client.instance.loadNatives.getEffekseerManagerCore();
        //   effekseerManagerCore.Stop(effectHandle);


        effectHandle = effekseerManagerCore.Play(effekseerEffectCore);
        effekseerManagerCore.SetEffectPosition(effectHandle, (float) mc.player.getX(), (float) mc.player.getY(), (float) mc.player.getZ());


        super.onEnable();
    }



}
