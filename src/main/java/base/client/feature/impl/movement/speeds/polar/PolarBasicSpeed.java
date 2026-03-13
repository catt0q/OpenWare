package base.client.feature.impl.movement.speeds.polar;

import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.helpers.utils.MoveUtil;

import static base.client.helpers.Helper.mc;

public class PolarBasicSpeed {

    static boolean jumping;
    static int index1,index2;

    public static void onenable(){
        jumping=false;
        index1=0;
        index2=-1;
    }


    public static EventOnMovePost onEventOnMovePost(EventOnMovePost e, double xt, double zt) {

        if (mc.player.isUsingItem() || mc.player.isSwimming() || mc.player.isShiftKeyDown()) return e;
        double bstn = 0.00249;
        MoveUtil.addmotXZ(xt * bstn,zt * bstn);

        index1++;
        return e;
    }






}
