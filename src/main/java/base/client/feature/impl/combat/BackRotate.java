package base.client.feature.impl.combat;

import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventLook;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMove;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.rotation.Rotation;
import base.client.helpers.utils.*;
import net.minecraft.util.Mth;

import static base.client.feature.impl.player.Breaker.lastpitch;
import static base.client.helpers.utils.RotationUtils.lastyaw;

public class BackRotate extends Module{

    float lastYaw1, lastPitch1;
    private final KillAuraNew killAura;
    boolean wasRotate;
    public NumberSetting speed = new NumberSetting("Speed",10,1,180,1,()->true);

    public BackRotate(KillAuraNew killAura){
        super("BackRotate","После работы киллауры поворачивается плавно обратно", Type.Combat);
        this.addSettings(speed);
        this.killAura = killAura;
    }
    @Override
    public void onEnable() {
      super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
    @EventTarget
    public void onTick(EventTick e) {
        if (killAura.getState() && killAura.lasttarget != null) {
            wasRotate = true;
            lastYaw1 =  lastyaw;
            lastPitch1 = lastpitch;
        } else {
            if (wasRotate) {
                Rotation target = new Rotation(mc.player.getYRot(), mc.player.getXRot());
                float yawDelta = Mth.wrapDegrees(target.getX() - lastYaw1);
                float pitchDelta = Mth.wrapDegrees(target.getY() - lastPitch1);
                float rotdiff = (float) Math.hypot(yawDelta, pitchDelta);
                if (rotdiff < 0.5f) {
                    wasRotate = false;
                    return;
                }
                yawDelta = Math.clamp(yawDelta, -speed.getValue(), speed.getValue());
                pitchDelta = Math.clamp(pitchDelta, -speed.getValue(), speed.getValue());

                float gcd = RotationUtils.getMouseGCD();

                yawDelta = Math.round(yawDelta / gcd) * gcd;
                pitchDelta = Math.round(pitchDelta / gcd) * gcd;

                lastYaw1 += yawDelta;
                lastPitch1 = Mth.clamp(lastPitch1 + pitchDelta, -90, 90);

                lastyaw = lastYaw1;
                lastpitch = lastPitch1;
            } else {
                lastYaw1 = mc.player.getYRot();
                lastPitch1 = mc.player.getXRot();
                lastyaw = lastYaw1;
                lastpitch = lastPitch1;
            }
        }
    }

    @EventTarget
    public void onMotion(EventPreMotion e) {
        if (killAura.getState() && killAura.lasttarget != null) {
        } else if (wasRotate) {
            e.setYaw(lastYaw1);
            e.setPitch(lastPitch1);
        }
    }

    @EventTarget
    public void onLook(EventLook e) {
        if (killAura.getState() && killAura.lasttarget != null) {
        } else if (wasRotate) {
            e.setYaw(lastYaw1);
            e.setPitch(lastPitch1);
        }
    }

    @EventTarget
    public void onMoveFix(EventOnMove e) {
        if (killAura.getState() && killAura.lasttarget != null) {
        } else if (wasRotate) {
            e.setYaw(lastYaw1);
        }
    }

    @EventTarget
    public void onJump(EventOnJump e) {
        if (killAura.getState() && killAura.lasttarget != null) {
        } else if (wasRotate) {
            e.setYaw(lastYaw1);
        }
    }
}
