package base.client.feature.impl.movement.speeds.other;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;

import static base.client.feature.impl.movement.Speed.*;
import static base.client.helpers.Helper.mc;

public class OtherSimpleHopSpeed {


    public static EventOnJump onEventOnJump(EventOnJump e) {
        if (!jumping) {    e.cancel();  }
        return e;
    }

    public static void onEventOnMovePost(double xt,double zt) {


     if(mc.player.onGround() && mc.player.verticalCollision) {
sjump();
     if(VerGroundModify.isEnabled()){
         switch (HopVMode.getCurrentMode()) {
             case ("Boost"):
                 MoveUtil.addmotY(HopVBst.getValue());
                 break;
             case ("Mult"):
                 MoveUtil.setmotY(mc.player.getDeltaMovement().y * HopVMult.getValue());
                 break;
         }

     }


         if(HorGroundModify.isEnabled()){
             switch (HopHMode.getCurrentMode()) {
                 case ("Strafe"):
                     MoveUtil.smartstrafe(HopStrafe.getValue());
                     break;
                 case ("MotionBoost"):
                     MoveUtil.addmotXZ(HopBst.getValue() * xt, HopBst.getValue() * zt);
                     break;
                 case ("MotionMult"):
                     MoveUtil.mult2ds(HopMult.getValue());
                     break;
             }

         }

     } else {

         switch (HopHMode.getCurrentMode()) {
             case ("Strafe"):
                 MoveUtil.smartstrafe(HopStrafe.getValue());
                 break;
             case ("MotionBoost"):
                 MoveUtil.addmotXZ(HopBst.getValue() * xt, HopBst.getValue() * zt);
                 break;
             case ("MotionMult"):
                 MoveUtil.mult2ds(HopMult.getValue());
                 break;
         }

         switch (HopVMode.getCurrentMode()) {
             case ("Boost"):
                 MoveUtil.addmotY(HopVBst.getValue());
                 break;
             case ("Mult"):
                 MoveUtil.setmotY(mc.player.getDeltaMovement().y * HopVMult.getValue());
                 break;
         }

     }




    }



}
