package base.client.feature.impl.combat.velocities;

import base.client.Client;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import static base.client.helpers.Helper.mc;

public class VelocityMatrix {

    public static EventReceivePacketPre proccesPacket(EventReceivePacketPre e){
        PacketHelper.Values pc=Client.instance.packet;
        /*   MoveUtil.setmotY(s12.getMovement().y);
                    if(MoveUtil.getspeed2()<0.1){

                    }

                   //MoveUtil.limit2speed(0.3);
if(mc.player.onGround()){
   mc.player.jumpFromGround();
}
*/
           /*         if(RotationUtils.yawdiff(pc.LastYaw,mc.player.getYRot())>0.0001 || RotationUtils.pitchdiff(pc.LastPitch,mc.player.getXRot())>0.0001){
                        pc.sendPacket(new ServerboundMovePlayerPacket.Rot(pc.LastYaw,pc.LastPitch,false,mc.player.horizontalCollision),10);
  }else{
                        pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,mc.player.horizontalCollision),10);
   }
*/
                   /* if(!pc.LastGround) {

                        pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                        pc.LastTpNum++;
                        pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                        Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, true));
                        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ, false, mc.player.horizontalCollision), 10, true);
                      //  pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,mc.player.horizontalCollision),10);
                        pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                        pc.LastTpNum++;
                        pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                        Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, true));

                        MoveUtil.setmotY(-0.078);
                        MoveUtil.limit2speed(0.2);
                        if (MoveUtil.getspeed2() < 0.2) {
                            MoveUtil.strafe(0.1);
                        }e.cancel();
                    }*/

        if(!pc.LastGround) {

            pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
            pc.LastTpNum++;
            pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, true));
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ, false, mc.player.horizontalCollision), 10, true);
            //  pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,mc.player.horizontalCollision),10);
            pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
            pc.LastTpNum++;
            pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, true));

            MoveUtil.setmotY(-0.078);
            MoveUtil.limit2speed(0.2);
            if (MoveUtil.getspeed2() < 0.2) {
                MoveUtil.strafe(0.1);
            }e.cancel();
        }




        //  MoveUtil.setmotY(s12.getMovement().y);
        //   MoveUtil.setmotY(-0.1);
        //  MoveUtil.setmotXZ(s12.getMovement().x,s12.getMovement().z);
        // pc.lastVeltimer.setLastMS(pc.lastVeltimer.getLastMS() + 100000);
        //  mc.player.setPos(pc.LastPosX,pc.LastPosY,pc.LastPosZ);




        return e;
    }

}
