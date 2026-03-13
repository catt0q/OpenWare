package base.client.helpers.utils.scaffold;

import base.client.event.events.impl.motion.EventPreMotion;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * handles anticheat bypass modes for scaffold
 */
public class ScaffoldBypass {
    private static final Minecraft mc = Minecraft.getInstance();

    public enum SprintMode {
        NORMAL, DISABLED, LEGIT, BYPASS, VULCAN, MATRIX, NCP, GRIM, INTAVE
    }

    public enum TowerMode {
        DISABLED, VANILLA, NCP, MATRIX, VULCAN
    }

    /**
     * handle sprint mode specific bypass logic
     */
    public static void handleSprintBypass(SprintMode mode, int airTicks, boolean motionBoost) {
        if (mc.player == null) return;

        switch (mode) {
            case BYPASS -> {
                if (airTicks == 1) MoveUtil.strafe();
            }
            case NCP -> {
                if (motionBoost && (airTicks == 1 || airTicks == 4)) {
                    MoveUtil.strafe();
                }
            }
            case GRIM -> {
                if (motionBoost && mc.player.onGround() && mc.player.fallDistance > 2) {
                    double boost = ScaffoldUtils.isCardinal() ? 1.1225 : 1.129;
                    Vec3 mot = mc.player.getDeltaMovement();
                    mc.player.setDeltaMovement(mot.x * boost, mot.y, mot.z * boost);
                    MoveUtil.strafe();
                }
                if (airTicks == 1) MoveUtil.strafe();
            }
            case MATRIX -> {
                if (mc.player.onGround()) MoveUtil.strafe();
            }
            case VULCAN -> {
                if (mc.options.keyShift.isDown()) mc.player.setSprinting(false);
            }
            case INTAVE -> {
                if (mc.player.onGround()) {
                    MoveUtil.strafe();
                    Vec3 mot = mc.player.getDeltaMovement();
                    mc.player.setDeltaMovement(mot.x * 0.998, mot.y, mot.z * 0.998);
                }
            }
            default -> {}
        }
    }

    /**
     * handle tower mode logic
     */
    public static void handleTower(TowerMode mode, int airTicks, EventPreMotion event) {
        if (mc.player == null) return;
        if (!mc.options.keyJump.isDown()) return;
        if (MoveUtil.ismovinginput()) return;

        switch (mode) {
            case VANILLA -> {
                if (mc.player.onGround()) mc.player.jumpFromGround();
            }
            case NCP -> {
                if (mc.player.onGround()) mc.player.jumpFromGround();
                if (airTicks == 4) MoveUtil.addmotY(0.098);
            }
            case MATRIX -> {
                if (ACUtil.ismatrixonground() && event != null) {
                    event.setOnGround(true);
                    MoveUtil.setmotY(0.42);
                }
            }
            case VULCAN -> {
                if (mc.player.onGround()) mc.player.jumpFromGround();
            }
            default -> {}
        }
    }

    /**
     * check if sprint should be disabled
     */
    public static boolean shouldDisableSprint(SprintMode mode, int airTicks) {
        return switch (mode) {
            case DISABLED -> true;
            case LEGIT -> airTicks > 0;
            default -> false;
        };
    }

    /**
     * convert string to SprintMode enum
     */
    public static SprintMode parseSprintMode(String mode) {
        return switch (mode) {
            case "Disabled" -> SprintMode.DISABLED;
            case "Legit" -> SprintMode.LEGIT;
            case "Bypass" -> SprintMode.BYPASS;
            case "Vulcan" -> SprintMode.VULCAN;
            case "Matrix" -> SprintMode.MATRIX;
            case "NCP" -> SprintMode.NCP;
            case "Grim" -> SprintMode.GRIM;
            case "Intave" -> SprintMode.INTAVE;
            default -> SprintMode.NORMAL;
        };
    }

    /**
     * convert string to TowerMode enum
     */
    public static TowerMode parseTowerMode(String mode) {
        return switch (mode) {
            case "Vanilla" -> TowerMode.VANILLA;
            case "NCP" -> TowerMode.NCP;
            case "Matrix" -> TowerMode.MATRIX;
            case "Vulcan" -> TowerMode.VULCAN;
            default -> TowerMode.DISABLED;
        };
    }
}
