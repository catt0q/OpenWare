package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MoreSwing extends Module {

TimerHelper combatstarted = new TimerHelper();

    ModeSetting Mode;
    NumberSetting Swings,Radius,CombatMS;
    ModeSetting swingMode;
    ModeSetting AInteractionHand;



    public MoreSwing() {
        super("MoreSwing", "Бесконечное золотое вращение", Type.Combat);
        Mode = new ModeSetting("Mode", "EnemyInRadius",() -> true, "Stable","EnemyInRadius","OnAttack","InCombat");

        Swings = new NumberSetting("Swings", 1,1, 10F, 1, () -> Mode.currentMode.equals("Stable"));
        Radius = new NumberSetting("Radius", 6,1, 12F, 1, () -> Mode.currentMode.equals("EnemyInRadius"));
        CombatMS = new NumberSetting("CombatMS", 2000,0, 10000F, 50, () -> Mode.currentMode.equals("InCombat"));
        swingMode = new ModeSetting("Swing Mode", "Default", () -> true, "Default", "Packet");
         AInteractionHand = new ModeSetting("InteractionHand", "Right", () ->true, "Right", "Left");


        this.addSettings(Mode,swingMode,AInteractionHand,Swings,Radius,CombatMS
               );

    }

    @Override
    public void onEnable() {
        combatstarted.lastMS+=50000;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onPrePacket(EventSendPacketCancel event) {
        PacketHelper.Values pc = Client.instance.packet;

        if (!event.isCancelled()) {
            Packet<?> packetp = event.getPacket();
            if (packetp instanceof ServerboundInteractPacket) {

                ServerboundInteractPacket c02 = (ServerboundInteractPacket) packetp;


                base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
                ServerboundInteractPacket.Action action = ac2.getAction();
                Entity ent = mc.level.getEntity(ac2.getEntityId());
                if (action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK) &&
                        (ent instanceof LivingEntity)) {

combatstarted.reset();

                }
            }
        }
    }

    @EventTarget
    public void onPrePacket(EventSendPacketPost event) {
        PacketHelper.Values pc = Client.instance.packet;


            Packet<?> packetp = event.getPacket();
            if (packetp instanceof ServerboundInteractPacket) {

                ServerboundInteractPacket c02 = (ServerboundInteractPacket) packetp;


                base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
                ServerboundInteractPacket.Action action = ac2.getAction();
                Entity ent = mc.level.getEntity(ac2.getEntityId());
                if (action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK) &&
                        (ent instanceof LivingEntity)) {

                    if(Mode.getCurrentMode().equals("OnAttack")){
                        proccesswing();
                    }


            }
        }
    }


    @EventTarget
    public void onUpdate(EventPreMotion event) {
        PacketHelper.Values pc = Client.instance.packet;
        this.setSuffix(Mode.getCurrentMode());
switch (Mode.getCurrentMode()){
    case ("Stable"):
        proccesswing();
 break;
    case ("InCombat"):
        if(!combatstarted.hasTimeElapsed((long)CombatMS.getValue(),false)) {
            proccesswing();
        }
        break;
    case ("EnemyInRadius"):
        java.util.List<Entity> targets=getTargets();
        if(targets.size()>0) {
            proccesswing();
        }
        break;

}


    }





    private void proccesswing() {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (AInteractionHand.getCurrentMode().equals("Left")) {
            hand = InteractionHand.OFF_HAND;
        }
        if (swingMode.getCurrentMode().equals("Packet")) {
            for(int i=0;i<Swings.getValue();i++) {
                mc.getConnection().send(new ServerboundSwingPacket(hand));
            }
        } else if (this.swingMode.getCurrentMode().equals("Default")) {
            for(int i=0;i<Swings.getValue();i++) {
                mc.player.swing(hand);
            }
        }
    }
    java.util.List<Entity> getTargets() {

        java.util.List<Entity> targets = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(),false).filter(LivingEntity.class::isInstance).collect(Collectors.toList());
        targets = targets.stream().filter(entity -> entity != null && entity.getId() != mc.player.getId() &&
                entity instanceof LivingEntity && !AntiBot.isBotList(entity.getUUID()) && Client.instance.friendManager.isFriend(entity.getName().getString()) && mc.player.distanceTo(entity)<Radius.getValue() &&
                entity.isAlive() ).collect(Collectors.toList());


        return targets;
    }

                }
