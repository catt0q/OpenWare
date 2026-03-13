package base.client.feature.impl.movement.speeds.matrix;

import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.impl.movement.Step;
import base.client.helpers.utils.MoveUtil;

import static base.client.helpers.Helper.mc;

public class MatrixLowHopSpeed {
    static int index1,index2,groundstate;
static boolean jumping;
    public static void onenable(){
          groundstate=0;jumping=false;
        index1=0;index2=0;
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


    public static EventPreMotion onEventPreMotion(EventPreMotion e) {
        if(index1==0) {
            groundstate=1;	 e.setOnGround(false);
        }
        if(index1==1) {
            groundstate=2;		 	e.setOnGround(true);
        }
        if(index1==2) {
            groundstate=2;			 e.setOnGround(true);
        }
        return e;
    }


    public static void onEventOnMovePost(EventOnMovePost e, double xt, double zt) {
        if(Step.isstepping()){
            index1=0; return;
        }

        if(mc.player.isSwimming()){  return;    }
        if (mc.player.onGround()) {
            sjump();   index2++;
        }
        if(index1==1 && index2>=3){
            MoveUtil.setmotY(0); index2=0;
        }
        index1++;
    }
    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }
}
