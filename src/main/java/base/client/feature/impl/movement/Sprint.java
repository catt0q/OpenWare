package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.input.EventSprint;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.MoreKB;
import base.client.feature.impl.exploit.TeleportBack;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import com.mojang.blaze3d.platform.InputConstants;

public class Sprint extends Module {
    public static ModeSetting Mode,AllDirCondition;
    public static BooleanSetting IgnoreFoodLevel,IgnoreBlind;
    public Sprint() {
        super("Sprint", "Позволяет контролировать спринт", Type.Movement);
        Mode = new ModeSetting("Mode", "Vanilla", () -> true, "NoSprint", "AllDir", "Vanilla", "Legit");
        AllDirCondition= new ModeSetting("AllDir Condition", "Always", () -> true, "Only Ground", "Only Air", "Always");

        IgnoreFoodLevel = new BooleanSetting("Ignore Food Level", false, () -> !Mode.getCurrentMode().equals("NoSprint"));
        IgnoreBlind = new BooleanSetting("Ignore Blind", false, () -> !Mode.getCurrentMode().equals("NoSprint"));


        addSettings(Mode,AllDirCondition,IgnoreFoodLevel,IgnoreBlind

        );
    }



    @EventTarget
    public void onEMI(EventMoveInput e) {

        switch (Mode.getCurrentMode()){
            case ("Legit"),("Vanilla"),("AllDir"):

                if(e.isForward() && !e.isBackward() && !e.isSneak() && !MoreKB.issprintblocked) {
                    e.setSprint(true);
                }


                break;


        }


    }


    @EventTarget
    public void onSprint(EventSprint e) {
        if(Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState()){
            return;
        }


        if(Mode.getCurrentMode().equals("NoSprint")) {
            e.setSprint(false);
          if(!mc.player.isSprinting()){
e.cancel();
          }

        }else    if(Mode.getCurrentMode().equals("AllDir")) {
            if (!mc.player.isCrouching()  && acceptAllDir()
            ) {
                e.setSprint(true);
            }
            if(mc.player.isSprinting()==e.isSprint()){
e.cancel();
            }
    }else    if(Mode.getCurrentMode().equals("Vanilla")) {
            if (!mc.player.isCrouching()  && MoveUtil.getmf()>0
            ) {
                e.setSprint(true);
            }
            if(mc.player.isSprinting()==e.isSprint()){
                e.cancel();
            }
        }

    }


    @EventTarget
    public void onUpdate(EventPreMotion e) {

        if(Mode.getCurrentMode().equals("NoSprint")) {
            mc.player.setSprinting(false);
        }else    if(Mode.getCurrentMode().equals("AllDir")) {
            if (!mc.player.isCrouching()  && acceptAllDir()
            ) {
                mc.player.setSprinting(true);
            }else{
                mc.player.setSprinting(false);
            }

 } else if(Mode.getCurrentMode().equals("Vanilla")) {
            if (!mc.player.isCrouching()  && MoveUtil.getmf()>0
            ) {
                mc.player.setSprinting(true);
            }else{
                mc.player.setSprinting(false);
            }

        }


    }

    @EventTarget
    public void onTick(EventTick e) {



    }

    public static boolean canusesprint(){
        if(Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState() && TeleportBack.Mode.getCurrentMode().equals("Matrix")){
            return false;
        }
        if(!Client.instance.featureManager.getModuleByClass(Sprint.class).getState()){
            return (MoveUtil.getmf()>0);
        }
if(Mode.getCurrentMode().equals("NoSprint")){
    return false;
}
        if(Mode.getCurrentMode().equals("AllDir")){
            return acceptAllDir();
        }

 return (MoveUtil.getmf()>0);
 }

    public static boolean acceptAllDir(){
        if(Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState() && TeleportBack.Mode.getCurrentMode().equals("Matrix")){
            return false;
        }
        PacketHelper.Values pc = Client.instance.packet;
        if(AllDirCondition.getCurrentMode().equals("Only Ground") && !pc.LastGround) { 	return (MoveUtil.getmf()>0); 	}
        else if(AllDirCondition.getCurrentMode().equals("Only Air") && pc.LastGround) { return (MoveUtil.getmf()>0); 	}

        return MoveUtil.ismovingmfms() && Sprint.Mode.getCurrentMode().equals("AllDir");
    }

    public static boolean canignoreFood(){
        if(!Client.instance.featureManager.getModuleByClass(Sprint.class).getState()){
            return false;
        }

        return IgnoreFoodLevel.getState();
    }
    public static boolean canignoreBlind(){
        if(!Client.instance.featureManager.getModuleByClass(Sprint.class).getState()){
            return false;
        }

        return IgnoreBlind.getState();
    }


}
