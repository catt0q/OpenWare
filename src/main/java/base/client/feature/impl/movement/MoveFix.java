package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMove;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import net.minecraft.util.Mth;
public class MoveFix extends Module {
    boolean lastpacketyaw = false;
    public static boolean needspoofyaw = false;
    public static float RealYaw = 0;
    public static boolean lastcansprint = false;
//Self==Silent Target==Idk
    public static ModeSetting MoveMode = new ModeSetting("Type", "Self", () -> true, "Target", "Self");
    public static ModeSetting SMode = new ModeSetting("Sprint Mode", "Correct", () -> true, new String[]{"Correct", "AllDir", "NoSprint"});

    public MoveFix() {
        super("MoveFix", "Фиксит да", Type.Movement);

        this.addSettings(MoveMode, SMode );
    }

    @EventTarget
    public void onEnable() {
        if (mc.player != null) {
            RealYaw = mc.player.getYRot();
        }
        needspoofyaw = false;
        lastcansprint = false;
        lastpacketyaw = false;
        super.onEnable();
    }

    @EventTarget
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void eoj(EventOnJump e) {
        if(needspoofyaw){
            e.setYaw(MoveFix.getfixedyaw(e.getYaw()));
        }

    }
    @EventTarget
    public void eom(EventOnMove e) {
        if(needspoofyaw) {
            e.setYaw(MoveFix.getfixedyaw(e.getYaw()));
        }
    }
    @EventTarget
    public void emi(EventMoveInput e) {
        if(needspoofyaw){
            int states = calcmovementinput();
            boolean mfda=e.getfor()!=0;
            boolean msda=e.getstrafe()!=0;
 

            if(mfda || msda) {
                if(states==10 ) { e.forward = false; e.backward = true; e.left = false; e.right = false;   }
                if(states==0 ) { 	 e.forward = true; e.backward = false; e.left = false; e.right = false; }

                if(states==-1 ) { 	 e.forward = true; e.backward = false; e.left = false; e.right = true;   }
                if(states==1 ) { 	 e.forward = true; e.backward = false; e.left = true; e.right = false;  }

                if(states==-2 ) { 	e.forward = false; e.backward = false; e.left = false; e.right = true;  }
                if(states==2 ) { 	e.forward = false; e.backward = false; e.left = true; e.right = false;  }

                if(states==-3 ) { e.forward = false; e.backward = true; e.left = false; e.right = true;   }
                if(states==3 ) { 	e.forward = false; e.backward = true; e.left = true; e.right = false;  }

            }else {
                e.forward = false; e.backward = false; e.left = false; e.right = false;
            }
        }

    }

    public static int calcmovementinput() {
        int states = 0;
        float startfloat = MoveUtil.getdir();
        if (MoveUtil.getdir() == -1) {
            startfloat = (mc.player.getYRot());
        }
        if(MoveFix.MoveMode.getCurrentMode().equals("Target")){
            startfloat=MoveFix.RealYaw;
        }


        boolean nextcansprint = true;


        float bestdir = Mth.wrapDegrees(MoveFix.RealYaw);

        float ryaw = Mth.wrapDegrees(MoveFix.RealYaw);

        float val = 45;
        float result1 = RotationUtils.addyaw(ryaw, val);
        float result2 = RotationUtils.yawdiff(startfloat, result1);
        double bestdiff = 9999;
        if (result2 < bestdiff) {
            bestdiff = result2;
            bestdir = result1;
            nextcansprint = true;
            states = 1;
        }
        val = -45;
        result1 = RotationUtils.addyaw(ryaw, val);
        result2 = RotationUtils.yawdiff(startfloat, result1);
        if (result2 < bestdiff) {
            bestdiff = result2;
            bestdir = result1;
            nextcansprint = true;
            states = -1;
        }
        val = 90;
        result1 = RotationUtils.addyaw(ryaw, val);
        result2 = RotationUtils.yawdiff(startfloat, result1);
        if (result2 < bestdiff) {
            bestdiff = result2;
            bestdir = result1;
            nextcansprint = false;
            states = 2;
        }
        val = 135;
        result1 = RotationUtils.addyaw(ryaw, val);
        result2 = RotationUtils.yawdiff(startfloat, result1);
        if (result2 < bestdiff) {
            bestdiff = result2;
            bestdir = result1;
            nextcansprint = false;
            states = 3;
        }
        val = -90;
        result1 = RotationUtils.addyaw(ryaw, val);
        result2 = RotationUtils.yawdiff(startfloat, result1);
        if (result2 < bestdiff) {
            bestdiff = result2;
            bestdir = result1;
            nextcansprint = false;
            states = -2;
        }
        val = -135;
        result1 = RotationUtils.addyaw(ryaw, val);
        result2 = RotationUtils.yawdiff(startfloat, result1);
        if (result2 < bestdiff) {
            bestdiff = result2;
            bestdir = result1;
            nextcansprint = false;
            states = -3;
        }
        val = 0;
        result1 = RotationUtils.addyaw(ryaw, val);
        result2 = RotationUtils.yawdiff(startfloat, result1);
        if (result2 < bestdiff) {
            bestdiff = result2;
            bestdir = result1;
            nextcansprint = true;
            states = 0;
        }
        val = -180;
        result1 = RotationUtils.addyaw(ryaw, val);
        result2 = RotationUtils.yawdiff(startfloat, result1);
        if (result2 < bestdiff) {
            bestdiff = result2;
            bestdir = result1;
            nextcansprint = false;
            states = 10;
        }


        if (SMode.getCurrentMode().equals("NoSprint")) {
            MoveFix.lastcansprint = false;
        } else if (SMode.getCurrentMode().equals("Correct")) {
            if (!nextcansprint && mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
            MoveFix.lastcansprint = nextcansprint;
        } else if (SMode.getCurrentMode().equals("AllDir")) {
            MoveFix.lastcansprint = true;
        }
        return states;
    }


    public static float getfixedyaw(float prevyaw) {
        if (!Client.instance.featureManager.getModuleByClass(MoveFix.class).getState()) {
            return prevyaw;
        }
        if (MoveFix.needspoofyaw) {
            return MoveFix.RealYaw;
        }
        return prevyaw;
    }
}