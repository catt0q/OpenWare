package base.client.feature.impl.combat.velocities.grim;

import base.client.Client;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.impl.movement.Speed;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static base.client.feature.impl.combat.Velocity.*;

public class VelocityGrim2372 {



    public static EventReceivePacketPre proccesPacketPre(EventReceivePacketPre e){
        PacketHelper.Values pc= Client.instance.packet;
        boolean iss12 = ((e.getPacket() instanceof ClientboundExplodePacket) || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket) && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()));
        if(iss12 ) {
            if ((Client.instance.featureManager.getModuleByClass(Speed.class).getState())) {
               return e;
            }


            ticks=MoveUtil.getspeed3()<0.01 ? 1 : 3;
            e.cancel();
            mc.player.swing(InteractionHand.MAIN_HAND);
             if(MoveUtil.getspeed3()>0.01) {
                 pc.sendPacket(new ServerboundMovePlayerPacket.Rot(pc.LastYaw,pc.LastPitch,false, false), 10, true);

                 TimerUtil.setTimerspeed(0.4984);
             }





            PacketHelper.Values.sendPacket(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    mc.player.blockPosition(), Direction.UP, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));


            mc.player.swing(InteractionHand.MAIN_HAND);




        }
        return e;
    }



    public static void proccesMove(){
        PacketHelper.Values pc= Client.instance.packet;
if(ticks>0){
    if(prevMot.equals(Vec3.ZERO)){
        prevMot=mc.player.getDeltaMovement();
    }

    MoveUtil.stop3();
}else if(!prevMot.equals(Vec3.ZERO)){
    //MoveUtil.setmotXYZ(prevMot);prevMot=Vec3.ZERO;
    MoveUtil.stop3();
    TimerUtil.setTimerspeed(1);
}


    }
    public static EventSendPacketCancel proccesPacketsend(EventSendPacketCancel e){
        PacketHelper.Values pc= Client.instance.packet;
        if(e.getPacket() instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) e.getPacket();
            if (ticks > 0) {
                ticks--;
            }
        }
        return e;
    }

    public static void proccesPacketsend(){
        PacketHelper.Values pc= Client.instance.packet;
         if (ticks > 0) {

         /*    Vec3 vec = mc.player.calculateViewVector(mc.player.getYRot(), mc.player.getXRot());
             BlockHitResult blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
             PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
*/
                          /*    try (BlockStatePredictionHandler blockStatePredictionHandler = mc.level.getBlockStatePredictionHandler().startPredicting()) {
                     pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND,
                             new BlockHitResult(mc.player.position().with(Direction.Axis.Y, mc.player.getBlockY() - i), Direction.UP, mc.player.blockPosition().below().below(i), false), blockStatePredictionHandler.currentSequence()));
                 }*/



        /*     Vec3 vec = mc.player.calculateViewVector(mc.player.getYRot(),  mc.player.getXRot());
             BlockHitResult blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
             PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(hand, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
             vec = mc.player.calculateViewVector(mc.player.getYRot(), 90);
             blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
             PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(hand, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
*/
             mc.player.swing(InteractionHand.MAIN_HAND);
             Vec3 vec = mc.player.calculateViewVector(mc.player.getYRot(),  mc.player.getXRot());
             BlockHitResult blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
             PacketHelper.Values.sendPacket(new ServerboundPlayerActionPacket(
 ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                     mc.player.blockPosition(), Direction.UP, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));

             mc.player.swing(InteractionHand.MAIN_HAND);


         }
    }
}
