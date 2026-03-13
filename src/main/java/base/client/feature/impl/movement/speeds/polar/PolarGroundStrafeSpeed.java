package base.client.feature.impl.movement.speeds.polar;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

import static base.client.feature.impl.movement.Speed.jumping;
import static base.client.helpers.Helper.mc;

public class PolarGroundStrafeSpeed {

    static int index1,boostTick,flagskip,stage;

    public static void onenable(){
        jumping=false;
        index1=0;
        boostTick=0;
        stage = 0;
        flagskip = 0;
    }
    public static EventOnJump onEventOnJump(EventOnJump e) {
        if (!jumping) {    e.cancel();  }
        return e;
    }

    public static EventOnMovePost onEventOnMovePost(EventOnMovePost e, double xt, double zt) {
        PacketHelper.Values pc = Client.instance.packet;
        if (mc.player.verticalCollision && ACUtil.isground() && index1 > 2) {
            boostTick++;
            index1 = 0;TimerUtil.setTimerspeed(0.5);
            EntityUtil.dtp(0,-0.078,0); MoveUtil.stop3();
            flagskip++;
        }
        if(flagskip==0) {
            if (index1 == 1 && boostTick > 0) {
                sjump();
                MoveUtil.setmotY(-0.078);

            }
            if (index1 == 2 && boostTick > 0) {TimerUtil.setTimerspeed(1);
                sjump();
            }
        }
        index1++;

        return e;
    }

    public static EventReceivePacketPost onEventReceivePacketPost(EventReceivePacketPost e) {
        if (e.getPacket() instanceof ClientboundPlayerPositionPacket) {
            if (flagskip > 0) {
                flagskip = 0;index1=1;
                TimerUtil.setTimerspeed(1.5);
            }

        }
        return e;
    }



    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;
    }



}
