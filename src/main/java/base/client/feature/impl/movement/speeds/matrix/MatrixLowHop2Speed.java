package base.client.feature.impl.movement.speeds.matrix;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.impl.movement.Step;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import static base.client.helpers.Helper.mc;

public class MatrixLowHop2Speed {
    static int index1,index2;
static boolean jumping;
    public static void onenable(){
   jumping=false;
        index1=0;index2=0;
    }
    public static EventOnJump onEventOnJump(EventOnJump e) {
        if (!jumping) {    e.cancel();  }
        return e;
    }




    public static void onEventOnMovePost(EventOnMovePost e, double xt, double zt) {
        PacketHelper.Values pc= Client.instance.packet;
        double nx=pc.LastPosX+1000+Math.random()*10000;   double nz=pc.LastPosZ+1000+Math.random()*10000;

        if(Step.isstepping()){
            index1=0; return;
        }

        if(mc.player.verticalCollision && mc.player.onGround()){
            index1=0;
        }
        if(index1==0){
            double sp=MoveUtil.getspeed2();
            sjump();
            if(MoveUtil.getspeed2()<0.4){
                MoveUtil.mult2ds(1.1);
            }
        }

        if(index1==2){
            pc.sendPacket(ACUtil.matrixflagpacket(),10,true);
            pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum),10,true);
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY, pc.LastPosZ,pc.LastTpNum,true,true));

            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ,false,mc.player.horizontalCollision),10,true);

            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(nx,
                    pc.LastPosY, nz, pc.LastGround, mc.player.horizontalCollision), 10, true);
            pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum),10,true);
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY, pc.LastPosZ,pc.LastTpNum,true,true));

            mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
            MoveUtil.setmotY(-0.078);
            MoveUtil.limit2speed(0.2495);
        }
        index1++;
    }
    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }
}
