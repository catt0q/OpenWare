package base.client.feature.impl.movement.speeds.matrix;

import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.impl.movement.Step;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;

import static base.client.helpers.Helper.mc;

public class MatrixYPortSpeed {

    static boolean jumping;
    static int index1;

    public static void onenable(){
        jumping=false;
        index1=0;
    }
    public static EventOnJump onEventOnJump(EventOnJump e) {
        if (!jumping) {    e.cancel();  }
        return e;
    }

    public static EventOnMovePost onEventOnMovePost(EventOnMovePost e, double xt, double zt) {

        if(Step.isstepping()){
            index1=0; return e;
        }
        if(mc.player.isSwimming()){  return e;    }
        if (mc.player.onGround()) {
            sjump();
            MoveUtil.smartstrafe();
        } else {
            if(!mc.player.isUsingItem() && !mc.player.isShiftKeyDown()) {
                if(index1==1){
                    MoveUtil.minsmartstrafe(0.2499);
                }

                if (index1 > 1 && index1 <5) {
                    if (ACUtil.matrixboost2()) {
                    } else if (MoveUtil.getspeed2() > 0.2 && MoveUtil.getspeed2() < 0.25 && MoveUtil.getdir() != -1 && RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) < 22) {
                        double prevmot = MoveUtil.getspeed2();
                        MoveUtil.maximize2speed(0.2495, 1.0015); // ChatHelper.addChatMessage(prevmot+" "+MoveUtil.getspeed2());
                    } else if (MoveUtil.getspeed2() < 0.3 && MoveUtil.getspeed2() > 0.2 && MoveUtil.getdir() != -1 && RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 44) {
                        if (RotationUtils.yawdiff(MoveUtil.getdir(), MoveUtil.getmovedir()) > 130) {
                            double bstn = 0.1999;
                            if(MoveUtil.isdiag()) bstn=0;
                            MoveUtil.addmotX(xt * bstn);
                            MoveUtil.addmotZ(zt * bstn);
                        } else {
                            MoveUtil.limit2speed(0.1999);
                            MoveUtil.smartstrafe();
                        }

                    }
                }
                if (index1 == 1) {
                    MoveUtil.smartstrafe();
                } else if (index1 == 5) {
                    MoveUtil.limit2speed(0.08);
                    MoveUtil.setmotY(-0.95);
                    MoveUtil.smartstrafe();
                } else if (index1 == 6) {
                    MoveUtil.smartstrafe(0.2499);
                }
            }

        }
        index1++;

        return e;
    }


    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }
}
