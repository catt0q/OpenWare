package base.client.feature.impl.minigames;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.AntiBot;
import base.client.feature.impl.combat.MoreKB;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AntiFireball extends Module {

    boolean state=false;
    float prevyaw=0;
    float prevpitch=0;

      TimerHelper timerr= new TimerHelper();

      NumberSetting Distance;
      NumberSetting Delay;
      BooleanSetting ReverseKB;
      NumberSetting ReversePitch;

    public AntiFireball() {
        super("AntiFireball", "Отталкивает фаерболы", Type.Minigames);
        this.Distance = new NumberSetting("Distance" , 5, 0, 10, 0.1F,()->true);
        this.Delay = new NumberSetting("Delay" , 100, 0, 1000, 10F,()->true);
        this.ReverseKB = new BooleanSetting("Reverse", false,() -> (!Client.instance.featureManager.getModuleByClass(MoreKB.class).getState()
                || !MoreKB.ReverseKB.isEnabled()));
        this.ReversePitch = new NumberSetting("Reverse Pitch", 90.0F, -90F, 90.0F, 1.0F,()->ReverseKB.isVisible() && ReverseKB.isEnabled());

        this.addSettings(Distance,Delay,ReverseKB,ReversePitch);
    }
    @Override
    public void onEnable() {
        state=false;
        prevyaw=0;
        prevpitch=0;
        timerr.reset();
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
    @EventTarget
    public void onUpdate(EventPreMotion event) {

        if(timerr.hasTimeElapsed((long) Delay.getValue(),true)) {



            List<Entity> targets = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false).filter(LivingEntity.class::isInstance).collect(Collectors.toList());
            targets = targets.stream().filter(entity -> entity != null &&
                    entity instanceof Fireball &&
                    EntityUtil.getMinDistanceToEntity(mc.player,entity)<Distance.getValue()

            ).collect(Collectors.toList());


            targets.sort(Comparator.comparingDouble(entity -> EntityUtil.getMinDistanceToEntity(mc.player,entity)) );

            for(int qqi=0;qqi<targets.size();qqi++) {
                mc.gameMode.attack(mc.player, targets.get(qqi));
            }
            if(ReverseKB.isEnabled()) {
                PacketHelper.Values pc=Client.instance.packet;
                state=true;
                prevyaw=(float) (pc.LastYaw);  prevpitch=(float) (pc.LastPitch);
                ServerboundMovePlayerPacket duplc06=new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY,pc.LastPosZ,pc.LastYaw, ReversePitch.getValue(), pc.LastGround,mc.player.horizontalCollision);
              pc.sendPacket(duplc06,10);
            }

        }
    }

    @EventTarget
    public void onPostUpdate(EventPostMotion event) {
        if(state) {
            PacketHelper.Values pc=Client.instance.packet;
            ServerboundMovePlayerPacket duplc06=new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, prevyaw, prevpitch,pc.LastYaw, ReversePitch.getValue(), pc.LastGround,mc.player.horizontalCollision);
            pc.sendPacket(duplc06,10);
            state=false;

        }
    }

}
