package base.client.feature.impl.movement.speeds.matrix;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.impl.movement.LongJump;
import base.client.feature.impl.movement.Speed;
import base.client.feature.impl.movement.Step;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import static base.client.helpers.Helper.mc;

public class MatrixOldFastHopSpeed {
    static int index1,index2;
static int groundstate;
    public static void onenable(){
          groundstate=0;
        index1=0;index2=0;
    }
    public static EventOnJump onEventOnJump(EventOnJump e) {
          e.cancel();
        return e;
    }
    public static void onEventTick() {
        mc.player.setOnGround(false);
    }





    public static void onEventOnMovePost(double xt, double zt) {
        PacketHelper.Values pc=Client.instance.packet;
        double mult = 1.999D;

        if (mc.player.verticalCollision && pc.LastGround) {
            if(Step.isstepping()) return;
            if (Speed.NGSpeedBypass.isEnabled()) {
                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false, mc.player.horizontalCollision), 10, true);
            }
            pc.LastGPosX = pc.LastPosX;
            pc.LastGPosY = pc.LastPosY;
            pc.LastGPosZ = pc.LastPosZ;
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX + xt * mult, pc.LastPosY + 0.42, pc.LastPosZ + zt * mult, false, mc.player.horizontalCollision), 10, true);
            if (Speed.Ver188MM.isEnabled()) {
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, false, mc.player.horizontalCollision), 10, true);
                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, true, false));
            } else {
                pc.LastTpNum++;
                pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, true, false));
            }
            LongJump.timerfromlastflaglong.reset();
            mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
            index2 = 1;
            MoveUtil.smartstrafe(Speed.mfhspeed.getValue());
            MoveUtil.setmotY(0.4234);
            index1 = 0;

            TimerUtil.setTimerspeed(Speed.LowTimer.getValue());
        }


        if (index1 >= 1 && index1 <= 12) {
            MoveUtil.addmotY(0.003);
        }


        index1++;
    }

    public static EventReceivePacketPre onReceivePacketPost(EventReceivePacketPre e) {
        boolean iss12 = ((e.getPacket() instanceof ClientboundExplodePacket) || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket) && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()));
        if (iss12) {
            e.cancel();
        }
return e;
    }

}
