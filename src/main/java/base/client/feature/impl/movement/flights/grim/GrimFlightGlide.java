package base.client.feature.impl.movement.flights.grim;

import base.client.Client;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.movement.Flight;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import static base.client.feature.impl.movement.Flight.*;
import static base.client.helpers.Helper.mc;

public class GrimFlightGlide {
   static double currentspeed=0;
    public static void onenable(){
        currentspeed=0;
    }




    public static EventPreMotion onEventPreMotion(EventPreMotion e) {
if(index7==0){
    return e;
}
        PacketHelper.Values pc = Client.instance.packet;
        if(index2==1) {
            e.setYaw(pc.LastYaw); e.setPitch(pc.LastPitch);

  }else {
            e.setPitch((float) (pc.LastPitch+Math.random()*0.001));
            e.setYaw((float) (pc.LastYaw+Math.random()*0.001));
        }
        return e;
    }


    public static void onEventOnMovePost() {
        PacketHelper.Values pc = Client.instance.packet;

        double maxsprintspeed=0.300999D;

        if(index7==0) {
            if (mc.player.verticalCollision && ACUtil.isground()) {
                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,mc.player.horizontalCollision),10,true);
                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true,mc.player.horizontalCollision),10,true);


                TimerUtil.setTimerspeed(0.29); index1=1;
                Flight.jump();MoveUtil.addmotY(0.03);
            } else if (CombatUtil.fallDistance>0.1 && pc.lastTptimer.hasTimeElapsed(100,false)) {
                index7 = 1;
                currentspeed = mc.player.isSprinting() ? 0.28 : 0.22;
                if(MoveUtil.getdir()==-1){
                    currentspeed=0.01;
                }

            }else if(index1==1){
                index1=0;MoveUtil.addmotY(0.03);
                TimerUtil.setTimerspeed(1);
            }

        }

        if(index7==0){
            return;
        }
        if(index6>20 && ACUtil.ismatrixonground() && index2==2){
             Module md=Client.instance.featureManager.getModuleByClass(Flight.class);   md.toggle();MoveUtil.stop3();
           return;
        }


        if(index1%2==0 && index2==0)
        {MoveUtil.stop3();
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true,mc.player.horizontalCollision),10,true);
            long deltatime=System.currentTimeMillis()-flyTimer.getLastMS();
            flyTimer.reset();

            CombatUtil.resetfd();
            index1=0;
            index2=1;

            index4=0;
            TimerUtil.setTimerspeed(1);
            if(index3>=60){
                index3=0;
                TimerUtil.setTimerspeed(0.499);
                index4=1;
            }


            index3++;
        }else if(index2==2){
            long deltatime=System.currentTimeMillis()-flyTimer.getLastMS();
            if(MoveUtil.getdir()==-1){
                currentspeed = Math.max(currentspeed - 0.04, 0.01);
            }else if(RotationUtils.yawdiff(pc.LastYaw,mc.player.getYRot())<1){
                if (mc.player.isSprinting()) {

                    currentspeed = Math.min(currentspeed + 0.005, maxsprintspeed);
                    currentspeed=Math.max(currentspeed,0.2);
                } else {

                    currentspeed = Math.min(currentspeed + 0.005, 0.2499);
                    currentspeed=Math.max(currentspeed,0.15);
                }
            }
            index3++;
          index2=0;index1=-1;
            MoveUtil.minstrafe(currentspeed);
            if(index4==0 && currentspeed>=maxsprintspeed) {
                MoveUtil.addmotY(0.000998);
            }
        }else{
            index1=-1;
            MoveUtil.stop3();


                index5++;

        }
        index6++;
        index1++;
    }

    public static EventSendPacketCancel onEventSendPacketCancel(EventSendPacketCancel e) {
        if(index7==0){
            return e;
        }

        if(e.getPacket() instanceof ServerboundMovePlayerPacket && index2==1 && index4!=1) {
                 e.cancel();

     }

        return e;
    }
    public static EventReceivePacketPre onEventReceivePacketPre(EventReceivePacketPre e) {
        if(index7==0){
            return e;
        }
        Packet<?> packetp=e.getPacket();

        if (packetp instanceof ClientboundSetEntityMotionPacket) {
            if(((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()){
            if(index2==1) {
                ClientboundSetEntityMotionPacket s12=(ClientboundSetEntityMotionPacket) e.getPacket();
           //     MoveUtil.setmotXYZ(s12.getMovement().x/8000D,s12.getMovement().y/8000D,s12.getMovement().z/8000D);
                e.cancel();
                index2=2;
            }
            }
        }

        if (packetp instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket ss=(ClientboundPlayerPositionPacket) e.getPacket();

//e.setPacket(new ClientboundPlayerPositionPacket());

        }

        return e;
    }
}
