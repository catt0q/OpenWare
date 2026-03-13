package base.client.feature.impl.combat.velocities;

import base.client.Client;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import static base.client.feature.impl.combat.Velocity.*;

public class VelocityLegit {


    public static EventReceivePacketPost proccesPacket(EventReceivePacketPost e){

        boolean iss12=((e.getPacket() instanceof ClientboundExplodePacket) || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket) && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()));
        if(iss12 && modifynexts12) {
            MoveUtil.mult2ds(0.6);
        }
         return e;
    }

    public static EventReceivePacketPre proccesPacketPre(EventReceivePacketPre e){
        PacketHelper.Values pc= Client.instance.packet;
        if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {
            if(DoubleReduce.isEnabled() && DRM.getCurrentMode().equals("Simple2") && !pc.lastC02timer.hasTimeElapsed((long) LTimeFromLastAttack.getValue(),false)) {
                ClientboundSetEntityMotionPacket packetp = (ClientboundSetEntityMotionPacket) e.getPacket();
                double lastMotX=mc.player.getDeltaMovement().x(); double lastMotY=mc.player.getDeltaMovement().y(); double lastMotZ=mc.player.getDeltaMovement().z();
                //MoveUtil.setmotXYZ(packetp.getVelocityX(),packetp.getVelocityY(),packetp.getVelocityZ());
                MoveUtil.setmotXYZ(lastMotX,lastMotY,lastMotZ);

                if(!isgoodmotion()) {
                    modifynexts12=true;
                }
            }


        }
        return e;
    }



    public static void proccesMove(double yawt,double xt,double zt){
        PacketHelper.Values pc= Client.instance.packet;
        boolean wasjump=false;
        if(LegitJump.isEnabled() && mc.player.verticalCollision && mc.player.onGround() && mc.player.isSprinting() && !veltimer.hasTimeElapsed(60,false) && legitjump()) {
             wasjump=true;
        }
        if(!isgoodmotion()) {
            if(!wasjump && DoubleReduce.isEnabled() && DRM.getCurrentMode().equals("Simple1") && !veltimer.hasTimeElapsed((long) LTimeFromKB.getValue(),false) && !pc.lastC02timer.hasTimeElapsed((long) LTimeFromLastAttack.getValue(),false)) {
                MoveUtil.mult2ds(LegitReduceMult.getValue());
            }

            if(LegitTimer.isEnabled()) {
                if(ticksfromkb<=(LegitTimerTicks.getValue()-1)) {
                    TimerUtil.setTimerspeed(LegitTimerVal.getValue());
                }else if(ticksfromkb>(LegitTimerTicks.getValue()-1) && TimerUtil.getTimerspeed()==LegitTimerVal.getValue()) {
                    TimerUtil.setTimerspeed(1);
                }


            }

        }

        if(LegitTimer.isEnabled()) {
            if(ticksfromkb>(LegitTimerTicks.getValue()-1) && TimerUtil.getTimerspeed()==LegitTimerVal.getValue()) {
                TimerUtil.setTimerspeed(1);
            }


        }
    }

}
