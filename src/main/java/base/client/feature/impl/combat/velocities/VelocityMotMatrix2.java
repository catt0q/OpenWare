package base.client.feature.impl.combat.velocities;

import base.client.Client;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.impl.combat.Velocity;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

import static base.client.helpers.Helper.mc;

public class VelocityMotMatrix2 {
    public static EventReceivePacketPre proccesPacket(EventReceivePacketPre e){
        PacketHelper.Values pc= Client.instance.packet;
        ClientboundSetEntityMotionPacket s12 = (ClientboundSetEntityMotionPacket) e.getPacket();
        double bstn1 = 0.05;
        if(((s12.getMovement().y<0.1 || s12.getMovement().y>0.5 || Math.abs(s12.getMovement().x)>1 || Math.abs(s12.getMovement().z)>1)  && !(mc.player.onGround() && mc.player.verticalCollision)) ) {
            e.cancel();
        }else{
            MoveUtil.setmotY(s12.getMovement().y);
            e.cancel();
        }
        return e;
    }


}
