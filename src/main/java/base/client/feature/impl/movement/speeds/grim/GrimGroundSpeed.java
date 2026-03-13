package base.client.feature.impl.movement.speeds.grim;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;

import static base.client.feature.impl.movement.Speed.*;
import static base.client.helpers.Helper.mc;

public class GrimGroundSpeed {





    public static EventOnMovePost onEventOnMovePost(EventOnMovePost e, double xt, double zt) {
        PacketHelper.Values pc = Client.instance.packet;

        double bstn=GrimGroundNoAir.isEnabled() ? 0 : 0.03;
        if(index1%2==0 || ACUtil.isground()){
            bstn=0.0855;
        }

if(index1>1 && MoveUtil.getspeed2()>bstn) {
     MoveUtil.addmotXZ(xt * bstn, zt * bstn);


}
        index1++;

        return e;
    }


    public static EventPostMotion onEventPostMotion(EventPostMotion e) {
        PacketHelper.Values pc= Client.instance.packet;



        if(!ACUtil.isground()){
            index1=1;
        }

        if(index1%2==0 || ACUtil.isground()){
            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);
             pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);
        }


   return e;
    }
    public static EventReceivePacketPost onEventReceivePacketPost(EventReceivePacketPost e) {

        if (e.getPacket() instanceof ClientboundPlayerPositionPacket) {
           if(index1%2==1 && ACUtil.isground() && (!MoveUtil.motYstateh() || mc.player.verticalCollision)) {
               index1++;
           }
            TimerUtil.setTimerspeed(1);
        }
        return e;
    }



    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;
    }



}
