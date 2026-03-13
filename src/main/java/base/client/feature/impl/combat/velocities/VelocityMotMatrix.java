package base.client.feature.impl.combat.velocities;

import base.client.Client;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.impl.combat.Velocity;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import static base.client.helpers.Helper.mc;
import static base.client.helpers.Helper.pc;

public class VelocityMotMatrix {
    public static EventReceivePacketPre proccesPacket(EventReceivePacketPre e,double xt,double zt){
        PacketHelper.Values pc= Client.instance.packet;
        ClientboundSetEntityMotionPacket s12 = (ClientboundSetEntityMotionPacket) e.getPacket();
        double bstn1 = 0;
        pc.lastVeltimer.reset();
           e.cancel();

Velocity.prevMot=mc.player.getDeltaMovement();
double prevSpeed=MoveUtil.getspeed2();
       // MoveUtil.setmotY(s12.getMovement().y);


 /*
MoveUtil.setmotXZ(s12.getMovement().x,s12.getMovement().z);

if(MoveUtil.getspeed2()<prevSpeed){
 MoveUtil.setmotXZ(Velocity.prevMot.x,Velocity.prevMot.z);
}
*/
        //MoveUtil.mult2ds(1.06);


        if(mc.player.onGround() && ACUtil.isground()){
if(MoveUtil.motYstateh()){

            }




        }else{
if(MoveUtil.getspeed2()>0.2) {


}




        }

Velocity.ticks=20;




        return e;
    }

    public static void proccesMove(){
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if(Velocity.ticks==20) {
            if (mc.player.getDeltaMovement().y > 0 && CombatUtil.fallDistance > 0) {
                //       MoveUtil.setmotY(-mc.player.getDeltaMovement().y);
            }
           // MoveUtil.setmotY(-mc.player.getDeltaMovement().y);
            if (mc.player.getDeltaMovement().y < 0 && CombatUtil.fallDistance > 0) {
            // MoveUtil.setmotY(-0.001);
            }


        }else{





        }

if(pc.airpackets>1) {
    if (Velocity.ticks>18 && MoveUtil.getspeed2()>0.25) {
    //    ChatHelper.addChatMessage("hi");
        MoveUtil.addmotXZ(xt * 0.009, zt * 0.009);

    }


}


        if(Velocity.ticks>0){
            Velocity.ticks--;
        }
    }

}
