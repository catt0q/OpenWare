package base.client.feature.impl.movement.speeds.matrix;

import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.impl.movement.Step;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;

import static base.client.helpers.Helper.mc;

public class MatrixGroundSpeed {
    static int index1,index2;
static int groundstate;
    public static void onenable(){
          groundstate=0;
        index1=0;index2=0;
    }
    public static EventOnJump onEventOnJump(EventOnJump e) {
          e.cancel();
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
        if(!mc.player.verticalCollision || !ACUtil.ismatrixonground()) {
            index1=-1;index2=0; return;
        }   if(mc.player.isSwimming()){  return;    }
        if(index1==0) {
            MoveUtil.smartstrafe(0.2499);
        }
        if(index1==1) {
            MoveUtil.smartstrafe(0.3579);
        }
        if(index1==2) {MoveUtil.smartstrafe(0.3249);
            index1=-1;
            index2++;
        }		 	  index1++;

        return;
    }
}
