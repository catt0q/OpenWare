package base.client.helpers.utils.scaffold;

import base.client.Client;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * utility class for scaffold calculations
 */
public class ScaffoldUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * find target block position below player with motion prediction
     */
    public static BlockPos getTargetBlock(double sameY, boolean useSameY, int expandBlocks) {
        if (mc.player == null) return null;

        double multt = 1.2;
        double motY = mc.player.getDeltaMovement().y;
        if (mc.player.onGround()) {
            motY = MoveUtil.motYstate() > 0 ? 0.42 : 0;
        }
        
        double dPosY = mc.player.getY() - 1;
        double diffY = (motY <= 0) ? -0.4 : 0.01;
        int targetY = (int) Math.floor(dPosY + motY - diffY);

        // same Y handling
        if (useSameY && MoveUtil.ismovinginput()) {
            if (dPosY > sameY) {
                targetY = (int) Math.floor(sameY + motY - diffY);
            }
        }

        // expand
        double motX = mc.player.getDeltaMovement().x;
        double motZ = mc.player.getDeltaMovement().z;
        double expX = 0, expZ = 0;

        if (expandBlocks > 0 && MoveUtil.ismovinginput()) {
            double dir = MoveUtil.getDirection(mc.player.getYRot());
            for (int i = 1; i <= expandBlocks; i++) {
                double ox = -Math.sin(dir) * i;
                double oz = Math.cos(dir) * i;
                BlockPos test = new BlockPos(
                    (int) Math.floor(mc.player.getX() + ox),
                    targetY,
                    (int) Math.floor(mc.player.getZ() + oz)
                );
                if (isAir(test)) {
                    expX = ox;
                    expZ = oz;
                    break;
                }
            }
        }

        return new BlockPos(
            (int) Math.floor(mc.player.getX() + motX * multt + expX),
            targetY,
            (int) Math.floor(mc.player.getZ() + motZ * multt + expZ)
        );
    }

    /**
     * find a solid block to place against for the target air block
     * returns PlaceData with the block to click on and the face to click
     */
    public static PlaceData findPlacement(BlockPos target) {
        if (mc.level == null || target == null) return null;
        if (!isAir(target)) return null;

        // prioritize DOWN face (placing on block below)
        BlockPos below = target.below();
        if (!isAir(below)) {
            return new PlaceData(
                below,
                Direction.UP,
                new Vec3(below.getX() + 0.5, below.getY() + 1.0, below.getZ() + 0.5)
            );
        }

        // check horizontal faces
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos neighbor = target.relative(dir);
            if (!isAir(neighbor)) {
                Direction face = dir.getOpposite();
                return new PlaceData(
                    neighbor,
                    face,
                    new Vec3(
                        neighbor.getX() + 0.5 + face.getStepX() * 0.5,
                        neighbor.getY() + 0.5 + face.getStepY() * 0.5,
                        neighbor.getZ() + 0.5 + face.getStepZ() * 0.5
                    )
                );
            }
        }

        // check 1 block further (for gaps)
        for (Direction dir1 : Direction.values()) {
            BlockPos step1 = target.relative(dir1);
            if (!isAir(step1)) continue;
            for (Direction dir2 : Direction.values()) {
                BlockPos neighbor = step1.relative(dir2);
                if (!isAir(neighbor)) {
                    Direction face = dir2.getOpposite();
                    return new PlaceData(
                        neighbor,
                        face,
                        new Vec3(
                            neighbor.getX() + 0.5 + face.getStepX() * 0.5,
                            neighbor.getY() + 0.5 + face.getStepY() * 0.5,
                            neighbor.getZ() + 0.5 + face.getStepZ() * 0.5
                        )
                    );
                }
            }
        }

        return null;
    }

    /**
     * calculate rotation angles to look at a position
     */
    public static float[] calculateRotation(Vec3 target) {
        if (mc.player == null || target == null) return new float[]{0, 0};

        double eyeX = mc.player.getX();
        double eyeY = mc.player.getY() + mc.player.getEyeHeight();
        double eyeZ = mc.player.getZ();

        double dx = target.x - eyeX;
        double dy = target.y - eyeY;
        double dz = target.z - eyeZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        pitch = Mth.clamp(pitch, -90, 90);

        return new float[]{yaw, pitch};
    }

    /**
     * smooth rotation from current to target with speed limit
     */
    public static float[] smoothRotation(float currentYaw, float currentPitch, 
                                          float targetYaw, float targetPitch, 
                                          float speed) {
        float yawDiff = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float newYaw = currentYaw;
        float newPitch = currentPitch;

        if (Math.abs(yawDiff) > speed) {
            newYaw += Math.signum(yawDiff) * speed;
        } else {
            newYaw = targetYaw;
        }

        if (Math.abs(pitchDiff) > speed * 0.5f) {
            newPitch += Math.signum(pitchDiff) * speed * 0.5f;
        } else {
            newPitch = targetPitch;
        }

        return new float[]{newYaw, newPitch};
    }

    /**
     * apply GCD fix for anticheat bypass
     */
    public static float[] applyGCDFix(float prevYaw, float prevPitch, float newYaw, float newPitch) {
        float[] fixed = ACUtil.GCDFix(prevYaw, prevPitch, newYaw, newPitch);
        return new float[]{fixed[0], Mth.clamp(fixed[1], -90, 90)};
    }

    /**
     * update sameY level based on player movement
     */
    public static double updateSameY(double currentSameY) {
        if (mc.player == null) return currentSameY;

        double dPosY = mc.player.getY() - 1;
        
        if (currentSameY - dPosY > 0.6) {
            return dPosY;
        } else if (dPosY - currentSameY >= 1 && (mc.player.onGround() || dPosY - currentSameY >= 3)) {
            return dPosY;
        }
        return currentSameY;
    }

    /**
     * check if block position is air or replaceable
     */
    public static boolean isAir(BlockPos pos) {
        if (mc.level == null) return false;
        return mc.level.getBlockState(pos).isAir() || mc.level.getBlockState(pos).canBeReplaced();
    }

    /**
     * check if player is moving in a cardinal direction
     */
    public static boolean isCardinal() {
        if (mc.player == null) return false;
        float y = Math.abs(mc.player.getYRot() % 90);
        return y < 10 || y > 80;
    }

    /**
     * data class for placement info
     */
    public static class PlaceData {
        public final BlockPos block;
        public final Direction face;
        public final Vec3 hitVec;

        public PlaceData(BlockPos block, Direction face, Vec3 hitVec) {
            this.block = block;
            this.face = face;
            this.hitVec = hitVec;
        }
    }
}
