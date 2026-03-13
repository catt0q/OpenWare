package base.client.feature.impl.player;

import base.CattoWare;
import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventLook;
import base.client.event.events.impl.game.EventOnSprint;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.movement.MoveFix;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static base.client.helpers.utils.BlockPosUtil.fixvec;

public class OldScaffold extends Module {

    private int index3;
    private static int index5;
    private int index7;
    private double prevY;
    private float lastyaw;
    private float lastpitch;
    private double bestdist = 999;

    // Cached rotation values to avoid recalculation
    private float cachedYaw;
    private float cachedPitch;
    private boolean rotationCached = false;

    public boolean needspoofnext = false;
    boolean skippackets = false;

    public static TimerHelper timefromlastC08 = new TimerHelper();

    ArrayDeque<LatePacket> Packets = new ArrayDeque<LatePacket>();

    public BlockPos prevpos = null;

    public BlockPos npb = null;

    public BlockPos finishpos = null;

    public Direction npd = null;

    public ModeSetting Mode;
    public static BooleanSetting LockY = new BooleanSetting("LockY", true, () -> true);

    public static NumberSetting InteractRange = new NumberSetting("Interact Range", 4, 0, 8, 0.01F, () -> true);
    BooleanSetting mfix = new BooleanSetting("MoveFix", "Также требуется включить модуль MoveFix", true, () -> true);

    public static BooleanSetting BypassTech;

    public static ModeSetting Tower = new ModeSetting("Tower",
            "None", () -> true, "Matrix", "None");

    public OldScaffold() {
        super("OldScaffold", "OLD: СТРОИТЬСЯ", Type.Player);

        this.Mode = new ModeSetting("Mode", "1.17", () -> true, "1.17", "Vanilla", "Matrix");
        BypassTech = new BooleanSetting("Bypass Tech", false, () -> true);
        this.addSettings(Mode, Tower, InteractRange, LockY, mfix);
    }

    @Override
    public void onEnable() {
        skippackets = false;
        PacketHelper.Values pc = Client.instance.packet;
        prevpos = null;
        npb = null;
        npd = null;
        finishpos = null;

        index3 = 0;
        index5 = 0;
        index7 = 0;
        timefromlastC08.reset();

        prevY = pc.LastGPosY - 1;
        lastyaw = pc.LastYaw;
        lastpitch = pc.LastPitch;
        bestdist = 999;
        rotationCached = false;

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mfix.isEnabled()) {
            MoveFix.needspoofyaw = false;
        }
        rotationCached = false;
        sendAllPackets();
        super.onDisable();
    }

    @EventTarget
    public void onTick(EventTick e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (mc.player == null) {
            return;
        }

        double multt = 1.2;

        double motY = (LockY.isEnabled()) ? 0 : mc.player.getDeltaMovement().y;
        if (mc.player.onGround()) {
            if (MoveUtil.motYstate() > 0) {
                motY = 0.42;
            } else {
                motY = 0;
            }

        }
        double dPosY = mc.player.getY() - 1;
        double diffY = (motY <= 0) ? -0.4 : 0.01;

        finishpos = new BlockPos((int) Math.floor(mc.player.getX() + mc.player.getDeltaMovement().x * multt),
                (int) Math.floor(dPosY + motY - diffY),
                (int) Math.floor(mc.player.getZ() + mc.player.getDeltaMovement().z * multt));
        if (LockY.isEnabled() || (MoveUtil.checkhoping() && MoveUtil.motYstate() < 1)) {
            if (prevY - (dPosY) > 0.6) {
                prevY = dPosY;
            } else if (dPosY - prevY >= 1 && (mc.player.onGround() || dPosY - prevY >= 3)) {
                prevY = dPosY;

            } else if (dPosY > prevY) {
                finishpos = new BlockPos((int) Math.floor(mc.player.getX() + mc.player.getDeltaMovement().x * multt),
                        (int) Math.floor(prevY + motY - diffY),
                        (int) Math.floor(mc.player.getZ() + mc.player.getDeltaMovement().z * multt));
            }
        }

        ItemStack itst = mc.player.getMainHandItem();
        if (itst == null || !(itst.getItem() instanceof BlockItem)) {
            return;
        }

        float prevYaw = mc.player.getYRot();
        float prevPitch = mc.player.getXRot();
        boolean blockplaced = false;

        switch (Mode.getCurrentMode()) {

            case ("1.17"):

                bestdist = 999;
                npd = null;
                npb = null;

                if (BlockPosUtil.placeableblock(finishpos)) {
                    getNextBlockNew(finishpos, finishpos, 0,
                            (int) Math.round(InteractRange.getValue()), false);
                }

                if (finishpos == null || npb == null || npd == null) {
                    return;
                }

                for (int i = 0; i < Math.round(MoveUtil.getspeed2() * 20); i++) {
                    bestdist = 999;
                    npd = null;
                    npb = null;

                    if (BlockPosUtil.placeableblock(finishpos)) {
                        getNextBlockNew(finishpos, finishpos, 0,
                                Math.round(InteractRange.getValue()), false);
                    } else {
                        break;
                    }

                    if (finishpos != null && npb != null && npd != null) {

                        float newrotY = getRotationsBlockPerfect117(npb, npd)[0];
                        float newrotX = getRotationsBlockPerfect117(npb, npd)[1];
                        float yaw2, pitch2;
                        double diffygp = RotationUtils.yawdiff(pc.LastYaw, newrotY);
                        double diffpgp = RotationUtils.pitchdiff(pc.LastPitch, newrotX);
                        yaw2 = RotationUtils.addyaw(pc.LastYaw, newrotY, (float) diffygp, true, true);
                        pitch2 = RotationUtils.addpitch(pc.LastPitch, newrotX, (float) diffpgp, true);
                        newrotY = ACUtil.GCDFix((float) pc.LastYaw, (float) pc.LastPitch, yaw2, pitch2)[0];
                        newrotX = ACUtil.GCDFix((float) pc.LastYaw, (float) pc.LastPitch, yaw2, pitch2)[1];
                        newrotX = Math.clamp(newrotX, -90, 90);

                        needspoofnext = true;
                        pc.LastYaw = newrotY;
                        pc.LastPitch = newrotX;

                        HitResult hr = TraceUtil.ClientpickNew(InteractRange.getValue(), 1, false, newrotY, newrotX);

                        BlockHitResult bhr = (BlockHitResult) hr;

                        Vec3 vc3 = fixvec(bhr.getLocation(), npd);
                        boolean checkpass = BlockPosUtil.placecheck(npd, bhr.getLocation(), 0);

                        BlockHitResult newbhr = new BlockHitResult(vc3, npd, npb, false);
                        if (bhr != null && npb.getCenter().distanceTo(bhr.getBlockPos().getCenter()) < 0.001
                                && npd == bhr.getDirection()
                                && checkpass) {

                            blockplaced = true;
                            ServerboundMovePlayerPacket duplc06 = new ServerboundMovePlayerPacket.PosRot(pc.LastPosX,
                                    pc.LastPosY, pc.LastPosZ, newrotY, newrotX, pc.LastGround,
                                    mc.player.horizontalCollision);
                            pc.sendPacket(duplc06, 10);

                            pc.LastYaw = newrotY;
                            pc.LastPitch = newrotX;

                            BlockItem bi = (BlockItem) mc.player.getMainHandItem().getItem();
                            mc.level.setBlockAndUpdate(BlockPosUtil.generateBlock(npb, npd),
                                    bi.getBlock().defaultBlockState());
                            pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,

                                    newbhr,

                                    mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));

                            pc.sendPacket(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));

                        }
                    } else {
                        break;
                    }

                }

                if (blockplaced) {
                    ServerboundMovePlayerPacket duplc062 = new ServerboundMovePlayerPacket.PosRot(pc.LastPosX,
                            pc.LastPosY, pc.LastPosZ, mc.player.getYRot(), mc.player.getXRot(), pc.LastGround,
                            mc.player.horizontalCollision);
                    pc.sendPacket(duplc062, 10);
                }

                break;

            case ("Vanilla"):
                bestdist = 999;
                npd = null;
                npb = null;

                if (BlockPosUtil.placeableblock(finishpos)) {
                    getNextBlockNew(finishpos, finishpos, 0,
                            (int) Math.round(InteractRange.getValue()), false);
                }

                if (finishpos == null || npb == null || npd == null) {
                    return;
                }

                float newrotY = getRotationsBlockPerfect117(npb, npd)[0];
                float newrotX = getRotationsBlockPerfect117(npb, npd)[1];
                float yaw2, pitch2;
                double diffygp = RotationUtils.yawdiff(lastyaw, newrotY);
                double diffpgp = RotationUtils.pitchdiff(lastpitch, newrotX);
                yaw2 = RotationUtils.addyaw(lastyaw, newrotY, (float) diffygp, true, true);
                pitch2 = RotationUtils.addpitch(lastpitch, newrotX, (float) diffpgp, true);
                newrotY = ACUtil.GCDFix((float) lastyaw, (float) lastpitch, yaw2, pitch2)[0];
                newrotX = ACUtil.GCDFix((float) lastyaw, (float) lastpitch, yaw2, pitch2)[1];
                newrotX = Math.clamp(newrotX, -90, 90);

                HitResult hr = TraceUtil.ClientpickNew(InteractRange.getValue(), 1, false, newrotY, newrotX);

                BlockHitResult bhr = (BlockHitResult) hr;

                Vec3 vc3 = fixvec(bhr.getLocation(), npd);
                boolean checkpass = BlockPosUtil.placecheck(npd, bhr.getLocation(), 0);
                if (bhr != null && npb.getCenter().distanceTo(bhr.getBlockPos().getCenter()) < 0.001
                        && npd == bhr.getDirection()
                        && checkpass) {
                    lastyaw = newrotY;
                    lastpitch = newrotX;

                }
                break;

            case ("Matrix"):

                bestdist = 999;
                npd = null;
                npb = null;

                if (BlockPosUtil.placeableblock(finishpos)) {
                    getNextBlockNew(finishpos, finishpos, 0,
                            (int) Math.round(InteractRange.getValue()), false);
                }

                if (finishpos == null || npb == null || npd == null) {
                    return;
                }

                float newrotYm = getRotationsBlockPerfect117(npb, npd)[0];
                float newrotXm = getRotationsBlockPerfect117(npb, npd)[1];
                float yaw2m, pitch2m;
                double diffygpm = RotationUtils.yawdiff(lastyaw, newrotYm);
                double diffpgpm = RotationUtils.pitchdiff(lastpitch, newrotXm);

                yaw2m = RotationUtils.addyaw(lastyaw, newrotYm, (float) diffygpm, true, true);
                pitch2m = RotationUtils.addpitch(lastpitch, newrotXm, (float) diffpgpm, true);
                newrotYm = ACUtil.GCDFix((float) lastyaw, (float) lastpitch, yaw2m, pitch2m)[0];
                newrotXm = ACUtil.GCDFix((float) lastyaw, (float) lastpitch, yaw2m, pitch2m)[1];
                newrotXm = Math.clamp(newrotXm, -90, 90);

                hr = TraceUtil.ClientpickNew(InteractRange.getValue(), 1, false, newrotYm, newrotXm);

                bhr = (BlockHitResult) hr;

                vc3 = fixvec(bhr.getLocation(), npd);
                checkpass = BlockPosUtil.placecheck(npd, bhr.getLocation(), 0);
                if (bhr != null && npb.getCenter().distanceTo(bhr.getBlockPos().getCenter()) < 0.001
                        && npd == bhr.getDirection()
                        && checkpass) {
                    lastyaw = newrotYm;
                    lastpitch = newrotXm;

                }
                break;

        }
        if (finishpos == null || npb == null || npd == null) {

            if (mfix.isEnabled()) {
                MoveFix.needspoofyaw = false;
                MoveFix.RealYaw = lastyaw;
            }
        } else {

            if (mfix.isEnabled()) {
                MoveFix.needspoofyaw = true;
                MoveFix.RealYaw = lastyaw;
            }
        }

    }

    @EventTarget
    public void onMotion(EventPreMotion e) {
        EventPreMotion e2 = (EventPreMotion) e;
        PacketHelper.Values pc = Client.instance.packet;

        if (mc.player == null) {
            return;
        }

        ItemStack itst = mc.player.getMainHandItem();
        if (itst == null || !(itst.getItem() instanceof BlockItem)) {
            return;
        }

        switch (Mode.getCurrentMode()) {

            case ("Vanilla"):
                e.setYaw(lastyaw);
                e.setPitch(lastpitch);

                HitResult hr = TraceUtil.ClientpickNew(InteractRange.getValue(), 1, false, lastyaw, lastpitch);

                BlockHitResult bhr = (BlockHitResult) hr;

                Vec3 vc3 = fixvec(bhr.getLocation(), npd);
                boolean checkpass = BlockPosUtil.placecheck(npd, bhr.getLocation(), 0);

                BlockHitResult newbhr = new BlockHitResult(vc3, npd, npb, false);

                if (newbhr != null && npb.getCenter().distanceTo(bhr.getBlockPos().getCenter()) < 0.001
                        && npd == bhr.getDirection()
                        && checkpass) {

                    BlockItem bi = (BlockItem) mc.player.getMainHandItem().getItem();
                    mc.level.setBlockAndUpdate(BlockPosUtil.generateBlock(npb, npd), bi.getBlock().defaultBlockState());
                    pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,

                            newbhr,

                            mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));

                    pc.sendPacket(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
                break;

            case ("Matrix"):
                e.setYaw(lastyaw);
                e.setPitch(lastpitch);
                // here to jump scaf
                if (e.isOnGround()) {
                    return;
                }

                // here to ground tthing

                HitResult hrm = TraceUtil.ClientpickNew(InteractRange.getValue(), 1, false, lastyaw, lastpitch);

                BlockHitResult bhrm = (BlockHitResult) hrm;

                Vec3 vc3m = fixvec(bhrm.getLocation(), npd);
                boolean checkpassm = BlockPosUtil.placecheck(npd, bhrm.getLocation(), 0);

                BlockHitResult newbhrm = new BlockHitResult(vc3m, npd, npb, false);

                if (newbhrm != null && npb.getCenter().distanceTo(bhrm.getBlockPos().getCenter()) < 0.001
                        && npd == bhrm.getDirection()
                        && checkpassm) {

                    BlockItem bi = (BlockItem) mc.player.getMainHandItem().getItem();
                    mc.level.setBlockAndUpdate(BlockPosUtil.generateBlock(npb, npd), bi.getBlock().defaultBlockState());
                    pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,

                            newbhrm,

                            mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));

                    pc.sendPacket(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    pc.sendPacket(new ServerboundSwingPacket(InteractionHand.OFF_HAND));

                }
                break;

        }

        if (Packets.size() > 30
                || (Packets.size() > 0 && Packets.getFirst().getRequiredMs() > System.currentTimeMillis())) {
            sendAllPackets();
        }

        if (!(LockY.isEnabled())) {
            switch (Tower.getCurrentMode()) {

                case ("Matrix"):

                    if (MoveUtil.motYstate() > 0) {
                        if (ACUtil.ismatrixonground() && index5 <= 0) {
                            e.setOnGround(true);
                            MoveUtil.setmotY(0.42);
                            mc.player.setSprinting(false);

                            MoveUtil.limit2speed(0.1);
                            index5 = 1;
                        }

                    }

                    index5--;

                    break;
            }
        }

    }

    @EventTarget
    public void eom(EventOnMovePost e) {

        /*
         * switch (Tower.getCurrentMode()){
         * 
         * case ("Matrix"):
         * 
         * if(MoveUtil.motYstate()>0){
         * if(pc.airpackets==1){
         * 
         * }
         * 
         * 
         * }
         * 
         * 
         * 
         * break;
         * 
         * 
         * 
         * }
         */

    }

    @EventTarget
    public void onSprint(EventOnSprint event) {
        if (index5 > 0) {
            event.setsprint(false);
        }

    }

    @EventTarget
    public void onLook(EventLook e) {

        if (needspoofnext) {
            needspoofnext = false;
            PacketHelper.Values pc = Client.instance.packet;
            e.setYaw(pc.LastYaw);
            e.setPitch(pc.LastPitch);
            switch (Mode.getCurrentMode()) {

                case ("Vanilla"):
                    e.setYaw(lastyaw);
                    e.setPitch(lastpitch);
                    break;

                case ("Matrix"):
                    e.setYaw(lastyaw);
                    e.setPitch(lastpitch);
                    break;
            }

        }
    }

    @EventTarget
    public void onEventSendPacketBlink(EventSendPacketBlink e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();
        if (skippackets)
            return;
        if (BypassTech.isVisible() && BypassTech.isEnabled()) {
            if (packetp instanceof ServerboundUseItemOnPacket) {
                ServerboundUseItemOnPacket c08 = (ServerboundUseItemOnPacket) packetp;

                index3++;
                if (index3 == 5) {
                    Packets.add(new LatePacket(e.getPacket(), (System.currentTimeMillis() + 400)));
                    e.setCancelled(true);
                    return;
                } else if (index3 >= 0) {
                    index3 = 0;
                }

                boolean changedrot = false;
                boolean firstmovement = false;
                skippackets = true;
                for (LatePacket lp : Packets) {
                    Packet packet = lp.getPacket();
                    if (packet instanceof ServerboundMovePlayerPacket c03 && !firstmovement) {
                        firstmovement = true;
                    } else {
                        if (!changedrot) {
                            if (packet instanceof ServerboundMovePlayerPacket c03 && c03.hasPosition()) {
                                packet = new ServerboundMovePlayerPacket.PosRot(c03.x, c03.y, c03.z, lastyaw, lastpitch,
                                        c03.onGround, c03.horizontalCollision);
                                changedrot = true;
                            }
                        } else {
                            if (packet instanceof ServerboundMovePlayerPacket c03) {
                                if (c03.hasPosition()) {
                                    packet = new ServerboundMovePlayerPacket.Pos(c03.x, c03.y, c03.z, c03.onGround,
                                            c03.horizontalCollision);
                                } else {
                                    packet = new ServerboundMovePlayerPacket.StatusOnly(c03.onGround,
                                            c03.horizontalCollision);
                                }
                                if (c03.hasRotation()) {
                                    // changedrot=false;
                                }

                                // packet=new
                                // ServerboundMovePlayerPacket.PosRot(c03.x,c03.y,c03.z,lastyaw,lastpitch,c03.onGround,c03.horizontalCollision);

                            }
                        }

                    }
                    if (packet instanceof ServerboundUseItemOnPacket) {
                        changedrot = false;
                    }

                    pc.sendPacket(packet, 5);
                }
                skippackets = false;
                Packets.clear();
                index7 = 2;
            } else {
                if (index7 > 0) {
                    index7--;
                } else {
                    Packets.add(new LatePacket(e.getPacket(), (System.currentTimeMillis() + 400)));
                    e.setCancelled(true);
                }
            }
        }

    }

    public void sendAllPackets() {

        skippackets = true;
        while (Packets.size() > 0) {
            pc.sendPacket(Packets.pollFirst().getPacket(), 5);
        }
        skippackets = false;
    }

    public static boolean isTowering() {

        return index5 > 0 && Client.instance.featureManager.getModuleByClass(OldScaffold.class).getState();
    }

    public double getNextBlock(BlockPos p, double currdist, int maxdist) {
        double placedist = currdist;
        if (currdist > maxdist) {
            return maxdist;
        }
        if (!BlockPosUtil.placeableblock(p)) {
            return maxdist;
        }

        while (placedist < maxdist) {
            BlockPos bestbp = null;
            double bestdist = 999;

            int beststate = -1;

            BlockPos beast = p.east();// 1
            BlockPos bdown = p.below();// 2
            BlockPos bnorth = p.north();// 3
            BlockPos bwest = p.west();// 4
            BlockPos bsouth = p.south();// 5

            BlockPos bup = p.above();

            if (!BlockPosUtil.placeableblock(beast)) {
                npd = Direction.EAST.getOpposite();
                npb = p;
                return currdist + 1;
            } else if (getNextBlock(beast, currdist + 1, maxdist) < bestdist) {
                bestbp = beast;
                bestdist = getNextBlock(beast, currdist + 1, maxdist);
                beststate = 1;
            }
            if (!BlockPosUtil.placeableblock(bnorth)) {
                npd = Direction.NORTH.getOpposite();
                npb = p;
                return currdist + 1;
            } else if (getNextBlock(bnorth, currdist + 1, maxdist) < bestdist) {
                bestbp = bnorth;
                bestdist = getNextBlock(bnorth, currdist + 1, maxdist);
                beststate = 3;
            }

            if (!BlockPosUtil.placeableblock(bwest)) {
                npd = Direction.WEST.getOpposite();
                npb = p;
                return currdist + 1;
            } else if (getNextBlock(bwest, currdist + 1, maxdist) < bestdist) {
                bestbp = bwest;
                bestdist = getNextBlock(bwest, currdist + 1, maxdist);
                beststate = 4;
            }

            if (!BlockPosUtil.placeableblock(bsouth)) {
                npd = Direction.SOUTH.getOpposite();
                npb = p;
                return currdist + 1;
            } else if (getNextBlock(bsouth, currdist + 1, maxdist) < bestdist) {
                bestbp = bsouth;
                bestdist = getNextBlock(bsouth, currdist + 1, maxdist);
                beststate = 5;
            }

            if (!BlockPosUtil.placeableblock(bdown)) {
                npd = Direction.DOWN.getOpposite();
                npb = p;
                return currdist + 1;
            } else if (getNextBlock(bdown, currdist + 1, maxdist) < bestdist) {
                bestbp = bdown;
                bestdist = getNextBlock(bdown, currdist + 1, maxdist);
                beststate = 2;
            }

            if (bestdist < maxdist) {
                if (beststate == 1) {
                    getNextBlock(beast, currdist + 1, maxdist);
                }
                if (beststate == 2) {
                    getNextBlock(bdown, currdist + 1, maxdist);
                }
                if (beststate == 3) {
                    getNextBlock(bnorth, currdist + 1, maxdist);
                }
                if (beststate == 4) {
                    getNextBlock(bwest, currdist + 1, maxdist);
                }
                if (beststate == 5) {
                    getNextBlock(bsouth, currdist + 1, maxdist);
                }

                return bestdist;
            } else {
                return maxdist;
            }

        }

        return maxdist;
    }

    public void StartNextBlockNew(BlockPos startp, BlockPos p, double currdist, int maxdist, boolean up) {
        bestdist = 999;
        getNextBlockNew(startp, p, currdist, maxdist, up);
    }

    public boolean getNextBlockNew(BlockPos startp, BlockPos p, double currdist, int maxdist, boolean up) {

        if (currdist > maxdist) {
            return false;
        }
        if (!BlockPosUtil.placeableblock(p)) {
            return false;
        }

        BlockPos testpos = BlockPosUtil.checkforplaceableblock(p, up);

        if (testpos != null && bestdist > currdist) {
            bestdist = currdist;
            npb = testpos;
            npd = BlockPosUtil.getDirection(startp, testpos);
            return true;
        }

        BlockPos beast = p.east();
        BlockPos bdown = p.below();
        BlockPos bnorth = p.north();
        BlockPos bwest = p.west();
        BlockPos bsouth = p.south();
        BlockPos bup = p.above();
        boolean finded = false;
        finded = getNextBlockNew(startp, beast, currdist + 1, maxdist, up) ? true : finded;
        finded = getNextBlockNew(startp, bwest, currdist + 1, maxdist, up) ? true : finded;
        finded = getNextBlockNew(startp, bnorth, currdist + 1, maxdist, up) ? true : finded;
        finded = getNextBlockNew(startp, bsouth, currdist + 1, maxdist, up) ? true : finded;
        finded = getNextBlockNew(startp, bdown, currdist + 1, maxdist, up) ? true : finded;
        if (up) {
            finded = getNextBlockNew(startp, bup, currdist + 1, maxdist, up) ? true : finded;
        }
        return finded;
    }

    public float[] getRotationsBlockPerfect117(BlockPos p, Direction dir) {
        Vec3 center = p.getCenter();
        double deltaX2 = 0;
        double deltaY2 = 0;
        double deltaZ2 = 0;

        double diff = 0.501;
        switch (dir) {
            case DOWN -> deltaY2 = -diff;
            case UP -> deltaY2 = diff;

            case EAST -> deltaX2 = diff;
            case WEST -> deltaX2 = -diff;

            case NORTH -> deltaZ2 = -diff;
            case SOUTH -> deltaZ2 = diff;
        }

        deltaY2 -= 0.45;

        double playerX = pc.LastPosX;
        double playerY = pc.LastPosY + mc.player.getEyeHeight();
        double playerZ = pc.LastPosZ;

        final float deltaX = (float) ((center.x() + deltaX2) - (playerX));
        final float deltaY = (float) ((center.y() + deltaY2) - (playerY));
        final float deltaZ = (float) ((center.z() + deltaZ2) - (playerZ));
        final float distance = (float) (Math.abs(deltaX) + Math.abs(deltaZ));
        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ));
        final float pitch = (float) (-Math.toDegrees(Math.atan(deltaY / distance)));
        if (deltaX < 0.0f && deltaZ < 0.0f) {
            yaw = (float) (90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        } else if (deltaX > 0.0f && deltaZ < 0.0f) {
            yaw = (float) (-90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        if (yaw < -360) {
            yaw += 360;
        }
        if (yaw > 360) {
            yaw -= 360;
        }

        return new float[] { yaw, pitch };
    }

    public float[] getRotationsBlock(BlockPos p, Direction dir) {
        Vec3 center = p.getCenter();
        PacketHelper.Values pc = Client.instance.packet;
        double deltaX2 = 0;
        double deltaY2 = 0;
        double deltaZ2 = 0;

        double diff = 0.5;

        if (dir == Direction.UP) {
            deltaY2 = diff;
        } else if (dir == Direction.DOWN) {
            deltaY2 = -diff;
        }

        else if (dir == Direction.EAST) {
            deltaX2 = diff;
        } else if (dir == Direction.WEST) {
            deltaX2 = -diff;
        }

        else if (dir == Direction.NORTH) {
            deltaZ2 = -diff;
        } else if (dir == Direction.SOUTH) {
            deltaZ2 = +diff;
        }
        deltaY2 -= 0.4;

        final float deltaX = (float) ((center.x() + deltaX2) - (pc.LastPosX + mc.player.getDeltaMovement().x));
        final float deltaY = (float) ((center.y() + deltaY2) - (pc.LastPosY + 1.8 + mc.player.getDeltaMovement().y));
        final float deltaZ = (float) ((center.z() + deltaZ2) - (pc.LastPosZ + mc.player.getDeltaMovement().z));
        final float distance = (float) (Math.abs(deltaX) + Math.abs(deltaZ));
        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ));
        final float pitch = (float) (-Math.toDegrees(Math.atan(deltaY / distance)));
        if (deltaX < 0.0f && deltaZ < 0.0f) {
            yaw = (float) (90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        } else if (deltaX > 0.0f && deltaZ < 0.0f) {
            yaw = (float) (-90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        if (yaw < -360) {
            yaw += 360;
        }
        if (yaw > 360) {
            yaw -= 360;
        }
        return new float[] { yaw, pitch };
    }
}
