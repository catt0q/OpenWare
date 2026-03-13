package base.client.helpers.utils.scaffold;

import base.client.Client;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.TraceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * handles block placement and validation for scaffold
 */
public class ScaffoldPlacer {
    private static final Minecraft mc = Minecraft.getInstance();

    public enum RaycastMode {
        OFF, NORMAL, STRICT
    }

    /**
     * validate placement with raycast
     */
    public static boolean validatePlacement(ScaffoldUtils.PlaceData data, float yaw, float pitch, RaycastMode mode) {
        if (data == null) return false;
        if (mode == RaycastMode.OFF) return true;

        HitResult hit = TraceUtil.ClientpickNew(4.5f, 1, false, yaw, pitch);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return mode == RaycastMode.NORMAL;
        }

        BlockHitResult bhr = (BlockHitResult) hit;
        if (mode == RaycastMode.STRICT) {
            return bhr.getBlockPos().equals(data.block) && bhr.getDirection() == data.face;
        }
        return bhr.getBlockPos().equals(data.block);
    }

    /**
     * place block with manual rotation packets (like OldScaffold)
     * this method sends: rotation packet -> place packet -> swing packet
     */
    public static boolean placeWithRotation(ScaffoldUtils.PlaceData data, float yaw, float pitch) {
        if (mc.player == null || mc.level == null || data == null) return false;

        PacketHelper.Values pc = Client.instance.packet;
        BlockHitResult hit = new BlockHitResult(data.hitVec, data.face, data.block, false);

        // send rotation packet FIRST (like OldScaffold line 228-231)
        ServerboundMovePlayerPacket rotPacket = new ServerboundMovePlayerPacket.PosRot(
            pc.LastPosX, pc.LastPosY, pc.LastPosZ,
            yaw, pitch,
            pc.LastGround, mc.player.horizontalCollision
        );
        pc.sendPacket(rotPacket, 10);

        // update last rotation
        pc.LastYaw = yaw;
        pc.LastPitch = pitch;

        // client-side block prediction (like OldScaffold line 236-238)
        if (mc.player.getMainHandItem().getItem() instanceof BlockItem bi) {
            mc.level.setBlockAndUpdate(
                data.block.relative(data.face),
                bi.getBlock().defaultBlockState()
            );
        }

        // send place packet
        pc.sendPacket(new ServerboundUseItemOnPacket(
            InteractionHand.MAIN_HAND,
            hit,
            mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()
        ));

        // send swing
        pc.sendPacket(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));

        return true;
    }

    /**
     * convert string to RaycastMode enum
     */
    public static RaycastMode parseRaycastMode(String mode) {
        return switch (mode) {
            case "Normal" -> RaycastMode.NORMAL;
            case "Strict" -> RaycastMode.STRICT;
            default -> RaycastMode.OFF;
        };
    }
}
