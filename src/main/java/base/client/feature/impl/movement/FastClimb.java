package base.client.feature.impl.movement;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
public class FastClimb extends Module {

    public static boolean strafefastladdermatrixexempt=false;

    public static ModeSetting ladderMode;
    public static NumberSetting ladderSpeed;
    public static NumberSetting ladderTimer;
    public static NumberSetting ladderMatrixDelay;
    boolean wasclimb=false;
    int ticks=0;
    public FastClimb() {
        super("FastClimb", "Позволяет быстро забираться по лестницам и лианам", Type.Movement);
        ladderMode = new ModeSetting("Mode", "Matrix", () -> true, "Matrix","Timer", "Vanilla", "Grim","Funtime");
        ladderSpeed = new NumberSetting("Ladder Speed", 0.5F, 0.01F, 1F, 0.01F, () -> ladderMode.currentMode.equals("Vanilla"));
        ladderTimer = new NumberSetting("Ladder Timer", 1.5F, 1F, 10F, 0.01F, () -> ladderMode.currentMode.equals("Timer"));
        ladderMatrixDelay = new NumberSetting("Ladder Matrix Delay", 11, 2, 40, 1, () -> ladderMode.currentMode.equals("Matrix"));

        addSettings(ladderMode,ladderSpeed,ladderTimer,ladderMatrixDelay);
    }

    @Override
    public void onEnable() {wasclimb=false;ticks=0;strafefastladdermatrixexempt=false;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }

    @EventTarget
    public void onPreMotion(EventOnMovePost event) {
        this.setSuffix(ladderMode.getCurrentMode());
        if (mc.player == null || mc.level == null)
            return;
        switch (ladderMode.getCurrentMode()) {
            case "Timer":
                if (mc.player.onClimbable() && mc.player.horizontalCollision && MoveUtil.getms()==0 && MoveUtil.getmf()!=0) {
                    TimerUtil.setTimerspeed(ladderTimer.getValue());
                    wasclimb=true;
                }else {
                    if(wasclimb) {TimerUtil.setTimerspeed(1);}
                    wasclimb=false;
                }

                break;
            case "Grim":
                if (mc.player.onClimbable() && mc.player.horizontalCollision) {
 //Grim Exempt Moment

                    if(mc.player.onGround()){
                        MoveUtil.setmotY(0.42);
                    }

                    if(MoveUtil.getspeed2()<0.06){
                        MoveUtil.addmotY(0.0599);
                    }
                }
                break;
            case "Vanilla":
                if (mc.player.onClimbable() && mc.player.horizontalCollision && MoveUtil.ismovinginput()) {
                     MoveUtil.addmotY(ladderSpeed.getValue());
                }
                break;
            case "Funtime":
                if (mc.player.onClimbable() && mc.player.horizontalCollision && MoveUtil.ismovinginput()) {
                    MoveUtil.addmotY(0.09);
                }
                break;

        }
    }
    @EventTarget
    public void Move(EventOnMovePost eventMove) {
        this.setSuffix(ladderMode.getCurrentMode());
        if (mc.player == null || mc.level == null)
            return;
        switch (ladderMode.getCurrentMode()) {
            case "Matrix":
                if (mc.player.onClimbable() && MoveUtil.getms()==0 && MoveUtil.getmf()!=0) {
                    if(ticks==ladderMatrixDelay.getValue()) {
                        MoveUtil.stop3();
                        if(mc.player.horizontalCollision){
                        MoveUtil.smartstrafe(-0.13);
                            strafefastladdermatrixexempt=true;
                        }
                    }
                    if(ticks>=ladderMatrixDelay.getValue()+1) { ticks=0;
                        MoveUtil.stop3(); MoveUtil.setmotY(0.998);
                         MoveUtil.smartstrafe(-0.13);
                        strafefastladdermatrixexempt=true;
                    }

                    ticks++;
                }else {
                    ticks=0;
                }

                break;
        }
    }



}
