package base.client.feature.impl.movement;

import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.KillAura;
import base.client.feature.impl.combat.KillAuraNew;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
public class ElytraSpeed extends Module {

    private final KillAuraNew killAura;

    public ElytraSpeed(KillAuraNew killAura) {
        super("ElytraSpeed", "ускоряет вас на элитрах", Type.Movement);
        this.killAura = killAura;
    }

    @EventTarget
    public void omp(EventTick e) {
//        if (mc.player.isGliding()) {

            double yawt = Math.toRadians(MoveUtil.getdir());

            if(killAura.getState()&&killAura.lasttarget!=null&&!mc.player.onGround())yawt = Math.toRadians(RotationUtils.getRotationToEntity(killAura.getTarget()).getX());

            double xt = -Math.sin(yawt);    double zt = Math.cos(yawt);
        MoveUtil.strafe(0.6);
           MoveUtil.setmotY(-0.05);

//            if (mc.player.input.getMoveVector().x == 0 && mc.player.input.getMoveVector().y == 0) {
//                MoveUtil.stop3();
//            } else {
//                MoveUtil.setmotX(xt * 2);
//                MoveUtil.setmotZ(zt * 2);
//                MoveUtil.setmotY(0);
//            }
//            if (MoveUtil.motYstate() == -1) {
//                MoveUtil.setmotY(-2);
//            } else if (MoveUtil.motYstate() == 1) {
//                MoveUtil.setmotY(2);
//            }
//        }
    }
}
