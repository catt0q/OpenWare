package base.client.feature.impl.movement.glides;

import base.client.Client;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.AirStuck;
import base.client.feature.impl.movement.TargetStrafe;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static base.client.feature.impl.movement.Glide.*;
import static base.client.helpers.Helper.mc;
import static base.client.helpers.Helper.pc;

public class MatrixDamageGlide {


    public static void onEventOnMovePost(double xt,double zt) {
        PacketHelper.Values pc=Client.instance.packet;

        if(index2==1) {
            if (pc.lastVeltimer.hasTimeElapsed(1300, false) && index1 > 4) {
                index2 = 0;
            }
            MoveUtil.setmotY(-0.00001);

            if (index6 == 1) {
                index6--;
                TimerUtil.setTimerspeed(1);

            }


        }

        if (!Ver188MM.isEnabled()) {
            if(CombatUtil.fallDistance>2.9 && index2==0) {
                CombatUtil.resetfd();

                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);

                pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision), 10);

    if (Disabler.notimerbalance()) {
                    TimerUtil.setTimerspeed(0.31);
                }
                index7 = 3;
                index2 = 1;
                index6 = 1;
                index1=0;

            }
        }



index1++;
    }

    public static EventPreMotion onEventPreMotion(EventPreMotion e) {

        if (Ver188MM.isEnabled()) {
            if(CombatUtil.fallDistance>2.9 && index2==0) {
                CombatUtil.resetfd();



                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(e.getPosX(), e.getPosY(), e.getPosZ(),
                        true, mc.player.horizontalCollision),10,true);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(e.getPosX(), e.getPosY(), e.getPosZ(),
                        false, mc.player.horizontalCollision),10,true);





            if (Disabler.notimerbalance()) {
                TimerUtil.setTimerspeed(0.31);
            }
            index7 = 3;
            index2 = 1;
            index6 = 1;
            index1=0;

  }
        }



        if(index7>0){
            index7--;
            e.setYaw(pc.LastYaw); e.setPitch(pc.LastPitch);
        }



        return e;
    }



}
