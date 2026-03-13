package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.KillAura;
import base.client.feature.impl.combat.KillAuraNew;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import base.client.feature.Module;
public class MovementCorrection extends Module {

    private final KillAuraNew killAura;

    public MovementCorrection(KillAuraNew killAura) {
        super("MovementCorrection", "Исправляет твое движение чтобы античиты не флагали", Type.Movement);
        addSettings();
        this.killAura = killAura;
    }

    @EventTarget
    public void onMoveFix(EventMoveInput e) {
        PacketHelper.Values pc = Client.instance.packet;

        float closestDiff = Float.MAX_VALUE;

        float fmf = e.getfor();
        float fms = e.getstrafe();

        if (fmf == 0 && fms == 0) {
            return;
        }

        float playerDirection = getNeedDirection(fmf, fms);

        for (float movementForward = -1; movementForward <= 1; movementForward += 1f) {
            for (float movementSideways = -1; movementSideways <= 1; movementSideways += 1f) {
                if (movementForward == 0 && movementSideways == 0) {
                    continue;
                }

                float predictDirection = MoveUtil.getDirection(pc.LastYaw, movementForward, movementSideways);
                float diff = Math.abs(playerDirection - predictDirection);

                if (diff < closestDiff) {
                    closestDiff = diff;
                    fmf = movementForward;
                    fms = movementSideways;
                }
            }
        }

        if (fmf <= 0) {
            mc.player.setSprinting(false);
        }

        e.forward = fmf > 0;
        e.backward = fmf < 0;

        e.left = fms < 0;
        e.right = fms > 0;
    }

    private float getNeedDirection(float movementForward, float movementSideways) {
        TargetStrafe targetStrafe = (TargetStrafe) Client.instance.featureManager.getModuleByClass(TargetStrafe.class);

        if (killAura.getState() && killAura.getTarget() != null && targetStrafe.getState()) {
            return RotationUtils.getRotationToEntity(killAura.getTarget()).getX();
        }

        return MoveUtil.getDirection(mc.player.getYRot(), movementForward, movementSideways);
    }

}
