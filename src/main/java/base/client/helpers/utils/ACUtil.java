package base.client.helpers.utils;

import base.client.Client;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

//AntiCheat util
public class ACUtil {

    public static boolean ismatrixonground() {

        if (Minecraft.getInstance().player.verticalCollision && Minecraft.getInstance().player.onGround()) {
            return true;
        }
        for (int i = 0; i <= 10; ++i) {
            BlockPos bp = new BlockPos(Minecraft.getInstance().player.getBlockX(),
                    (int) (Minecraft.getInstance().player.getY() - i * 0.1),
                    Minecraft.getInstance().player.getBlockZ());
            BlockState bs = Minecraft.getInstance().level.getBlockState(bp);
            Block bl = bs.getBlock();

            if (!(bl instanceof AirBlock) &&
                    !bs.isCollisionShapeFullBlock(Minecraft.getInstance().level, bp)) {
                break;

            }
            if (!(bl instanceof AirBlock)) {
                return true;
            }

        }
        return false;
    }

    public static boolean ismatrixonground2() {

        for (int i = 0; i <= 10; ++i) {
            BlockPos bp = new BlockPos(Minecraft.getInstance().player.getBlockX(),
                    (int) (Minecraft.getInstance().player.getY() - i * 0.1),
                    Minecraft.getInstance().player.getBlockZ());
            BlockState bs = Minecraft.getInstance().level.getBlockState(bp);
            Block bl = bs.getBlock();

            if (!(bl instanceof AirBlock)) {
                return true;
            }

        }
        return false;
    }

    public static boolean isground() {

        BlockPos bp = new BlockPos(Minecraft.getInstance().player.getBlockX(),
                (int) Math.floor(Minecraft.getInstance().player.getY() - 0.001),
                Minecraft.getInstance().player.getBlockZ());
        BlockState bs = Minecraft.getInstance().level.getBlockState(bp);

        return !(bs.isAir() || bs.getBlock() instanceof LiquidBlock);
    }

    public static boolean matrixisvoidcheck() {
        if (Minecraft.getInstance().player.onGround()) {
            return false;
        }
        for (int i = Minecraft.getInstance().player.getBlockY(); i > 0; i--) {
            BlockState bs = Minecraft.getInstance().level.getBlockState(new BlockPos(
                    Minecraft.getInstance().player.getBlockX(), i, Minecraft.getInstance().player.getBlockZ()));
            Block bl = bs.getBlock();

            if (!(bl instanceof AirBlock)) {
                return false;
            }
        }

        return true;
    }

    public static double matrixisvoidcheckblocks() {
        if (Minecraft.getInstance().player.onGround()) {
            return Minecraft.getInstance().player.getBlockY();
        }
        for (int i = Minecraft.getInstance().player.getBlockY(); i > 0; i--) {

            BlockState bs = Minecraft.getInstance().level.getBlockState(new BlockPos(
                    Minecraft.getInstance().player.getBlockX(), i, Minecraft.getInstance().player.getBlockZ()));
            Block bl = bs.getBlock();

            if (!(bl instanceof AirBlock)) {
                return i;
            }

        }

        return -1;
    }

    public static double[] matrixgetpredictsetback() {
        PacketHelper.Values pc = Client.instance.packet;
        if (!ismatrixonground() && matrixisvoidcheck()) {
            return new double[] { pc.LastPosX, (pc.LastPosY > -10) ? -10 : pc.LastPosY, pc.LastPosZ };
        }
        return new double[] { pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ };
    }

    public static boolean matrixisusingitem() {
        boolean using = Minecraft.getInstance().player.isUsingItem();
        PacketHelper.Values pc = Client.instance.packet;
        if (pc.ticknotusingitem < 3) {
            using = true;
        }
        return using;
    }

    public static double getmatrixbasicspeed() {
        return 0.1999;
    }

    public static double findnearestgroundY10() {
        // search up to 50 blocks down (500 * 0.1 = 50)
        for (int i = 0; i <= 500; ++i) {
            BlockPos bp = new BlockPos(Minecraft.getInstance().player.getBlockX(),
                    (int) (Minecraft.getInstance().player.getY() - i * 0.1),
                    Minecraft.getInstance().player.getBlockZ());
            BlockState bs = Minecraft.getInstance().level.getBlockState(bp);
            Block bl = bs.getBlock();

            if (!(bl instanceof AirBlock)) {
                return bp.getY();
            }

        }
        return -1;
    }

    public static boolean matrixboost2() {
        double minspeed = getmatrixbasicspeed();
        if (MoveUtil.getspeed2() < minspeed) {
            MoveUtil.smartstrafe(minspeed);
            return true;
        }

        return false;
    }

    public static float[] GCDFix(float prevyaw, float prevpitch, float newyaw, float newpitch) {
        float f = (float) (Minecraft.getInstance().options.sensitivity().get() * 0.6F + 0.2F);
        float gcd = f * f * f * 1.2F;
        float nextYaw = newyaw;
        float nextPitch = newpitch;
        float deltaYaw = nextYaw - prevyaw;
        float deltaPitch = nextPitch - prevyaw;
        float fixedDeltaYaw = deltaYaw - (deltaYaw % gcd);
        float fixedDeltaPitch = deltaPitch - (deltaPitch % gcd);
        float fixedYaw = prevyaw + fixedDeltaYaw;
        float fixedPitch = prevyaw + fixedDeltaPitch;
        return new float[] { fixedYaw, fixedPitch };
    }

    public static float getMouseGCD() {
        return (float) (Math.pow(Minecraft.getInstance().options.sensitivity().get() * 0.6F + 0.2F, 3) * 1.2F);
    }

    public static void sendOffsetPosition(double offset) {
        sendOffsetPosition(offset, Minecraft.getInstance().player.position());

    }

    public static void sendOffsetPosition(double offset, Vec3 pos) {
        PacketHelper.Values pc = Client.instance.packet;
        ServerboundMovePlayerPacket duplc04 = new ServerboundMovePlayerPacket.Pos(pos.x,
                pos.y + offset, pos.z,
                false, Minecraft.getInstance().player.horizontalCollision);
        pc.sendPacket(duplc04, 10);

    }

    public static void send117DuplPacket() {
        PacketHelper.Values pc = Client.instance.packet;
        send117DuplPacket(pc.LastYaw, pc.LastPitch);
    }

    public static void send117DuplPacket(float yaw, float pitch) {
        PacketHelper.Values pc = Client.instance.packet;
        int ap = pc.airpackets;
        ServerboundMovePlayerPacket duplc06 = new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY,
                pc.LastPosZ, yaw, pitch, pc.LastGround, Minecraft.getInstance().player.horizontalCollision);
        pc.sendPacket(duplc06, 10);
        if (!pc.LastGround && ap == pc.airpackets) {
            pc.airpackets++;
        }

    }

    public static ServerboundMovePlayerPacket.PosRot matrixflagpacket() {
        return new ServerboundMovePlayerPacket.PosRot(0, 0, 0, 0, 0, false, false);
    }

}