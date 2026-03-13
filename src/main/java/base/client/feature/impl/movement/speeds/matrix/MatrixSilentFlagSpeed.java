package base.client.feature.impl.movement.speeds.matrix;

import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.feature.impl.movement.Speed;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;

import static base.client.helpers.Helper.mc;

public class MatrixSilentFlagSpeed {

    static int index1,index2,groundstate;

    public static void onenable(){
        index1=0;
        index2=-1;
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

        double nmult=1.9;   double nboost2=1.19;
        TimerUtil.setTimerspeed(0.2);
        if (index1 % 2 == 0) {
            double prevmotY = mc.player.getDeltaMovement().y();
            index2++;
            mc.player.jumpFromGround();
            MoveUtil.setmotY(prevmotY);
            if (index2 > 1) {
                MoveUtil.mult2ds(nboost2);
            }
            MoveUtil.limit2speed(9.25);
            e.setOnGround(true);

            groundstate = 2;   } else {
            MoveUtil.mult2ds(nmult);
            MoveUtil.limit2speed(9.25);

            e.setOnGround(false);
            groundstate = 1;

        }


        index1++;
        return e;
    }


    public static EventSendPacketBlink onEventSendPacketBlink(EventSendPacketBlink e) {
        if(mc.player!=null) {
            Speed.BlinkPackets.add(e.getPacket());
            e.cancel();
        }
        return e;
    }


}
