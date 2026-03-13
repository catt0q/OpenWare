package base.client.feature.impl.combat.velocities;

import base.client.Client;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

import static base.client.feature.impl.combat.Velocity.*;

public class VelocityPolarTest {

   public static EventReceivePacketPost proccesPacket(EventReceivePacketPost e){


         return e;
    }

    public static EventReceivePacketPre proccesPacketPre(EventReceivePacketPre e){
        PacketHelper.Values pc= Client.instance.packet;
        if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {
            ClientboundSetEntityMotionPacket s12 = (ClientboundSetEntityMotionPacket) e.getPacket();
            Vec3 savedmot = mc.player.getDeltaMovement();
            MoveUtil.setmotXYZ(s12.getMovement().x, s12.getMovement().y, s12.getMovement().z);
            if (!isgoodmotion() && MoveUtil.getspeed2() > 0.1){

                if(polarticks>70) {
                    if(!mc.player.onGround()) {
                        polarticks = 0;
                        e.cancel();
                    }


                }
        }
            MoveUtil.setmotXYZ(savedmot);




        }




        return e;
    }



    public static void proccesMove(double yawt,double xt,double zt){
        PacketHelper.Values pc= Client.instance.packet;
        boolean wasjump=false;
        if(mc.player.verticalCollision && mc.player.onGround() && mc.player.isSprinting() && !veltimer.hasTimeElapsed(60,false) && Math.random()>0.5) {
         mc.player.jumpFromGround();   wasjump=true;
        }
        if(!isgoodmotion() ) {



        }

        polarticks++;
    }

}
