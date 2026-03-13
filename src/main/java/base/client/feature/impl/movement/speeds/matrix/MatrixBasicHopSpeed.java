package base.client.feature.impl.movement.speeds.matrix;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.impl.combat.Criticals;
import base.client.feature.impl.movement.AirStuck;
import base.client.feature.impl.movement.Sprint;
import base.client.feature.impl.movement.Step;
import base.client.feature.impl.player.Scaffold;
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

public class MatrixBasicHopSpeed {

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
        if(Scaffold.isTowering() || Step.isstepping() || mc.player.isSwimming() ||
                (MoveUtil.getmf()<0 && (Client.instance.featureManager.getModuleByClass(Sprint.class).getState())
                         && Sprint.acceptAllDir()



                ) ){
            index1=0; jumping=false;
            return e;    }
        BlockPos bp=new BlockPos(Minecraft.getInstance().player.getBlockX(), (int) (Minecraft.getInstance().player.getY() +3), Minecraft.getInstance().player.getBlockZ());
        BlockState bs=Minecraft.getInstance().level.getBlockState(bp);
        Block bl=bs.getBlock();

        boolean vel=pc.lastVeltimer.hasTimeElapsed(0,false);
        vel=true;
        boolean usingitem= ACUtil.matrixisusingitem();
        boolean critallowed=(pc.lastC02timer.hasTimeElapsed(110,false) || !(Client.instance.featureManager.getModuleByClass(Criticals.class).getState()));

        if (mc.player.onGround() || (pc.LastGround && mc.player.verticalCollision)) {
            if(vel) {
                sjump();

                if (!ACUtil.matrixisusingitem() && !mc.player.isShiftKeyDown()  && critallowed && !mc.player.horizontalCollision && (bl instanceof AirBlock) && vel) {
                    if (MoveUtil.getspeed2() < 0.2) {
                        MoveUtil.minsmartstrafe(0.2495);
                    } else {
                        if (MoveUtil.getspeed2() < 0.45 && index2 == 0) {
                            MoveUtil.mult2ds(1.07);   // ChatHelper.addChatMessage(" "+MoveUtil.getspeed2());
                        }

                    }
                }
            }
        } else if(!usingitem){
            if(!mc.player.isCrouching() && !mc.player.horizontalCollision && (bl instanceof AirBlock) && vel && critallowed) {
                if (ACUtil.matrixboost2()) {
                } else if (!usingitem && MoveUtil.getspeed2() < 0.3 && MoveUtil.getspeed2() > 0.2 && MoveUtil.getdir() != -1 && RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 44) {
                    if (RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 130) {
                        double bstn = 0.199;
                        //if (diag || Client.instance.featureManager.getModuleByClass(TargetStrafe.class).getState()) bstn = 0;
                        MoveUtil.addmotX(xt * bstn);
                        MoveUtil.addmotZ(zt * bstn);
                    } else if(!usingitem){

                        MoveUtil.limit2speed(0.199);
                        MoveUtil.smartstrafe();
                    }

                }else if(!usingitem) {

                    if (MoveUtil.getspeed2() > 0.2 && MoveUtil.getspeed2() < 0.2495
                    ) {
                        MoveUtil.maximize2speed(0.2495, 1.0015);

                    }


                }


            }
        }




        //Сам в шоке от условий
        if (mc.player.getDeltaMovement().y>-0.5 && (!mc.player.verticalCollision || mc.player.onGround()) && !ACUtil.matrixisusingitem() && pc.lastVeltimer.hasTimeElapsed(3000,false) && pc.lastC02timer.hasTimeElapsed(3000,false) && !Client.instance.featureManager.getModuleByClass(AirStuck.class).getState() && (bl instanceof AirBlock)
        ) {
            MoveUtil.addmotY(-0.003);
        }
        index2=mc.player.verticalCollision ? 1 : 0;

        index1++;

        return e;
    }


    private static void sjump(){
   jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }
}
