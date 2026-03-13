package base.client.feature.impl.movement.speeds.matrix;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.impl.movement.AirStuck;
import base.client.feature.impl.movement.Step;
import base.client.feature.impl.movement.TargetStrafe;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static base.client.helpers.Helper.mc;

public class MatrixHighHopSpeed {
    static int index1,index2,index3,groundstate;
static boolean jumping,wasboost;
    public static void onenable(){
          groundstate=0;jumping=false;
        index1=0;index2=0;index3=0;
    }
    public static EventOnJump onEventOnJump(EventOnJump e) {
        if (!jumping) {    e.cancel();  }
        return e;
    }
    public static void onEventTick() {
        if(groundstate==1){
            mc.player.setOnGround(false); groundstate=0;
        }else if(groundstate==2){
            mc.player.setOnGround(true);groundstate=0;
        }
    }


    public static EventPreMotion onEventPreMotion(EventPreMotion e,double xt,double zt) {
        PacketHelper.Values pc= Client.instance.packet;
       wasboost=false;
        if(mc.player.onGround() && mc.player.verticalCollision) { wasboost=true; index2=1;
            groundstate = 2;      sjump(); 	 if(!ACUtil.matrixisusingitem() && !mc.player.isShiftKeyDown()) {
                MoveUtil.minsmartstrafe(0.3);  }

          }
        if (!wasboost && ACUtil.ismatrixonground()
                && mc.player.getDeltaMovement().y()>=0 && (index2!=1 || !pc.LastGround) && (!mc.player.horizontalCollision || index1!=2 || MoveUtil.getspeed2()>0.04)
        ) {
          e.setOnGround(true);    groundstate = 2;  CombatUtil.fallDistance=0;
          jumping=true;  mc.player.jumpFromGround();

            if(!ACUtil.matrixisusingitem() && !mc.player.isShiftKeyDown()) {
                MoveUtil.minsmartstrafe(0.3);  }
            jumping=false;
            index2++;
        }





        index1++;
        return e;
    }

    public static EventOnMovePost onEventOnMovePost(EventOnMovePost e, double xt, double zt) {
        PacketHelper.Values pc = Client.instance.packet;

        if(Step.isstepping()){
            index1=0; return e;
        }

        if (index1 >= 2 && index1 <= 16 && !wasboost) {

            if (!mc.player.isShiftKeyDown() && !mc.player.isInWater()) {

                BlockPos bp2 = new BlockPos(Minecraft.getInstance().player.getBlockX(), (int) (Minecraft.getInstance().player.getY() + 3), Minecraft.getInstance().player.getBlockZ());
                BlockState bs2 = Minecraft.getInstance().level.getBlockState(bp2);
                Block bl2 = bs2.getBlock();
                if (!mc.player.horizontalCollision && (bl2 instanceof AirBlock) && pc.lastVeltimer.hasTimeElapsed(166, false)) {
                    if (ACUtil.matrixboost2()) {
                    } else if (MoveUtil.getspeed2() < 0.3 && MoveUtil.getspeed2() > 0.2 && MoveUtil.getdir() != -1 && RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 44) {
                        if (RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 130) {
                            double bstn = 0.199;
                            if (MoveUtil.isdiag() || Client.instance.featureManager.getModuleByClass(TargetStrafe.class).getState())
                                bstn = 0;
                            MoveUtil.addmotX(xt * bstn);
                            MoveUtil.addmotZ(zt * bstn);
                        } else {
                            MoveUtil.limit2speed(0.199);
                            MoveUtil.smartstrafe();
                        }

                    }
                }
                if ((!mc.player.verticalCollision || mc.player.onGround()) && !ACUtil.matrixisusingitem() && pc.lastVeltimer.hasTimeElapsed(3000, false) && !Client.instance.featureManager.getModuleByClass(AirStuck.class).getState() && (bl2 instanceof AirBlock)
                ) {
                    MoveUtil.addmotY(-0.003);
                }
            }
        }
        return e;
    }

    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }
}
