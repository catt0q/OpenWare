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
import net.minecraft.world.InteractionResult;
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

public class Scaffold extends Module {

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

    // bypass state tracking
    private int airTicks = 0;
    private int groundTicks = 0;
    private double sameYLevel = 0;

    // bridging mode state
    private int tellyTicks = 0;
    private int godbridgeTicks = 0;
    private float yawOffset = 0;
    private float pitchOffset = 0;
    private boolean rotationBoost = false;

    public static TimerHelper timefromlastC08 = new TimerHelper();
    private final TimerHelper placeTimer = new TimerHelper();

    // used to alternate X/Z stepping while moving diagonally
    private boolean diagonalToggle = false;

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
            "None", () -> true, "None", "Matrix", "NCP", "Vulcan");

    public static ModeSetting Sprint = new ModeSetting("Sprint",
            "Normal", () -> true, "Normal", "Legit", "Disabled", "Grim", "NCP", "Vulcan", "Intave");

    public static ModeSetting Bridging = new ModeSetting("Bridging",
            "Normal", () -> true, "Normal", "Godbridge", "Breezily", "Telly", "Eagle", "Snap");

    public static BooleanSetting MotionBoost = new BooleanSetting("Motion Boost",
            "Extra speed boost on landing (Grim/NCP)", false,
            () -> Sprint.getCurrentMode().equals("Grim") || Sprint.getCurrentMode().equals("NCP"));

    public static BooleanSetting SilentSwitch = new BooleanSetting("Silent Switch",
            "Silently switch to block slot (server only)", false, () -> true);

    public static BooleanSetting AutoSwitch = new BooleanSetting("Auto Switch",
            "Auto switch to block slot if not holding blocks", true, () -> !SilentSwitch.isEnabled());

    public static BooleanSetting RequireUseKey = new BooleanSetting("Require Use Key",
            "Only place while holding right click", true, () -> true);

    public static NumberSetting PlaceDelayMs = new NumberSetting("Place Delay",
            "Delay between places", 85f, 0f, 500f, 1f, () -> true, NumberSetting.NumberType.MS);

    public static NumberSetting MaxPlacesPerTick = new NumberSetting("Max Places/Tick",
            "Limits fast place spam", 1f, 1f, 5f, 1f, () -> true);

    private int savedSlot = -1;
    private int blockSlot = -1;

    public Scaffold() {
        super("Scaffold", "СТРОИТЬСЯ", Type.Player);

        this.Mode = new ModeSetting("Mode", "1.17", () -> true, "1.17", "Vanilla", "Matrix");
        BypassTech = new BooleanSetting("Bypass Tech", false, () -> true);
        this.addSettings(Mode, Bridging, Sprint, Tower, InteractRange, LockY, mfix,
                RequireUseKey, PlaceDelayMs, MaxPlacesPerTick,
                MotionBoost, SilentSwitch, AutoSwitch);
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
        placeTimer.setLastMS(0);

        prevY = pc.LastGPosY - 1;
        lastyaw = pc.LastYaw;
        lastpitch = pc.LastPitch;
        bestdist = 999;
        rotationCached = false;

        // bypass state init
        airTicks = 0;
        groundTicks = 0;
        sameYLevel = mc.player != null ? Math.floor(mc.player.getY()) - 1 : 0;

        // bridging state init
        tellyTicks = 0;
        godbridgeTicks = 0;
        yawOffset = 0;
        pitchOffset = 0;
        rotationBoost = false;

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mfix.isEnabled()) {
            MoveFix.needspoofyaw = false;
        }
        rotationCached = false;
        TimerUtil.reset();

        // restore original slot if silent switch was used
        if (SilentSwitch.isEnabled() && savedSlot != -1) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(savedSlot));
        }

        // reset slot tracking
        savedSlot = -1;
        blockSlot = -1;

        // reset sprint override state
        index5 = 0;

        sendAllPackets();
        super.onDisable();
    }

    @EventTarget
    public void onTick(EventTick e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (mc.player == null) {
            return;
        }

        boolean diagonalMove = isDiagonalMoveInput();
        if (diagonalMove && MoveUtil.ismovinginput()) {
            diagonalToggle = !diagonalToggle;
        } else {
            diagonalToggle = false;
        }

        // update air/ground tracking
        if (mc.player.onGround()) {
            groundTicks++;
            airTicks = 0;
        } else {
            airTicks++;
            groundTicks = 0;
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

        // Predict next block under player, but clamp horizontal prediction.
        // Without clamping, fast/diagonal movement can overshoot 1+ blocks and cause bad placements/lagback.
        double dx = mc.player.getDeltaMovement().x;
        double dz = mc.player.getDeltaMovement().z;
        double horizLen = Math.sqrt(dx * dx + dz * dz);
        double maxHoriz = 0.85; // blocks/tick (keeps diagonal stable without becoming "fast place")
        if (horizLen > maxHoriz && horizLen > 0.0) {
            double s = maxHoriz / horizLen;
            dx *= s;
            dz *= s;
        }

        int baseY = (int) Math.floor(dPosY + motY - diffY);
        BlockPos primary = new BlockPos(
                (int) Math.floor(mc.player.getX() + dx * multt),
                baseY,
                (int) Math.floor(mc.player.getZ() + dz * multt));
        BlockPos altX = new BlockPos(
                (int) Math.floor(mc.player.getX() + dx * multt),
                baseY,
                (int) Math.floor(mc.player.getZ()));
        BlockPos altZ = new BlockPos(
                (int) Math.floor(mc.player.getX()),
                baseY,
                (int) Math.floor(mc.player.getZ() + dz * multt));

        // For diagonal movement we can't reliably place on the diagonal corner (no adjacent face),
        // so we alternate X/Z stepping to keep a valid support face and avoid lagback.
        if (diagonalMove) {
            BlockPos preferred = diagonalToggle ? altX : altZ;
            BlockPos secondary = diagonalToggle ? altZ : altX;
            if (BlockPosUtil.placeableblock(preferred)) {
                finishpos = preferred;
            } else if (BlockPosUtil.placeableblock(secondary)) {
                finishpos = secondary;
            } else {
                finishpos = primary;
            }
        } else {
            finishpos = primary;
            if (!BlockPosUtil.placeableblock(finishpos)) {
                if (BlockPosUtil.placeableblock(altX)) {
                    finishpos = altX;
                } else if (BlockPosUtil.placeableblock(altZ)) {
                    finishpos = altZ;
                }
            }
        }
        if (LockY.isEnabled() || (MoveUtil.checkhoping() && MoveUtil.motYstate() < 1)) {
            if (prevY - (dPosY) > 0.6) {
                prevY = dPosY;
            } else if (dPosY - prevY >= 1 && (mc.player.onGround() || dPosY - prevY >= 3)) {
                prevY = dPosY;

            } else if (dPosY > prevY) {
                int lockY = (int) Math.floor(prevY + motY - diffY);
                BlockPos primaryLock = new BlockPos(
                        (int) Math.floor(mc.player.getX() + dx * multt),
                        lockY,
                        (int) Math.floor(mc.player.getZ() + dz * multt));
                BlockPos altXLock = new BlockPos(
                        (int) Math.floor(mc.player.getX() + dx * multt),
                        lockY,
                        (int) Math.floor(mc.player.getZ()));
                BlockPos altZLock = new BlockPos(
                        (int) Math.floor(mc.player.getX()),
                        lockY,
                        (int) Math.floor(mc.player.getZ() + dz * multt));

                if (diagonalMove) {
                    BlockPos preferred = diagonalToggle ? altXLock : altZLock;
                    BlockPos secondary = diagonalToggle ? altZLock : altXLock;
                    if (BlockPosUtil.placeableblock(preferred)) {
                        finishpos = preferred;
                    } else if (BlockPosUtil.placeableblock(secondary)) {
                        finishpos = secondary;
                    } else {
                        finishpos = primaryLock;
                    }
                } else {
                    finishpos = primaryLock;
                    if (!BlockPosUtil.placeableblock(finishpos)) {
                        if (BlockPosUtil.placeableblock(altXLock)) {
                            finishpos = altXLock;
                        } else if (BlockPosUtil.placeableblock(altZLock)) {
                            finishpos = altZLock;
                        }
                    }
                }
            }
        }

        // find best block slot in hotbar
        int bestBlockSlot = findBestBlockSlot();

        // auto switch: change client slot to block slot
        if (AutoSwitch.isEnabled() && !SilentSwitch.isEnabled()) {
            if (bestBlockSlot != -1 && !(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
                mc.player.getInventory().setSelectedSlot(bestBlockSlot);
            }
        }

        // silent switch: find block slot and switch server-side only
        if (SilentSwitch.isEnabled()) {
            if (bestBlockSlot != -1) {
                // always keep server synced to block slot
                if (blockSlot != bestBlockSlot) {
                    silentSwitchTo(bestBlockSlot);
                }
            }
        }

        // check if holding block item (or silent switch active)
        ItemStack itst;
        if (SilentSwitch.isEnabled() && blockSlot != -1) {
            itst = mc.player.getInventory().getItem(blockSlot);
        } else {
            itst = mc.player.getMainHandItem();
        }

        if (itst == null || !(itst.getItem() instanceof BlockItem)) {
            // no blocks found
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

                int maxPlaces = (int) Math.round(MaxPlacesPerTick.getValue());
                for (int i = 0; i < maxPlaces; i++) {
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

                            // Place using vanilla gameMode for correct prediction + sound + swing
                            if (!placeLegit(newbhr)) {
                                break;
                            }

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

        // apply bridging mode rotation override
        float[] bridgingRot = handleBridgingMode();
        if (bridgingRot != null) {
            lastyaw = bridgingRot[0];
            lastpitch = bridgingRot[1];
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

        ItemStack itst = (SilentSwitch.isEnabled() && blockSlot != -1)
                ? mc.player.getInventory().getItem(blockSlot)
                : mc.player.getMainHandItem();
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

                    placeLegit(newbhr);
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

                    placeLegit(newbhrm);
                }
                break;

        }

        if (Packets.size() > 30
                || (Packets.size() > 0 && Packets.getFirst().getRequiredMs() > System.currentTimeMillis())) {
            sendAllPackets();
        }

        // handle bypass modes
        handleSprintBypasses();
        handleTowerBypasses(e);

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
        // matrix tower needs sprint disabled
        if (index5 > 0) {
            event.setsprint(false);
        }
        // diagonal bridge: never sprint (reduces flags + keeps placements stable)
        if (isDiagonalMoveInput()) {
            event.setsprint(false);
        }
        // disabled sprint mode
        if (Sprint.getCurrentMode().equals("Disabled")) {
            event.setsprint(false);
        }
    }

    private boolean isDiagonalMoveInput() {
        if (mc == null || mc.options == null) {
            return false;
        }
        boolean forward = mc.options.keyUp.isDown() || mc.options.keyDown.isDown();
        boolean strafe = mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
        return forward && strafe;
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

        // silent switch: inject slot switch packets around block placement
        if (SilentSwitch.isEnabled() && blockSlot != -1 && packetp instanceof ServerboundUseItemOnPacket) {
            int currentSlot = mc.player.getInventory().getSelectedSlot();
            if (currentSlot != blockSlot) {
                // send slot switch to block slot, then the place packet, then switch back
                e.setCancelled(true);
                pc.sendPacket(new ServerboundSetCarriedItemPacket(blockSlot));
                pc.sendPacket(packetp);
                pc.sendPacket(new ServerboundSetCarriedItemPacket(currentSlot));
                return;
            }
        }

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

        return index5 > 0 && Client.instance.featureManager.getModuleByClass(Scaffold.class).getState();
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

    /**
     * handles sprint bypass modes based on Rise Client deobf
     */
    private void handleSprintBypasses() {
        if (mc.player == null)
            return;

        // If moving diagonally (WASD combo), force no-sprint. Sprinting diagonally tends to
        // overshoot placements and get flagged/lagbacked.
        if (isDiagonalMoveInput() && MoveUtil.ismovinginput()) {
            mc.player.setSprinting(false);
            return;
        }

        switch (Sprint.getCurrentMode()) {
            case "Normal" -> {
                //
                // normal: enable sprint while moving
                if (MoveUtil.ismovinginput() && !mc.options.keyShift.isDown()) {
                    mc.player.setSprinting(true);
                }
            }
            case "Legit" -> {
                // legit: disable sprint when looking back (placing), re-enable when moving
                // forward
                if (npb != null && npd != null) {
                    // disable sprint while placing
                    mc.player.setSprinting(false);
                } else if (MoveUtil.ismovinginput()) {
                    // enable sprint when moving and not placing
                    mc.player.setSprinting(true);
                }
            }
            case "Grim" -> {
                // grim: motion boost on landing + strafe
                float normalizedYaw = mc.player.getYRot() % 90f;
                boolean isCardinal = Math.abs(normalizedYaw) <= 10f || Math.abs(normalizedYaw) >= 80f;

                if (MotionBoost.isEnabled() && mc.player.onGround() && mc.player.fallDistance > 2 && groundTicks > 7) {
                    MoveUtil.strafe();
                    double boost = isCardinal ? 1.1225 : 1.129;
                    MoveUtil.mult2ds(boost);
                }

                // strafe on first air tick
                if (airTicks == 1) {
                    MoveUtil.strafe();
                }

                // strafe when grounded
                if (mc.player.onGround()) {
                    MoveUtil.strafe();
                }

                // enable sprint
                if (MoveUtil.ismovinginput()) {
                    mc.player.setSprinting(true);
                }
            }
            case "NCP" -> {
                boolean atSameY = Math.abs(mc.player.getY() - 1 - sameYLevel) < 0.01;

                // auto jump when not at same Y
                if (!atSameY && mc.player.onGround() && MoveUtil.ismovinginput()) {
                    mc.player.jumpFromGround();
                    MoveUtil.strafe();
                }

                // add motion at peak of jump (air tick 4)
                if (airTicks == 4 && !mc.options.keyShift.isDown()) {
                    MoveUtil.addmotY(0.098);
                }

                // strafe on first air tick
                if (airTicks == 1 && !mc.options.keyShift.isDown()) {
                    MoveUtil.strafe();
                }

                // motion boost on landing
                if (MotionBoost.isEnabled() && (airTicks == 1 || airTicks == 4)) {
                    MoveUtil.strafe();
                }

                // enable sprint
                if (MoveUtil.ismovinginput()) {
                    mc.player.setSprinting(true);
                }
            }
            case "Vulcan" -> {
                // vulcan: disable sprint when sneak pressed, otherwise enable
                if (mc.options.keyShift.isDown()) {
                    mc.player.setSprinting(false);
                } else if (MoveUtil.ismovinginput()) {
                    mc.player.setSprinting(true);
                }
            }
            case "Intave" -> {
                // intave: timer boost + motion dampen on ground
                if (mc.player.onGround() && !mc.options.keyShift.isDown()) {
                    TimerUtil.setTimerspeed(1.0029f);
                    MoveUtil.strafe();
                    MoveUtil.mult2ds(0.998);
                }
                // enable sprint
                if (MoveUtil.ismovinginput()) {
                    mc.player.setSprinting(true);
                }
            }
            case "Disabled" -> {
                // force disable sprint
                mc.player.setSprinting(false);
            }
        }

    }

    /**
     * handles tower bypass modes based on Rise Client deobf
     */
    private void handleTowerBypasses(EventPreMotion e) {
        if (mc.player == null)
            return;
        if (LockY.isEnabled())
            return;
        if (!mc.options.keyJump.isDown())
            return;
        if (MoveUtil.ismovinginput())
            return;

        switch (Tower.getCurrentMode()) {
            case "NCP" -> {
                // ncp tower: jump + motion boost at air tick 4
                if (mc.player.onGround()) {

                }
            }
            case "Vulcan" -> {
                // vulcan tower: simple jump
                if (mc.player.onGround()) {
                    mc.player.jumpFromGround();
                }

            }

            case "Matrix" -> {
                // existing matrix tower logic
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
            }
        }
    }

    /**
     * check if player yaw is cardinal direction
     */
    private boolean isCardinal() {
        float y = Math.abs(mc.player.getYRot() % 90);
        return y < 10 || y > 80;
    }

    /**
     * handles bridging modes - modifies rotation calculation based on selected
     * technique
     */
    private float[] handleBridgingMode() {
        if (mc.player == null || npb == null || npd == null)
            return null;

        switch (Bridging.getCurrentMode()) {
            case "Normal" -> {

                return null;
            }
            case "Godbridge" -> {
                float baseYaw = mc.player.getYRot();
                float targetYaw = baseYaw - (baseYaw % 90f) - 180f + (45f * (baseYaw > 0 ? 1 : -1));
                float targetPitch = 76.4f;

                double edgeThreshold = 0.15;
                boolean nearXEdge = Math.abs(mc.player.getX() % 1.0) > 1.0 - edgeThreshold
                        || Math.abs(mc.player.getX() % 1.0) < edgeThreshold;
                boolean nearZEdge = Math.abs(mc.player.getZ() % 1.0) > 1.0 - edgeThreshold
                        || Math.abs(mc.player.getZ() % 1.0) < edgeThreshold;

                if (nearXEdge && nearZEdge) {

                    mc.options.keyShift.setDown(true);

                }

                godbridgeTicks++;
                if (godbridgeTicks <= 10) {
                    mc.player.setSprinting(true);
                } else if (godbridgeTicks == 11) {
                    mc.player.setSprinting(false);
                }

                if (Math.random() > 0.99) {
                    yawOffset = (float) ((Math.random() - 0.5) / 10.0);
                    pitchOffset = (float) ((Math.random() - 0.5) / 10.0);
                }

                return new float[] { targetYaw + yawOffset, targetPitch + pitchOffset };
            }
            case "Breezily" -> {
                if (npd == Direction.DOWN) {
                    return new float[] { mc.player.getYRot(), 90f };
                }

                double offsetX = npd.getStepX();
                double offsetZ = npd.getStepZ();
                float targetYaw = (float) Math.toDegrees(Math.atan2(offsetZ, offsetX)) - 90f;
                float targetPitch = 80f;

                if (Math.random() > 0.99) {
                    yawOffset = (float) (Math.random() - 0.5);
                    pitchOffset = (float) (Math.random() - 0.5);
                }

                return new float[] { targetYaw + yawOffset, targetPitch + pitchOffset };
            }
            case "Telly" -> {
                tellyTicks++;

                if (mc.player.onGround() && MoveUtil.ismovinginput()) {
                    if (!mc.options.keyShift.isDown() && (mc.player.fallDistance <= 1 || tellyTicks <= 3)) {
                        rotationBoost = true;
                        mc.player.jumpFromGround();
                    }
                }

                float targetYaw = lastyaw;
                float targetPitch = lastpitch;

                if (rotationBoost && !mc.player.onGround()) {
                    targetPitch = (float) (targetPitch + Math.random() * 0.5);
                }

                if (mc.player.onGround() && tellyTicks > 5) {
                    rotationBoost = false;
                }

                return new float[] { targetYaw, targetPitch };
            }
            case "Eagle" -> {
                float baseYaw = (mc.player.getYRot() + 10000000f) % 360f;
                float targetYaw = baseYaw - 180f - (baseYaw % 90f) + 45f;
                float targetPitch = 78f;

                boolean isMovingCardinal = Math.min(Math.abs(baseYaw % 90f), Math.abs(90f - baseYaw) % 90f) < Math
                        .min(Math.abs(baseYaw + 45f) % 90f, Math.abs(90f - (baseYaw + 45f)) % 90f);

                if (isMovingCardinal) {
                    targetYaw += 90f;
                }

                if (airTicks >= 4 && MoveUtil.ismovinginput()) {
                    mc.options.keyShift.setDown(true);
                }

                if (mc.player.fallDistance == 1) {
                    mc.options.keyShift.setDown(false);
                }

                return new float[] { targetYaw + yawOffset / 2f, targetPitch + pitchOffset / 2f };
            }
            case "Snap" -> {
                if (npd != null && npb != null) {
                    Vec3 blockCenter = npb.getCenter();
                    double deltaX = blockCenter.x - mc.player.getX();
                    double deltaZ = blockCenter.z - mc.player.getZ();
                    // yaw should aim at the target block center
                    float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90f;
                    float targetPitch = 80f;
                    return new float[] { targetYaw, targetPitch };
                }
                return null;
            }
        }
        return null;
    }

    /**
     * finds best block slot in hotbar
     */
    public int findBestBlockSlot() {
        if (mc.player == null)
            return -1;

        int bestSlot = -1;
        int bestCount = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && stack.getItem() instanceof BlockItem) {
                if (stack.getCount() > bestCount) {
                    bestCount = stack.getCount();
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    /**
     * silently switch to block slot (server only)
     */
    public void silentSwitchTo(int slot) {
        if (mc.player == null || slot < 0 || slot > 8)
            return;
        if (mc.getConnection() == null)
            return;

        if (savedSlot == -1) {
            savedSlot = mc.player.getInventory().getSelectedSlot();
        }
        blockSlot = slot;
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
    }

    /**
     * switch back to original slot
     */
    public void silentSwitchBack() {
        if (mc.player == null || savedSlot == -1)
            return;
        if (mc.getConnection() == null)
            return;

        mc.getConnection().send(new ServerboundSetCarriedItemPacket(savedSlot));
        blockSlot = -1;
        savedSlot = -1;
    }

    /**
     * get total block count in entire inventory
     */
    public static int getTotalBlocks() {
        if (mc.player == null)
            return 0;

        int total = 0;

        // hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && stack.getItem() instanceof BlockItem) {
                total += stack.getCount();
            }
        }

        // main inventory
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && stack.getItem() instanceof BlockItem) {
                total += stack.getCount();
            }
        }

        return total;
    }

    /**
     * get first slot with block item and its stack
     */
    public ItemStack getBlockSlotWithItem() {
        if (mc.player == null)
            return null;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && stack.getItem() instanceof BlockItem) {
                return stack;
            }
        }
        return null;
    }

    /**
     * get block slot index
     */
    public int getBlockSlotIndex() {
        return blockSlot != -1 ? blockSlot : mc.player.getInventory().getSelectedSlot();
    }

    /**
     * send block place packet with silent switch wrapping
     */
    public void sendBlockPlacePacket(PacketHelper.Values pc, ServerboundUseItemOnPacket placePacket) {
        if (SilentSwitch.isEnabled() && blockSlot != -1) {
            int currentSlot = mc.player.getInventory().getSelectedSlot();
            if (currentSlot != blockSlot) {
                // switch to block, place, switch back
                pc.sendPacket(new ServerboundSetCarriedItemPacket(blockSlot));
                pc.sendPacket(placePacket);
                pc.sendPacket(new ServerboundSetCarriedItemPacket(currentSlot));
                return;
            }
        }
        // normal send
        pc.sendPacket(placePacket);
    }

    private boolean placeLegit(BlockHitResult hit) {
        if (mc.player == null || mc.gameMode == null) {
            return false;
        }

        if (RequireUseKey.isEnabled() && !mc.options.keyUse.isDown()) {
            return false;
        }

        if (!placeTimer.hasReached(PlaceDelayMs.getValue())) {
            return false;
        }
        placeTimer.reset();

        int prevSlot = mc.player.getInventory().getSelectedSlot();
        boolean switched = false;

        // If silent switch is enabled, the server is holding blocks, but the client hand may not.
        // For legit placement + animations/sound, temporarily switch client slot as well.
        if (SilentSwitch.isEnabled() && blockSlot != -1 && prevSlot != blockSlot) {
            mc.player.getInventory().setSelectedSlot(blockSlot);
            if (mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(blockSlot));
            }
            switched = true;
        }

        InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);

        // Always show local swing if we attempted to place.
        mc.player.swing(InteractionHand.MAIN_HAND);

        if (switched) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
            if (mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(prevSlot));
            }
        }

        return result != null && result.consumesAction();
    }
}
