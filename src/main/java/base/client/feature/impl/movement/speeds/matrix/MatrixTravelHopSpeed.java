package base.client.feature.impl.movement.speeds.matrix;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.Speed;
import base.client.feature.impl.movement.Step;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;

import static base.client.helpers.Helper.mc;

public class MatrixTravelHopSpeed {

    static boolean jumping;
    static int index1,index2;

    public static void onenable(){
        jumping=false;
        index1=0;
        index2=-1;
     }
    public static EventOnJump onEventOnJump(EventOnJump e) {
        if (!jumping) {    e.cancel();  }
        return e;
    }

    public static EventOnMovePost onEventOnMovePost(EventOnMovePost e,double xt,double zt) {
        PacketHelper.Values pc = Client.instance.packet;
        if(Step.isstepping()){
            index1=0; return e;
        }

        if(mc.player.verticalCollision && mc.player.onGround()){
            sjump();
        }
        if(index1<13 && index1>=1) {
           double yawt = Math.toRadians(MoveUtil.getdir());   xt = -Math.sin(yawt);  zt = Math.cos(yawt);
            if (MoveUtil.getdir() == -1) {  xt = 0;      zt = 0;  }
            double mult = 0.06249;


            if(xt!=0 || zt!=0) {

                for (int i = 0; i < Speed.THP.getValue(); i++) {
                    mc.player.setPos(mc.player.getX() + xt * mult, mc.player.getY(), mc.player.getZ() + zt * mult);
                    Disabler.savedabusepacket--;
                    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
                    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(),  mc.player.onGround(), false), 10, true);


                }

                pc.LastPosX = mc.player.getX();
                pc.LastPosY = mc.player.getY();
                pc.LastPosZ = mc.player.getZ();

            }
        }


        index1++;

        return e;
    }
    public static EventReceivePacketPre onReceivePacketPost(EventReceivePacketPre e) {
        boolean iss12 = ((e.getPacket() instanceof ClientboundExplodePacket) || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket) && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()));
        if (iss12) {
            e.cancel();
        }
        return e;
    }

    public static EventSendPacketCancel onEventSendPacketCancel(EventSendPacketCancel e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();
        if (packetp instanceof ServerboundMovePlayerPacket.Rot) {
            ServerboundMovePlayerPacket.Rot c05 = (ServerboundMovePlayerPacket.Rot) packetp;
            e.cancel();

        } else if (packetp instanceof ServerboundMovePlayerPacket.PosRot) {
            ServerboundMovePlayerPacket.PosRot c06 = (ServerboundMovePlayerPacket.PosRot) packetp;
            e.cancel();
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(c06.x, c06.y, c06.z, c06.onGround, c06.horizontalCollision));
        }

        if (packetp instanceof ServerboundUseItemPacket) {
            ServerboundUseItemPacket c082 = (ServerboundUseItemPacket) packetp;
            e.setPacket(new ServerboundUseItemPacket(c082.getHand(), c082.getSequence(),pc.LastYaw,pc.LastPitch));
        }
        return e;
    }

    private static void sjump(){
   jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }
}
