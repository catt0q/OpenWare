package base.client.feature.impl.movement;

import base.client.event.EventTarget;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMove;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.MoveUtil;

public class ElytraPlus extends Module {

    public static ModeSetting Mode, Version;

    public static NumberSetting Factor;

    public ElytraPlus() {
        super("Elytra+", "Летать на элитрах быстро", Type.Movement);

        Mode = new ModeSetting("Mode", "Grim", () -> true, "Grim");
        Version = new ModeSetting("Version", "1.17+", () -> Mode.getCurrentMode().equals("Grim"), "1.16.5-", "1.17+");

          Factor = new NumberSetting("Factor", 0.9F, 0.001F,
                  1F, 0.001F,()->(Mode.getCurrentMode().equals("Grim")));


        this.addSettings(Mode, Version,Factor);
    }

    @EventTarget
    public void onEnable() {

        super.onEnable();
    }

    @EventTarget
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void eoj(EventOnJump e) {


    }
    @EventTarget
    public void eom(EventOnMove e) {

    }
    @EventTarget
    public void emi(EventMoveInput e) {


    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getmovedir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);




switch (Mode.getCurrentMode()){
    case ("Grim"):
        if(!mc.player.isFallFlying() || MoveUtil.getmovedir()==-1){
            return;
        }
        if(MoveUtil.motYstate()<=0){
            return;
        }

        double d=Factor.getValue()/10;
        final double d4 = d * Math.cos(Math.toRadians(mc.player.getYRot() + 90.0f));
        final double d5 = d * Math.sin(Math.toRadians(mc.player.getYRot() + 90.0f));

        EntityUtil.dtp(d4,0,d5);
        e.setPosX(mc.player.getX());
        e.setPosY(mc.player.getY());
        e.setPosZ(mc.player.getZ());

        if(Version.getCurrentMode().equals("1.16.5-"))
        MoveUtil.addmotXZ(d4,d5);






        break;

}



    }


}