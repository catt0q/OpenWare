package base.client.feature.impl.combat.velocities;

import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.impl.combat.Velocity;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

import static base.client.feature.impl.combat.Velocity.*;

public class VelocityReverse {

    public static void proccesMove(double yawt,double xt,double zt){
        if (ticksfromkb==revtick.getValue()) {
            MoveUtil.mult2ds(revmult.getValue());
            if(reverseStrafe.isEnabled()) {
                MoveUtil.smartstrafe();
            }else {
                MoveUtil.mult2ds(-1);
            }
            if(mc.player.getDeltaMovement().y()>0.1 && mc.player.fallDistance>0 && DownMR.isEnabled()) {
                MoveUtil.setmotY(-0.001);
            }

        }
    }

}
