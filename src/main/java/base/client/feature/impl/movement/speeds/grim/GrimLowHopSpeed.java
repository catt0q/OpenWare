package base.client.feature.impl.movement.speeds.grim;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import static base.client.feature.impl.movement.Speed.*;
import static base.client.helpers.Helper.mc;

public class GrimLowHopSpeed {

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
            index1 = 0;
            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING),10,true);
            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);

            flagskip++;
        }
        if (index1 == 1 && boostTick > 0) {
            sjump();

            MoveUtil.addmotY(-0.03);
            double bst = 0.085;
            MoveUtil.addmotX(xt * bst);
            MoveUtil.addmotZ(zt * bst);
        }
        if (index1 == 2 && boostTick > 0) {
            MoveUtil.addmotY(-0.0199);
            double bst = 0.03;
            MoveUtil.addmotX(xt * bst);
            MoveUtil.addmotZ(zt * bst);
        }
        index1++;

        return e;
    }

    public static EventReceivePacketPost onEventReceivePacketPost(EventReceivePacketPost e) {
        if (e.getPacket() instanceof ClientboundPlayerPositionPacket) {
            if (flagskip > 0) {
                flagskip = 0;
            }

        }
        return e;
    }



    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;
    }



}
