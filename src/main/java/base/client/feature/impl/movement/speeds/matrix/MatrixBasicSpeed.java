package base.client.feature.impl.movement.speeds.matrix;

import base.client.Client;
import base.client.feature.impl.movement.AirStuck;
import base.client.feature.impl.movement.TargetStrafe;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static base.client.helpers.Helper.mc;

public class MatrixBasicSpeed {

    static int index1;

    public static void onenable(){
        index1=0;
    }


    public static void onEventOnMovePost(double xt,double zt) {
        PacketHelper.Values pc=Client.instance.packet;
        BlockPos bp2=new BlockPos(Minecraft.getInstance().player.getBlockX(), (int) (Minecraft.getInstance().player.getY() +3), Minecraft.getInstance().player.getBlockZ());
        BlockState bs2=Minecraft.getInstance().level.getBlockState(bp2);
        Block bl2=bs2.getBlock();
        if(!mc.player.isShiftKeyDown() && !mc.player.isInWater()) {


            if(!mc.player.horizontalCollision && (bl2 instanceof AirBlock) && pc.lastVeltimer.hasTimeElapsed(166,false)) {
                if (ACUtil.matrixboost2()) {
                } else if (MoveUtil.getspeed2() < 0.3 && MoveUtil.getspeed2() > 0.2 && MoveUtil.getdir() != -1 && RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 44) {
                    if (RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 130) {
                        double bstn = 0.199;
                        if (MoveUtil.isdiag() || Client.instance.featureManager.getModuleByClass(TargetStrafe.class).getState()) bstn = 0;
                        MoveUtil.addmotX(xt * bstn);
                        MoveUtil.addmotZ(zt * bstn);
                    } else {
                        MoveUtil.limit2speed(0.199);
                        MoveUtil.smartstrafe();
                    }

                }
            }
            if ((!mc.player.verticalCollision || mc.player.onGround()) && !ACUtil.matrixisusingitem() && pc.lastVeltimer.hasTimeElapsed(3000,false) && !Client.instance.featureManager.getModuleByClass(AirStuck.class).getState() && (bl2 instanceof AirBlock)
            ) {
                MoveUtil.addmotY(-0.003);
            }
        }


    }



}
