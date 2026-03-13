package base.client.feature.impl.combat.velocities.grim;

import base.client.Client;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.impl.movement.Speed;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static base.client.feature.impl.combat.Velocity.*;

public class VelocityGrimSimple {



    public static EventReceivePacketPre proccesPacketPre(EventReceivePacketPre e){
        PacketHelper.Values pc= Client.instance.packet;
        boolean iss12 = ((e.getPacket() instanceof ClientboundExplodePacket) || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket) && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()));
        if(iss12 ) {
            if ((Client.instance.featureManager.getModuleByClass(Speed.class).getState())) {
               return e;
            }


            ticks=2;
            e.cancel();





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
    MoveUtil.setmotXYZ(prevMot);prevMot=Vec3.ZERO;
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


InteractionHand hand=InteractionHand.MAIN_HAND;
if(mc.player.getUsedItemHand().equals(hand)){
    hand=InteractionHand.OFF_HAND;
}





         }
    }
}
