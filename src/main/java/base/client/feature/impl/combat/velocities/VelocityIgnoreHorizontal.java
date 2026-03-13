package base.client.feature.impl.combat.velocities;

import base.client.Client;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

import static base.client.feature.impl.combat.Velocity.*;
import static base.client.helpers.Helper.mc;

public class VelocityIgnoreHorizontal {
    public static EventReceivePacketPre proccesPacket(EventReceivePacketPre e){
         ClientboundSetEntityMotionPacket s12=(ClientboundSetEntityMotionPacket) e.getPacket();
      MoveUtil.setmotY(s12.getMovement().y);
        e.cancel();
        return e;
    }
    public static void proccesMove(double yawt,double xt,double zt){
        if(ticksfromkb<BstTicks.getValue()) {
            if(BstMode.getCurrentMode().equals("Mult")) {   MoveUtil.mult2ds(MultBst.getValue());   }else {
                yawt = Math.toRadians(Minecraft.getInstance().player.getYRot());  yawt = Math.toRadians( MoveUtil.getdir());  xt = -Math.sin(yawt);     zt = Math.cos(yawt); if(MoveUtil.getdir()==-1) { xt=0; zt=0;	   	 }
                double mult=MotionBst.getValue();MoveUtil.addmotX(xt*mult); MoveUtil.addmotZ(zt*mult);
            }
        }
    }

}
