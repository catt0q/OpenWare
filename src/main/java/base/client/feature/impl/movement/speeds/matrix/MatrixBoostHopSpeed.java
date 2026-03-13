package base.client.feature.impl.movement.speeds.matrix;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;

import static base.client.helpers.Helper.mc;

public class MatrixBoostHopSpeed {

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

    public static EventOnMovePost onEventOnMovePost(EventOnMovePost e, double xt, double zt) {
        PacketHelper.Values pc = Client.instance.packet;
       if(mc.player.isSwimming()){  return e;    }
        if (mc.player.onGround()) {
            sjump();

            if(!mc.player.isUsingItem() && !mc.player.isShiftKeyDown()) {
                if (MoveUtil.getspeed2() > 3.1 && MoveUtil.getspeed2() < 3.3) {
                    MoveUtil.minsmartstrafe(0.395);
                } else if (MoveUtil.getspeed2() < 0.2) {
                    MoveUtil.minsmartstrafe(0.3);
                }
            }
        } else {
            if(!mc.player.isUsingItem() && !mc.player.isShiftKeyDown()) {


                if( ACUtil.ismatrixonground() && (!mc.player.onGround() && !(mc.level.getBlockState(new BlockPos((int) (mc.player.getX()+mc.player.getDeltaMovement().x()), (int) (mc.player.getY()+mc.player.getDeltaMovement().y()), (int) (mc.player.getZ()+mc.player.getDeltaMovement().z()))).getBlock() instanceof AirBlock)
                        && MoveUtil.getspeed2() > 0.2 )
                ) {     MoveUtil.mult2ds(1.999);
                }else   if (MoveUtil.getspeed2() < 0.2) {
                    MoveUtil.minsmartstrafe(0.1999);
                } else if (MoveUtil.getspeed2() > 0.2 && MoveUtil.getspeed2() < 0.25 && MoveUtil.getdir() != -1 && RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) < 22) {
                    double prevmot = MoveUtil.getspeed2();
                    MoveUtil.maximize2speed(0.2495, 1.0015); // ChatHelper.addChatMessage(prevmot+" "+MoveUtil.getspeed2());
                } else if (MoveUtil.getspeed2() < 0.3 && MoveUtil.getspeed2() > 0.2 && MoveUtil.getdir() != -1 && RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 44) {
                    if (RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 130) {
                        double bstn = 0.1999;
                        if (MoveUtil.isdiag()) bstn = 0;
                        MoveUtil.addmotX(xt * bstn);
                        MoveUtil.addmotZ(zt * bstn);
                    } else {
                        MoveUtil.limit2speed(0.1999);
                        MoveUtil.smartstrafe();
                    }

                }
            }
        }
        if ((!mc.player.verticalCollision || mc.player.onGround()) ) MoveUtil.addmotY(-0.003);

        index1++;
        return e;
    }


    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }

}
