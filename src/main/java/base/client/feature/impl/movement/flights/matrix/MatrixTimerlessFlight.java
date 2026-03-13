package base.client.feature.impl.movement.flights.matrix;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.impl.combat.KillAuraNew;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.Flight;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AirBlock;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static base.client.feature.impl.movement.Flight.*;
import static base.client.helpers.Helper.mc;

public class MatrixTimerlessFlight {

    static boolean spoofingvanillahardflightcheck;
    public static void onenable(){


    }






    public static void onEventOnMovePost(EventOnMovePost e, double xt, double zt) {
        PacketHelper.Values pc = Client.instance.packet;
        TimerUtil.setTimerspeed(Flight.LowTimer.getValue());
        MoveUtil.stop3();
        if(MoveUtil.getdir()!=-1) {
            if(Client.instance.featureManager.getModuleByClass(KillAuraNew.class).getState()){
                if(RotationUtils.lockedRots()){
                    pc.LastYaw=RotationUtils.lastyaw;
                    pc.LastPitch=RotationUtils.lastpitch;
                }else{
                    //     pc.LastYaw=mc.player.getYRot();
                    //       pc.LastPitch=mc.player.getXRot();
                }


            }



            for (int i = 0; i <6; i++) {
                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false, mc.player.horizontalCollision), 10);
            }

            int tries= (int) TimerlessPackets.getValue();

            double mult1 = 0.3;

            for (int i = 0; i < tries; i++) {
                EntityUtil.dtp(xt * mult1, 0, zt * mult1);
                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(),pc.LastYaw,pc.LastPitch,
                        false, mc.player.horizontalCollision), 10);
            }
            pc.LastPosX=mc.player.getX(); pc.LastPosY=mc.player.getY();   pc.LastPosZ=mc.player.getZ();

            while (!CombatPackets.isEmpty()) {
                pc.sendPacket(CombatPackets.pollFirst(), 5);
            }
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

            for(int i=0;i<tries+2;i++){
                pc.LastTpNum++;
                pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(mc.player.getX(), mc.player.getY(), mc.player.getZ(), pc.LastTpNum, false, false));
            }






            //  pc.sendPacket(new ServerboundMoveVehiclePacket(mc.player.position(),pc.LastYaw,pc.LastPitch,mc.player.onGround()), 10);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

            EntityUtil.dtp(-xt * mult1*tries, 0, -zt * mult1*tries);
            for (int i = 0; i < tries; i++) {
                EntityUtil.dtp(xt * mult1, 0, zt * mult1);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        false, mc.player.horizontalCollision), 10);
            }


        }else{
            //  pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
        }
    }


    public static EventSendPacketBlink onEventSendPacketBlink(EventSendPacketBlink e) {


        if(e.getPermissionidstate()<5 && (e.getPacket() instanceof ServerboundInteractPacket
                || e.getPacket() instanceof ServerboundSwingPacket || e.getPacket() instanceof ServerboundPlayerCommandPacket)
        ) {
            e.cancel();
            CombatPackets.add(e.getPacket());
        }
        if(e.getPermissionidstate()<5 && e.getPacket() instanceof ServerboundMovePlayerPacket) {
            e.cancel();
        }
        return e;
    }
}
