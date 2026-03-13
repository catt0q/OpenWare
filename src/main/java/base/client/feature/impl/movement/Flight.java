package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.*;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.world.EventBlockShape;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.flights.grim.GrimFlightGlide;
import base.client.feature.impl.movement.flights.matrix.MatrixRiksFlight;
import base.client.feature.impl.movement.flights.matrix.MatrixTimerlessFlight;
import base.client.feature.impl.movement.flights.vanilla.MotionFlight;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import base.client.managers.TranslationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import base.mixin.client.accessors.LivingEntityAccessor;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

import static base.client.feature.impl.exploit.Disabler.notimerbalance;

public class Flight extends Module {
    public static TimerHelper flyTimer = new TimerHelper();

    // Packet rate limiter - configurable via settings
    private static final int PACKETS_TO_RELEASE_PER_TICK = 8; // smooth release rate
    private static int[] packetHistory;
    private static int historyIndex = 0;
    private static Queue<Packet<?>> packetQueue = new LinkedList<>();

    ArrayDeque<LatePacket> SavedFlagPackets = new ArrayDeque<LatePacket>();
    boolean shouddelay = false;
    static boolean canjump = false;
    ArrayDeque<Packet> ServerPackets = new ArrayDeque<Packet>();
    public static ArrayDeque<Packet> CombatPackets = new ArrayDeque<Packet>();
    public static ModeSetting Mode, PMode, MMode, VMode, GMode;
    public static BooleanSetting MMDFOD, AutoDamage, OldJumpDamage, NewTestDamage, LatestDamage, InfDamage,
            NoFallDamage, JumpWait, DamageFloatJumpControl, Ver188MM, MFRotFix, MRVNOKICK, SpoofGround, OnGround,
            NoCliping,
            MotionReset, PacketLimiter, LagSilent, LagStrafe, LagAutoToggle, LagResetMotion, LagNoHeadMove;
    public static NumberSetting RFHorSpeed, RFHorSpeedBoost, MDFDuration, DamageTimer, FlyTimer, TimerlessPackets,
            MFPacketCount, MVPacketCount, LowTimer, HorSpeed, SHorSpeed, VerSpeed, PacketMax, PacketTicks,
            LagTimer1, LagTimer2, LagSpeed, LagBoostSpeed, LagTicks;

    public static int index1 = 0, index2 = 0, index3 = 0, index4 = 0, index5 = 0, index6 = 0, index7 = 0;
    double indexd6 = 0, indexd7 = 0;

    // Lag mode variables
    private boolean lagRelease = false;
    private boolean lagBoost = false;
    private boolean lagFlag = false;
    private boolean lagDoBoost = false;
    private boolean lagSend = false;
    private boolean lagCanStrafe = false;
    private int lagOffGroundTicks = 0;
    private int lagTicksSinceFlag = 0;

    // NewTestDamage state: 0 = waiting for first jump, 1 = first jump done (old
    // jump damage),
    // 2 = falling near ground (trigger MatrixSW), 3 = damage taken, flight active
    private int newTestDamagePhase = 0;
    private int newTestDamageJumps = 0;

    // LatestDamage (ported from SelfDamage NewGlitch)
    private boolean latestDamageDid = false;
    private int latestDamageDelayTicks = 0;
    private final TimerHelper infDamageTimer = new TimerHelper();

    int flydelay = 0;
    int flytype = 0;

    double lasttpposX = 0;
    double lasttpposY = 0;
    double lasttpposZ = 0;

    float fixedyaw, fixedpitch;

    boolean startfly = false;
    private BlockPos bp = null;
    public Block prevblock = null;

    double startposY = 0;
    public static int groundstate = 0;// 0=nothing 1=noground 2=ground

    public static boolean vmo = false;

    public Flight() {
        super("Flight", "Позволяет вам летать без креатив режима", Type.Movement);
        Mode = new ModeSetting("Mode", "Polar", () -> true, "Polar", "Matrix", "Vanilla", "Grim");

        // =====================================================================================================
        VMode = new ModeSetting("Vanilla Mode", "Motion", () -> Mode.getCurrentMode().equals("Vanilla"), "Motion",
                "AirWalk");
        vmo = (VMode.isVisible()
                && (VMode.getCurrentMode().equals("Motion") || VMode.getCurrentMode().equals("AirWalk")));

        SpoofGround = new BooleanSetting("SpoofGround", true,
                () -> VMode.isVisible() && VMode.getCurrentMode().equals("AirWalk"));
        OnGround = new BooleanSetting("OnGround", true,
                () -> SpoofGround.isVisible() && SpoofGround.isEnabled());
        HorSpeed = new NumberSetting("H-Speed", 0.6F, 0.1F, 15F, 0.1F,
                () -> (VMode.isVisible() && VMode.getCurrentMode().equals("Motion")));
        SHorSpeed = new NumberSetting("Sprint H-Speed", 2F, 0.1F, 15F, 0.1F,
                () -> (VMode.isVisible() && VMode.getCurrentMode().equals("Motion")));
        VerSpeed = new NumberSetting("V-Speed", 0.5F, 0.1F, 15F, 0.1F,
                () -> (VMode.isVisible() && VMode.getCurrentMode().equals("Motion")));
        NoCliping = new BooleanSetting("NoClip", false,
                () -> (VMode.isVisible() && VMode.getCurrentMode().equals("Motion")));
        MotionReset = new BooleanSetting("MotionReset", false,
                () -> (VMode.isVisible() && VMode.getCurrentMode().equals("Motion")));

        // =====================================================================================================
        PMode = new ModeSetting("Polar Mode", "MishaDuels", () -> Mode.getCurrentMode().equals("Polar"), "MishaDuels",
                "Explosion");

        // =====================================================================================================
        GMode = new ModeSetting("Grim Mode", "Glide", () -> Mode.getCurrentMode().equals("Grim"), "Glide");
        // =====================================================================================================

        MMode = new ModeSetting("Matrix Mode", "Vanilla", () -> Mode.getCurrentMode().equals("Matrix"), "Vanilla",
                "Vanilla2", "Jump7.16.0", "OldJump", "DamageFloat", "Timerless7.16.2", "BBGS", "Duel", "Lag");
        AutoDamage = new BooleanSetting("NoWorkDmg", true,
                () -> (MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat")));

        MDFDuration = new NumberSetting("DF Dur", 1400f, 100f, 3000f, 10,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat"));

        MMDFOD = new BooleanSetting("OldDmgIDK", false,
                () -> AutoDamage.isEnabled() && MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat"));
        DamageFloatJumpControl = new BooleanSetting("DONTUSE", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat"));
        OldJumpDamage = new BooleanSetting("SpamDamage", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat"));
        NewTestDamage = new BooleanSetting("DoubleDamage", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat") && !OldJumpDamage.isEnabled());
        LatestDamage = new BooleanSetting("LatestDamage", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat"));
        InfDamage = new BooleanSetting("HalalDamage", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat"));
        DamageTimer = new NumberSetting("DMG Timer", 1f, 1f, 15f, 0.01f,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("DamageFloat"));

        Ver188MM = new BooleanSetting("1.8.8", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Jump7.16.0"));

        NoFallDamage = new BooleanSetting("No Fall", true,
                () -> (MMode.isVisible() && MMode.getCurrentMode().equals("Jump7.16.0")));
        JumpWait = new BooleanSetting("Jump Wait", false,
                () -> (MMode.isVisible() && MMode.getCurrentMode().equals("Jump7.16.0"))
                        || (MMode.isVisible() && MMode.getCurrentMode().equals("OldJump")));

        FlyTimer = new NumberSetting("Timer", 1f, 1f, 25f, 0.01f, () ->

        (MMode.isVisible() && (MMode.getCurrentMode().equals("DamageFloat") ||
                MMode.getCurrentMode().equals("BBGS")))

        );

        TimerlessPackets = new NumberSetting("Timerless Packets", 7, 1, 10, 1,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Timerless7.16.2"));

        RFHorSpeed = new NumberSetting("Riks speed", 0.45F, 0.01F, 5.99F, 0.01F,
                () -> (MMode.getCurrentMode().equals("Duel") && Mode.getCurrentMode().equals("Matrix")));
        RFHorSpeedBoost = new NumberSetting("Riks boost speed", 2F, 0.01F, 9.99F, 0.01F,
                () -> (MMode.getCurrentMode().equals("Duel") && Mode.getCurrentMode().equals("Matrix")));
        MRVNOKICK = new BooleanSetting("Riks No Vanilla Kick", "НЕ КИКАЕМСЯ ДА", true, () -> MMode.isVisible());

        MFRotFix = new BooleanSetting("Float RotFix", "Это больше не везуал лол", true, () -> MMode.isVisible());

        MFPacketCount = new NumberSetting("Packets1", 20, 1, 50, 1, () -> MMode.isVisible());
        LowTimer = new NumberSetting("Low Timer", 0.3f, 0.01f, 1f, 0.01f, () -> MMode.isVisible());

        MVPacketCount = new NumberSetting("Packets2", 7, 1, 50, 1, () -> MMode.isVisible());

        // Packet rate limiter settings
        PacketLimiter = new BooleanSetting("PacketLimiter", true,
                () -> MMode.isVisible()
                        && (MMode.getCurrentMode().equals("Vanilla") || MMode.getCurrentMode().equals("Vanilla2")));
        PacketMax = new NumberSetting("PacketMax", 120, 1, 800, 1,
                () -> PacketLimiter.isVisible() && PacketLimiter.isEnabled());
        PacketTicks = new NumberSetting("PacketTicks", 15, 1, 100, 1,
                () -> PacketLimiter.isVisible() && PacketLimiter.isEnabled());

        // Initialize packet history array
        packetHistory = new int[15];

        // Lag mode settings
        LagTimer1 = new NumberSetting("LagTimer", 1.0f, 0.1f, 1.0f, 0.01f,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagTimer2 = new NumberSetting("LaunchTimer", 1.0f, 0.1f, 1.0f, 0.01f,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagSpeed = new NumberSetting("LagSpeed", 1.4f, -2.4f, 2.4f, 0.01f,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagBoostSpeed = new NumberSetting("BoostSpeed", 8.3f, 0.0f, 10.0f, 0.01f,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagTicks = new NumberSetting("LagTicks", 5, 1, 12, 1,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagNoHeadMove = new BooleanSetting("NoHeadMove", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagSilent = new BooleanSetting("LagSilent", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagStrafe = new BooleanSetting("LagStrafe", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagAutoToggle = new BooleanSetting("LagAutoToggle", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag"));
        LagResetMotion = new BooleanSetting("LagResetMotion", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Lag") && !LagAutoToggle.isEnabled());

        // =====================================================================================================

        addSettings(Mode, PMode, MMode, VMode, GMode,

                TimerlessPackets,

                SpoofGround, OnGround, HorSpeed, SHorSpeed, VerSpeed, NoCliping, MotionReset, // Vanilla Motion

                RFHorSpeed, RFHorSpeedBoost, MRVNOKICK, // Riks

                JumpWait, NoFallDamage, // Jump

                AutoDamage, MMDFOD, DamageFloatJumpControl, OldJumpDamage, NewTestDamage, LatestDamage, InfDamage, MDFDuration, // DamageFloat

                MFRotFix, MFPacketCount, MVPacketCount, // Matrix Vanilla

                PacketLimiter, PacketMax, PacketTicks, // Packet Rate Limiter

                LagTimer1, LagTimer2, LagSpeed, LagBoostSpeed, LagTicks, // Lag mode
                LagNoHeadMove, LagSilent, LagStrafe, LagAutoToggle, LagResetMotion, // Lag mode toggles

                LowTimer, DamageTimer,

                FlyTimer);
    }

    // Packet rate limiter methods
    private static int getPacketsInWindow() {
        if (packetHistory == null)
            return 0;
        int total = 0;
        for (int count : packetHistory) {
            total += count;
        }
        return total;
    }

    private static void advanceTick() {
        if (packetHistory == null || PacketTicks == null)
            return;
        int window = (int) PacketTicks.getValue();
        if (packetHistory.length != window) {
            // Resize history array if setting changed
            int[] newHistory = new int[window];
            System.arraycopy(packetHistory, 0, newHistory, 0, Math.min(packetHistory.length, window));
            packetHistory = newHistory;
            historyIndex = 0;
        }
        historyIndex = (historyIndex + 1) % window;
        packetHistory[historyIndex] = 0;
    }

    private static boolean canSendPacket() {
        if (PacketLimiter == null || !PacketLimiter.isEnabled())
            return true;
        if (PacketMax == null)
            return true;
        return getPacketsInWindow() < (int) PacketMax.getValue();
    }

    private static void recordPacketSent() {
        if (packetHistory == null)
            return;
        packetHistory[historyIndex]++;
    }

    public static void sendRateLimitedPacket(Packet<?> packet, PacketHelper.Values pc) {
        if (PacketLimiter == null || !PacketLimiter.isEnabled()) {
            // Rate limiter disabled, send normally
            pc.sendPacket(packet, 10, true);
            return;
        }

        if (canSendPacket()) {
            pc.sendPacket(packet, 10, true);
            recordPacketSent();
        } else {
            // Queue for later
            if (packetQueue != null) {
                packetQueue.add(packet);
            }
        }
    }

    private static void processPacketQueue(PacketHelper.Values pc) {
        if (packetQueue == null || PacketLimiter == null || !PacketLimiter.isEnabled())
            return;

        int sent = 0;
        while (!packetQueue.isEmpty() && canSendPacket() && sent < PACKETS_TO_RELEASE_PER_TICK) {
            Packet<?> packet = packetQueue.poll();
            pc.sendPacket(packet, 10, true);
            recordPacketSent();
            sent++;
        }
    }

    private static void resetRateLimiter() {
        if (PacketTicks != null) {
            packetHistory = new int[(int) PacketTicks.getValue()];
        } else {
            packetHistory = new int[15];
        }
        historyIndex = 0;
        if (packetQueue != null) {
            packetQueue.clear();
        }
    }

    // old jump damage: 8-iteration vehicle packet loop with 0.42/-0.09/-0.329 Y
    // offsets
    private void doOldJumpDamage() {
        PacketHelper.Values pc = Client.instance.packet;
        for (int i = 0; i < 8; i++) {
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
            EntityUtil.dtp(0, 0.42, 0);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                    mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                    mc.player.horizontalCollision), 10, true);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
            EntityUtil.dtp(0, -0.09, 0);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                    mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                    mc.player.horizontalCollision), 10, true);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
            EntityUtil.dtp(0, -0.329, 0);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                    mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                    mc.player.horizontalCollision), 10, true);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
        }
        MoveUtil.limit2speed(0.1);
    }

    // MatrixSW self damage: ascending Y offset sequence ported from SelfDamage.java
    private void doMatrixSWDamage() {
        PacketHelper.Values pc = Client.instance.packet;
        for (int i = 0; i < 3; i++) {
            ACUtil.sendOffsetPosition(0.00099999999);
            ACUtil.sendOffsetPosition(0.4209999868869758);
            ACUtil.sendOffsetPosition(0.7541999805212001);
            ACUtil.sendOffsetPosition(1.0023359791121464);
            ACUtil.sendOffsetPosition(1.1671092609382114);
            ACUtil.sendOffsetPosition(1.2501870787446805);
            ACUtil.sendOffsetPosition(1.2532033402537266);
            ACUtil.sendOffsetPosition(1.1777592750642398);
            ACUtil.sendOffsetPosition(1.0254240882136827);
            ACUtil.sendOffsetPosition(0.7977356006686946);
            ACUtil.sendOffsetPosition(0.4962008770059114);
            ACUtil.sendOffsetPosition(0.12229684053919243);
        }
        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                true, mc.player.horizontalCollision), 10);
    }

    private void doLatestDamage(PacketHelper.Values pc) {
        if (mc.player == null) return;
        for (int i = 0; i < 50; i++) {
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    (float) (pc.LastYaw + Math.random() * 0.1),
                    (float) (pc.LastPitch + Math.random() * 0.1),
                    false, mc.player.horizontalCollision), 10);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(
                    mc.player.getX(), mc.player.getY() + 0.0624 - Math.random() * 0.0001, mc.player.getZ(),
                    (float) (pc.LastYaw + Math.random() * 0.1),
                    (float) (pc.LastPitch + Math.random() * 0.1),
                    false, mc.player.horizontalCollision), 10);
        }
        pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                pc.LastYaw, pc.LastPitch,
                true, mc.player.horizontalCollision), 10);
    }

    public void onEnable() {
        PacketHelper.Values pc = Client.instance.packet;
        resetRateLimiter(); // Reset packet rate limiter
        shouddelay = false;
        canjump = false;
        ServerPackets.clear();
        SavedFlagPackets.clear();
        flyTimer.reset();
        startfly = false;
        bp = null;
        Block prevblock = null;
        index1 = 0;
        index2 = 0;
        index3 = 0;
        index4 = 0;
        index5 = 0;
        index6 = 0;
        index7 = 0;
        indexd6 = 0;
        indexd7 = 0;
        flydelay = 0;
        flytype = 0;
        latestDamageDid = false;
        latestDamageDelayTicks = 0;
        infDamageTimer.reset();
        lasttpposX = 0;
        lasttpposY = 0;
        lasttpposZ = 0;
        startposY = mc.player.getY();
        groundstate = 0;
        switch (Mode.getCurrentMode()) {

            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("Duel"):
                        MatrixRiksFlight.onenable();
                        break;
                    case ("Timerless7.16.2"):

                        if (!Disabler.notimerbalance()) {
                            ChatHelper.addTranslatedMessage("timer.balance_zero");
                            NotificationManager.publicity("Error",
                                    ChatFormatting.RED + TranslationManager.get("timer.balance_zero"), 4,
                                    NotificationType.ERROR);

                            Client.instance.featureManager.getModuleByClass(Flight.class).toggle();
                            return;
                        }
                        MatrixTimerlessFlight.onenable();
                        break;

                }

                break;

            case ("Grim"):
                switch (GMode.getCurrentMode()) {
                    case ("Glide"):
                        GrimFlightGlide.onenable();
                        break;
                }

                break;

        }

        if (MMode.isVisible()) {
            if (MMode.getCurrentMode().equals("DamageFloat")) {
                startposY = 0;
                index1 = 13;

                if (LatestDamage.isEnabled()) {
                    if (mc.player.onGround() && mc.player.verticalCollision) {
                        jump();
                    }
                    latestDamageDelayTicks = 7;
                }
            } else if (MMode.getCurrentMode().equals("Vanilla")) {
                fixedyaw = pc.LastYaw;
                fixedpitch = pc.LastPitch;
            } else if (MMode.getCurrentMode().equals("Vanilla2")) {
                fixedyaw = pc.LastYaw;
                fixedpitch = pc.LastPitch;
                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false, false), 10, true);
                pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

                ACUtil.send117DuplPacket();
            } else if (MMode.getCurrentMode().equals("Jump7.16.0")) {
                index1 = 15;
            } else if (MMode.getCurrentMode().equals("OldJump")) {
                index1 = 15;
            } else if (MMode.getCurrentMode().equals("Lag")) {
                // reset lag mode state
                lagRelease = false;
                lagBoost = false;
                lagFlag = false;
                lagDoBoost = false;
                lagSend = false;
                lagCanStrafe = false;
                lagOffGroundTicks = 0;
                lagTicksSinceFlag = 0;
                // auto jump to start flight
                if (mc.player.onGround()) {
                    jump();
                }
            }

        }

        CombatPackets.clear();

        super.onEnable();
    }

    public void onDisable() {
        PacketHelper.Values pc = Client.instance.packet;
        if (mc.player != null) {

            if (MotionReset.isVisible() && MotionReset.isEnabled()) {
                MoveUtil.stop3();
            }
            switch (Mode.getCurrentMode()) {
                case ("Grim"):
                    switch (GMode.getCurrentMode()) {
                        case ("Glide"):

                            break;
                    }

                    break;
            }
            if (MMode.isVisible() && MMode.getCurrentMode().equals("Vanilla")) {
                mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
            }
            if (MMode.isVisible() && MMode.getCurrentMode().equals("Vanilla2")) {

                // MoveUtil.setmotY(-0.078);
            }

            if (MMode.isVisible() && MMode.getCurrentMode().equals("Timerless7.16.2")) {
                int szzz = Client.instance.flagsch.getFlags().size();
                for (int i = 0; i < szzz; i++) {
                    Client.instance.flagsch.getFlags().removeLast();
                }

            }

            if (MMode.isVisible() && MMode.getCurrentMode().equals("Lag")) {
                if (!LagAutoToggle.isEnabled() && LagResetMotion.isEnabled() && lagBoost) {
                    MoveUtil.stop3();
                }
                lagRelease = false;
                lagBoost = false;
                lagFlag = false;
                lagDoBoost = false;
                lagSend = false;
                lagCanStrafe = false;
                lagOffGroundTicks = 0;
                lagTicksSinceFlag = 0;
            }

            // reset NewTestDamage phase state
            newTestDamagePhase = 0;
            newTestDamageJumps = 0;

            TimerUtil.setTimerspeed(1);
        }

        super.onDisable();
    }

    @EventTarget
    public void onJump(EventOnJump e) {
        // AirWalk cancels jumps
        if (Mode.getCurrentMode().equals("Vanilla") && VMode.getCurrentMode().equals("AirWalk")) {
            e.cancel();
            return;
        }
        if (canjump) {
            canjump = false;
        } else {
            e.cancel();
        }
    }

    @EventTarget
    public void onBlockShape(EventBlockShape e) {
        // AirWalk: make blocks below player have full collision
        if (Mode.getCurrentMode().equals("Vanilla") && VMode.getCurrentMode().equals("AirWalk")) {
            if (!(e.getState().getBlock() instanceof LiquidBlock) && e.getPos().getY() < mc.player.getY()) {
                e.setShape(Shapes.block());
            }
        }
    }

    @EventTarget
    public void onCollide(EventCollideEntity e) {
        if (NoCliping.isVisible() && NoCliping.isEnabled()) {
            e.cancel();
        }
    }

    @EventTarget
    public void onPush(EventPushOutOfBlocks e) {
        if (NoCliping.isVisible() && NoCliping.isEnabled()) {
            e.cancel();
        }
    }

    @EventTarget
    public void onMove(EventMove e) {
        // lag flight silent mode: cancel movement and freeze position
        if (Mode.getCurrentMode().equals("Matrix") && MMode.getCurrentMode().equals("Lag")) {
            if (LagSilent.isEnabled() && lagDoBoost && !lagFlag) {
                e.setSafeWalk(true);
                e.setCancelled(true);
                MoveUtil.stop3();
            }
        }
    }

    @EventTarget
    public void eom(EventOnMovePost e) {
        PacketHelper.Values pc = Client.instance.packet;
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {
            xt = 0;
            zt = 0;
        }
        if (MoveUtil.isFreezing) {
            return;
        }
        switch (Mode.getCurrentMode()) {
            case ("Grim"):
                switch (GMode.getCurrentMode()) {
                    case ("Glide"):
                        GrimFlightGlide.onEventOnMovePost();
                        break;
                }

                break;
            case ("Vanilla"):
                switch (VMode.getCurrentMode()) {
                    case ("Motion"):
                        MotionFlight.onEventOnMovePost();
                        break;
                }
                break;

            case ("Polar"):
                switch (PMode.getCurrentMode()) {
                    case ("MishaDuels"):
                        if (!mc.player.verticalCollision && index1 == 0) {
                            index1 = 2;
                        }

                        if (index1 == 0) {
                            // TimerUtil.setTimerspeed(0.1f);
                            jump2();

                            // horizontal in-air strafe (WASD), not just forward
                            MoveUtil.strafe(1);

                            // vertical control
                            if (mc.options.keyJump.isDown()) {
                                MoveUtil.setmotY(0.21);
                            } else if (mc.options.keyShift.isDown()) {
                                MoveUtil.setmotY(-0.5);
                            }
                        }

                        if (index3 > 0) {
                            index3--;
                            if (index3 == 0)
                                TimerUtil.setTimerspeed(1f);
                        }

                        if (index1 == 2) {
                            // TimerUtil.setTimerspeed(0.2f);
                            jump2();
                            MoveUtil.strafe(1);

                            if (mc.options.keyJump.isDown()) {
                                MoveUtil.setmotY(0.21);
                            } else if (mc.options.keyShift.isDown()) {
                                MoveUtil.setmotY(-0.5);
                            }
                            // TimerUtil.setTimerspeed(0.5f);
                        }

                        int i = 5;

                        if (MoveUtil.motYstate() < 0) {
                            i = 13;
                        } else if (MoveUtil.motYstate() > 0) {
                            i = 6;
                        }
                        if (index2 == 1) {
                            // TimerUtil.setTimerspeed(0.1f);
                            MoveUtil.setmotY(1);
                            MoveUtil.strafe(1);
                            index2 = 0;
                            index3 = 3;
                        }
                        // if(index3>0){ TimerUtil.setTimerspeed(3); }

                        if (index1 >= i) {
                            index1 = -1;
                        }
                        index1++;
                        break;
                    case ("Explosion"):
                        if (!mc.player.verticalCollision && index1 == 0) {
                            index1 = 2;
                        }

                        if (index1 == 0) {
                            TimerUtil.setTimerspeed(0.1f);
                            jump();

                        }

                        if (index3 > 0) {
                            index3--;
                            if (index3 == 0)
                                TimerUtil.setTimerspeed(1f);
                        }

                        if (index1 == 2) {
                            TimerUtil.setTimerspeed(0.2f);
                            jump();
                            TimerUtil.setTimerspeed(0.5f);
                        }

                        int iper = 17;

                        if (MoveUtil.motYstate() < 0) {
                            iper = 25;
                        } else if (MoveUtil.motYstate() > 0) {
                            iper = 12;
                        }
                        if (index2 == 1) {
                            TimerUtil.setTimerspeed(0.1f);
                            MoveUtil.setmotY(1);
                            MoveUtil.strafe(5);
                            index2 = 0;
                            index3 = 3;
                        }
                        // if(index3>0){ TimerUtil.setTimerspeed(3); }

                        if (index1 >= iper) {
                            index1 = -1;
                        }
                        index1++;
                        break;
                }
                break;
            case ("Matrix"):
                boolean needJump = false;
                double deffval = 0.0624D;
                double mothor = 0;
                double motver = 0;
                double divval = 1.001;
                double nx = pc.LastPosX + 1000 + Math.random() * 10000;
                double nz = pc.LastPosZ + 1000 + Math.random() * 10000;
                switch (MMode.getCurrentMode()) {

                    case ("Timerless7.16.2"):
                        MatrixTimerlessFlight.onEventOnMovePost(e, xt, zt);
                        break;

                    case ("Vanilla"):

                        if (MoveUtil.motYstate() == 0) {
                            if (MoveUtil.getdir() != -1) {
                                mothor = deffval;
                            }
                        } else if (MoveUtil.motYstate() > 0) {
                            mothor = 0;
                            motver = deffval;
                            if (MoveUtil.getdir() != -1) {
                                motver = deffval / Math.sqrt(2) / divval;
                                mothor = deffval / Math.sqrt(2) / divval;
                            }
                        } else if (MoveUtil.motYstate() < 0) {
                            mothor = 0;
                            motver = -deffval;
                            if (MoveUtil.getdir() != -1) {
                                motver = -deffval / Math.sqrt(2) / divval;
                                mothor = deffval / Math.sqrt(2) / divval;
                            }
                        }

                        int tries = (int) (MFPacketCount.getValue());
                        if (pc.lastTptimer.hasTimeElapsed(4000, false)) {
                            flydelay = 3;
                        }
                        if (flydelay > 0) {
                            tries = (int) 1;

                            mothor = 0;
                            motver = 0;
                            // tries=(int) (MFPacketCount.getValue());
                        } else {
                            tries = (int) (MFPacketCount.getValue());
                        }

                        if ((mothor != 0 || motver != 0 || flydelay > 0)
                                && Client.instance.flagsch.getFlags().size() <= 0) {

                            if (MFRotFix.isEnabled()) {

                                sendRateLimitedPacket(
                                        new ServerboundMovePlayerPacket.Pos(nx, pc.LastPosY, nz, false, false), pc);

                                pc.LastTpNum++;
                                sendRateLimitedPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), pc);
                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                        pc.LastPosY, pc.LastPosZ, pc.LastTpNum, false, false));

                            }

                            pc.lastTptimer.reset();

                            for (int i = 0; i < tries; i++) {
                                /*
                                 * pc.sendPacket(flytype==1 ? new
                                 * ServerboundMovePlayerPacket.Position(pc.LastPosX+xt*mothor,
                                 * pc.LastPosY+motver, pc.LastPosZ+zt*mothor, false) : new
                                 * ServerboundMovePlayerPacket.PositionRotation(pc.LastPosX+xt*mothor,
                                 * pc.LastPosY+motver, pc.LastPosZ+zt*mothor,pc.LastYaw,pc.LastPitch, false)
                                 * ,true);
                                 */
                                nx = pc.LastPosX + 90 + Math.random() * 90;
                                nz = pc.LastPosZ + 90 + Math.random() * 90;
                                if (index1 > 0) {
                                    sendRateLimitedPacket(
                                            new ServerboundMovePlayerPacket.Pos(pc.LastPosX + xt * mothor,
                                                    pc.LastPosY + motver, pc.LastPosZ + zt * mothor, false, false),
                                            pc);
                                }

                                sendRateLimitedPacket(
                                        new ServerboundMovePlayerPacket.PosRot(nx, pc.LastPosY, nz, pc.LastYaw,
                                                pc.LastPitch, false, false),
                                        pc);

                                pc.LastTpNum++;
                                sendRateLimitedPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), pc);
                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                        pc.LastPosY, pc.LastPosZ, pc.LastTpNum, false, false));
                                if (index1 > 0) {
                                    mc.player.setPos(pc.LastPosX + xt * mothor, pc.LastPosY + motver,
                                            pc.LastPosZ + zt * mothor);
                                    pc.LastPosX = mc.player.getX();
                                    pc.LastPosY = mc.player.getY();
                                    pc.LastPosZ = mc.player.getZ();
                                }
                                Disabler.savedabusepacket--;
                            }

                            if (MFRotFix.isEnabled()) {
                                pc.LastYaw = fixedyaw;
                                pc.LastPitch = fixedpitch;
                                flytype = 0;
                                Disabler.savedabusepacket--;
                                sendRateLimitedPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY,
                                        pc.LastPosZ, pc.LastYaw, pc.LastPitch, false, false), pc);

                            }

                            index1++;
                        }
                        MoveUtil.stop3();
                        TimerUtil.setTimerspeed(LowTimer.getValue());
                        if (flydelay > 0) {
                            flydelay--;
                        }
                        break;
                    case ("Vanilla2"):

                        if (MoveUtil.motYstate() == 0) {
                            if (MoveUtil.getdir() != -1) {
                                mothor = deffval;
                            }
                        } else if (MoveUtil.motYstate() > 0) {
                            mothor = 0;
                            motver = deffval;
                            if (MoveUtil.getdir() != -1) {
                                motver = deffval / Math.sqrt(2) / divval;
                                mothor = deffval / Math.sqrt(2) / divval;
                            }
                        } else if (MoveUtil.motYstate() < 0) {
                            mothor = 0;
                            motver = -deffval;
                            if (MoveUtil.getdir() != -1) {
                                motver = -deffval / Math.sqrt(2) / divval;
                                mothor = deffval / Math.sqrt(2) / divval;
                            }
                        }

                        int tries2 = (int) (MVPacketCount.getValue());

                        if ((mothor != 0 || motver != 0)) {

                            if (MFRotFix.isEnabled()) {

                                // pc.LastYaw=fixedyaw; pc.LastPitch=fixedpitch;
                                if (index5 == 1) {
                                    index5 = 0;

                                }

                            }

                            nx = pc.LastPosX + 90 + Math.random() * 90;
                            nz = pc.LastPosZ + 90 + Math.random() * 90;
                            // pc.sendPacket(new
                            // ServerboundMovePlayerPacket.PosRot(nx,pc.LastPosY,nz,pc.LastYaw,pc.LastPitch,false,false),10,true);

                            // pc.LastTpNum++; pc.sendPacket(new
                            // ServerboundAcceptTeleportationPacket(pc.LastTpNum));
                            // Client.instance.flagsch.getFlags().addFirst(new
                            // FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY,
                            // pc.LastPosZ,pc.LastTpNum,true,true));
                            // pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(),
                            // mc.player.getY(), mc.player.getZ(), false, false), 10, true);

                            sendRateLimitedPacket(
                                    new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(),
                                            mc.player.getZ(), pc.LastYaw, pc.LastPitch, false, false),
                                    pc);

                            sendRateLimitedPacket(new ServerboundMoveVehiclePacket(
                                    new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ()),
                                    pc.LastYaw, pc.LastPitch, false), pc);

                            // pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(),
                            // mc.player.getY(), mc.player.getZ(), false, false), 10, true);

                            sendRateLimitedPacket(
                                    new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(),
                                            mc.player.getZ(), pc.LastYaw, pc.LastPitch, false, false),
                                    pc);

                            for (int i = 0; i < tries2; i++) {

                                EntityUtil.dtp(xt * mothor, motver, zt * mothor);

                                sendRateLimitedPacket(new ServerboundMoveVehiclePacket(
                                        new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ()),
                                        pc.LastYaw, pc.LastPitch, false), pc);

                                Disabler.savedabusepacket--;
                                sendRateLimitedPacket(
                                        new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(),
                                                mc.player.getZ(), false, false),
                                        pc);

                                pc.LastPosX = mc.player.getX();
                                pc.LastPosY = mc.player.getY();
                                pc.LastPosZ = mc.player.getZ();

                            }

                            if (MFRotFix.isEnabled()) {
                                // pc.LastYaw=fixedyaw; pc.LastPitch=fixedpitch;
                                // pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX,
                                // pc.LastPosY, pc.LastPosZ,pc.LastYaw,pc.LastPitch, false,false) ,10,true);

                                // pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY,
                                // pc.LastPosZ, true, mc.player.horizontalCollision), 10, true);
                                // pc.sendPacket(new ServerboundMovePlayerPacket.Rot(pc.LastYaw,pc.LastPitch,
                                // true, mc.player.horizontalCollision), 10, true);
                                // pc.LastYaw=fixedyaw; pc.LastPitch=fixedpitch;

                                // pc.LastYaw=fixedyaw; pc.LastPitch=fixedpitch;

                                // mc.player.setPos(pc.LastPosX, pc.LastPosY-0.078, pc.LastPosZ);
                                // pc.LastPosX=mc.player.getX(); pc.LastPosY=mc.player.getY();
                                // pc.LastPosZ=mc.player.getZ();

                                index5 = 1;
                            }

                            index1++;
                        }
                        MoveUtil.stop3();
                        TimerUtil.setTimerspeed(LowTimer.getValue());
                        break;
                    case ("Duel"):
                        MatrixRiksFlight.onEventOnMovePost(e, xt, zt);

                        break;

                    case ("Jump7.16.0"):
                        needJump = ((index1 >= 5 && MoveUtil.motYstate() == 1)
                                || (index1 >= 11 && MoveUtil.motYstate() == 0)
                                || (index1 >= 14 && MoveUtil.motYstate() == -1));

                        if (Client.instance.flagsch.getFlags().size() <= 0 || !JumpWait.isEnabled()) {

                            if (needJump) {
                                index1 = 0;
                                MoveUtil.setmotY(0.42);

                                pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                pc.LastTpNum++;
                                pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                        pc.LastPosY, pc.LastPosZ, pc.LastTpNum, false, true));
                                if (NoFallDamage.isEnabled()) {
                                    CombatUtil.fallDistance = 0;
                                    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY,
                                            pc.LastPosZ, true, mc.player.horizontalCollision), 10, true);
                                }

                                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,
                                        mc.player.horizontalCollision), 10, true);

                                pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                pc.LastTpNum++;
                                pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                        pc.LastPosY, pc.LastPosZ, pc.LastTpNum, false, true));

                                if (notimerbalance()) {
                                    MoveUtil.smartstrafe(0.195);
                                    if (MoveUtil.motYstate() == 1) {
                                        MoveUtil.smartstrafe(0.1);
                                    } else if (MoveUtil.motYstate() < 0) {
                                        MoveUtil.smartstrafe(0.1);
                                    }
                                } else {

                                    MoveUtil.smartstrafe(0.3565);

                                }

                            }
                        } else if (Client.instance.flagsch.getFlags().size() > 0 && needJump) {
                            MoveUtil.stop3();
                        }
                        if (MoveUtil.motYstate() >= 0) {

                            if (index1 > 1) {
                                MoveUtil.minsmartstrafe(0.199);
                            }
                        }

                        pc.LastPosX = mc.player.getX();
                        pc.LastPosY = mc.player.getY();
                        pc.LastPosZ = mc.player.getZ();
                        index1++;
                        break;

                    case ("OldJump"):
                        needJump = ((index1 >= 3 && MoveUtil.motYstate() == 1)
                                || (index1 >= 11 && MoveUtil.motYstate() == 0)
                                || (index1 >= 15 && MoveUtil.motYstate() == -1));
                        if (Client.instance.flagsch.getFlags().size() <= 0 || !JumpWait.isEnabled()) {

                            if (needJump) {
                                index1 = 0;
                                pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                pc.LastTpNum++;
                                pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));
                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX,
                                        pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, true));

                                mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ,
                                        true, mc.player.horizontalCollision), 10, true);
                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ,
                                        false, mc.player.horizontalCollision), 10, true);

                                MoveUtil.setmotY(0.42);
                                MoveUtil.minsmartstrafe(0.199);
                            }
                        } else if (Client.instance.flagsch.getFlags().size() > 0 && needJump) {
                            MoveUtil.stop3();
                        }

                        pc.LastPosX = mc.player.getX();
                        pc.LastPosY = mc.player.getY();
                        pc.LastPosZ = mc.player.getZ();
                        index1++;
                        break;
                    case ("DamageFloat"):

                        if (LatestDamage.isEnabled() && !latestDamageDid) {
                            if (latestDamageDelayTicks > 0) {
                                latestDamageDelayTicks--;
                            } else {
                                doLatestDamage(pc);
                                latestDamageDid = true;
                            }
                        }

                        // InfDamage: same NewGlitch self-damage, repeating every 1.5s (no jump)
                        if (InfDamage.isEnabled() && infDamageTimer.hasTimeElapsed(1500, true)) {
                            doLatestDamage(pc);
                        }

                        // handle OldJumpDamage - fires old jump damage once per landing (like original
                        // DamageFloatJumpControl)
                        if (OldJumpDamage.isEnabled()) {
                            if (index1 >= 7 && mc.player.onGround() && mc.player.verticalCollision) {
                                doOldJumpDamage();
                                mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
                                MoveUtil.setmotY(0.42);
                                if (notimerbalance()) {
                                    MoveUtil.limit2speed(0.199);
                                }
                                index1 = 0;
                                flydelay = 4;
                                flytype = 1;
                            }
                        }

                        // handle NewTestDamage - proper sequence:
                        // phase 0: on ground, fire old jump damage ONCE to trigger jump
                        // phase 1: counting jumps - wait for 2 complete ground landings
                        // phase 2: on 3rd jump, when falling mid-air near ground, fire MatrixSW ONCE
                        // phase 3: flight active gay
                        if (NewTestDamage.isEnabled() && !OldJumpDamage.isEnabled()) {
                            boolean isOnGround = mc.player.onGround() && mc.player.verticalCollision;
                            boolean wasOnGroundLastTick = newTestDamageJumps < 0; // negative = was on ground

                            if (newTestDamagePhase == 0) {
                                // phase 0: waiting on ground to fire initial old jump damage
                                if (isOnGround) {
                                    // fire old jump damage ONCE to create the initial jump
                                    doOldJumpDamage();
                                    mc.player.setPos(Client.instance.packet.LastPosX, Client.instance.packet.LastPosY,
                                            Client.instance.packet.LastPosZ);
                                    jump();
                                    newTestDamagePhase = 1;
                                    newTestDamageJumps = -1; // start counting, on ground
                                }
                            } else if (newTestDamagePhase == 1) {
                                // phase 1: counting ground landings
                                // when we land (air->ground), that's a completed jump
                                if (!wasOnGroundLastTick && isOnGround) {
                                    // just landed - fire old jump damage again to continue
                                    doOldJumpDamage();
                                    mc.player.setPos(Client.instance.packet.LastPosX, Client.instance.packet.LastPosY,
                                            Client.instance.packet.LastPosZ);
                                    jump();
                                    int landCount = Math.abs(newTestDamageJumps) + 1;
                                    newTestDamageJumps = -landCount; // still on ground

                                    // after 2 landings, next phase
                                    if (landCount >= 2) {
                                        newTestDamagePhase = 2;
                                    }
                                }

                                // update ground state for next tick
                                if (isOnGround) {
                                    newTestDamageJumps = -Math.abs(newTestDamageJumps);
                                } else {
                                    newTestDamageJumps = Math.abs(newTestDamageJumps);
                                }
                            } else if (newTestDamagePhase == 2) {
                                // phase 2: on 3rd jump, wait until falling mid-air near ground
                                double yVel = mc.player.getDeltaMovement().y;
                                if (!isOnGround && yVel < -0.1 && mc.player.fallDistance > 0.5) {
                                    // falling mid-air near ground - fire MatrixSW (3 cycles)
                                    doMatrixSWDamage();

                                    // activate flight phase
                                    newTestDamagePhase = 3;
                                    flydelay = 4;
                                    flytype = 1;
                                    index2 = 1; // skip to flight phase
                                }

                                // update ground state
                                if (isOnGround) {
                                    newTestDamageJumps = -Math.abs(newTestDamageJumps);
                                } else {
                                    newTestDamageJumps = Math.abs(newTestDamageJumps);
                                }
                            }
                            // phase 3: flight active, handled by normal DamageFloat flight logic below
                        }

                        // skip normal AutoDamage logic if either new option is enabled
                        if (OldJumpDamage.isEnabled() || NewTestDamage.isEnabled()) {
                            // still handle flight phase after damage but skip AutoDamage section
                            flydelay = 4;
                            flytype = 1;
                        } else if (!AutoDamage.isEnabled()) {
                            flydelay = 4;
                            flytype = 1;
                        } else {
                            // Auto damage using MatrixFastExploit method
                            if (index1 == 13 && mc.player.onGround() && mc.player.verticalCollision) {
                                // Perform MatrixFastExploit damage sequence
                                for (int i = 0; i < 8; i++) {
                                    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                    EntityUtil.dtp(0, 0.42, 0);
                                    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                    pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                                            mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                                            mc.player.horizontalCollision), 10, true);
                                    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                    EntityUtil.dtp(0, -0.09, 0);
                                    pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                                            mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                                            mc.player.horizontalCollision), 10, true);
                                    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                    EntityUtil.dtp(0, -0.329, 0);
                                    pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                                            mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                                            mc.player.horizontalCollision), 10, true);
                                    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                }
                                MoveUtil.limit2speed(0.1);
                                // Set up for flight phase
                                flydelay = 4;
                                flytype = 1;
                                index1 = 0;
                            }
                            groundstate = 1;
                            mc.player.setOnGround(false);
                        }
                        boolean bjump = (DamageFloatJumpControl.isEnabled() && mc.options.keyJump.isDown());
        // LatestDamage: run NewGlitch self-damage (no limits) and activate on damage
        boolean latestDamageActivate = LatestDamage.isEnabled() && mc.player != null
                && mc.player.hurtTime > 0;
                        if (flydelay < 3 && (CombatUtil.ofallDistance < 3.3 || bjump || latestDamageActivate)
                                && index2 == 0) {
                            if (bjump) {
                                if (index1 >= 7) {
                                    TimerUtil.setTimerspeed(1);
                                    flydelay = 0;
                                    index1 = 0;
                                    startposY = 0;
                                    CombatUtil.resetfd();
                                    for (int i = 0; i < 8; i++) {
                                        pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                        EntityUtil.dtp(0, 0.42, 0);
                                        pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                        pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                                                mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                                                mc.player.horizontalCollision), 10, true);
                                        pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                        EntityUtil.dtp(0, -0.09, 0);
                                        pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                                                mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                                                mc.player.horizontalCollision), 10, true);
                                        pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                                        EntityUtil.dtp(0, -0.329, 0);
                                        pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                                                mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, false,
                                                mc.player.horizontalCollision), 10, true);
                                        pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);

                                    }
                                    MoveUtil.limit2speed(0.1);

                                    mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
                                    MoveUtil.setmotY(0.42);
                                    if (notimerbalance()) {
                                        MoveUtil.limit2speed(0.199);
                                    }
                                }
                            } else {

                                if ((mc.player.verticalCollision && mc.player.onGround()) || index1 >= 12) {
                                    TimerUtil.setTimerspeed(DamageTimer.getValue());

                                    pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                    pc.LastTpNum++;
                                    pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                                    Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                            pc.LastPosY, pc.LastPosZ, pc.LastTpNum, true, true));

                                    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY,
                                            pc.LastPosZ, false, mc.player.horizontalCollision), 10, true);
                                    // pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,
                                    // mc.player.horizontalCollision),10, true);

                                    Disabler.savedabusepacket--;

                                    pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                    pc.LastTpNum++;
                                    pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                                    Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX,
                                            pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, false));
                                    MoveUtil.stop3();
                                    MoveUtil.setmotY(0.42);
                                    flydelay++;
                                    index1 = 0;

                                }
                            }
                        } else if (flydelay == 3
                                || (CombatUtil.ofallDistance >= 3.3 || latestDamageActivate) && index2 == 0) {
                            startposY += CombatUtil.ofallDistance;
                            CombatUtil.resetfd();
                            // LatestDamage: bypass fall distance thresholds when taking damage
                            if (startposY >= 0.5 || CombatUtil.ofallDistance >= 3.3 || latestDamageActivate) {
                                if (MMDFOD.isEnabled()) {
                                    pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                    pc.LastTpNum++;
                                    pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                                    Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX,
                                            pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, true));
                                    mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
                                    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY,
                                            pc.LastPosZ, true, mc.player.horizontalCollision), 10, true);
                                    // pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY,
                                    // pc.LastPosZ, false, mc.player.horizontalCollision),10, true);
                                } else {
                                    pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                    pc.LastTpNum++;
                                    pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                                    Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX,
                                            pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, false));
                                    pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true,
                                            mc.player.horizontalCollision), 10, true);
                                }

                                TimerUtil.setTimerspeed(1);
                                flydelay++;
                                index2 = 1;
                                index1 = 0;
                            }

                        } else if (index2 == 1) {

                            if (!pc.lastVeltimer.hasTimeElapsed((long) MDFDuration.getValue(), false)) {

                                flytype = 1;
                                // MoveUtil.setmotY(Math.max(mc.player.getDeltaMovement().y,-0.000001 -
                                // Math.random() * 0.000001));
                                MoveUtil.setmotY(-Math.random() * 0.000011);
                                TimerUtil.setTimerspeed(FlyTimer.getValue());
                                boolean diag = MoveUtil.getmf() != 0 && MoveUtil.getms() != 0;
                                if (!mc.player.isUsingItem() && !mc.player.isShiftKeyDown()) {
                                    if (MoveUtil.getspeed2() < 0.2) {
                                        MoveUtil.minsmartstrafe(0.1999);
                                    }
                                }
                                MoveUtil.minstrafe(0.063 + Math.random() * 0.00001);
                            } else if ((flytype == 1 || index1 >= 11)) {
                                this.state = false;
                                this.onDisable();
                            }

                        }

                        if (flytype == 0) {
                            if (!(DamageFloatJumpControl.isEnabled() && mc.options.keyJump.isDown() && flydelay < 3)) {
                                MoveUtil.stop2();
                            }
                            if (flydelay == 4) {
                                MoveUtil.stop3();
                            }
                        }

                        // close the else-if chain from OldJumpDamage/NewTestDamage check
                        index1++;
                        break;

                    case ("Lag"):
                        // lag mode: flag-triggered flight
                        // increment ticks since flag every tick (emulates FlagHandler.ticksSinceFlag)
                        lagTicksSinceFlag++;

                        // strafe check (emulates StrafeEvent timing from original)
                        if (mc.player.onGround() || lagTicksSinceFlag >= 100) {
                            lagCanStrafe = false;
                        }

                        // strafe logic while flying (emulates StrafeEvent from original)
                        if (lagCanStrafe && LagStrafe.isEnabled() && MoveUtil.ismovinginput()) {
                            double shotSpeed = Math.sqrt(mc.player.getDeltaMovement().x * mc.player.getDeltaMovement().x
                                    + mc.player.getDeltaMovement().z * mc.player.getDeltaMovement().z);
                            double speed = shotSpeed * 0.9;
                            double motionX = mc.player.getDeltaMovement().x * 0.09999999999999998;
                            double motionZ = mc.player.getDeltaMovement().z * 0.09999999999999998;
                            double yaw = MoveUtil.getdir();
                            MoveUtil.setmotX(-Math.sin(Math.toRadians(yaw)) * speed + motionX);
                            MoveUtil.setmotZ(Math.cos(Math.toRadians(yaw)) * speed + motionZ);
                        }

                        // track offground ticks (in main update section)
                        if (!mc.player.onGround()) {
                            lagOffGroundTicks++;
                        } else {
                            lagOffGroundTicks = 0;
                        }

                        // main flight phases
                        if (!(lagDoBoost || lagFlag && lagBoost)) {
                            if (lagRelease && lagOffGroundTicks >= (int) LagTicks.getValue()) {
                                lagDoBoost = true;
                            }
                            if (mc.player.fallDistance > 0.5f && !lagRelease) {
                                lagSend = true;
                            }
                            if (lagSend && !lagFlag) {
                                MoveUtil.strafe(LagSpeed.getValue());
                                MoveUtil.setmotY(0.42);
                            }
                            if (lagFlag && lagSend) {
                                MoveUtil.strafe(LagSpeed.getValue());
                                MoveUtil.setmotY(0.42);
                                lagOffGroundTicks = 0;
                                // also reset fallFlyTicks via accessor
                                ((LivingEntityAccessor) mc.player).setFallFlyTicks(0);
                                lagRelease = true;
                                lagFlag = false;
                                lagSend = false;
                                mc.player.resetFallDistance();
                            }
                        }

                        if (lagDoBoost) {
                            if (!LagSilent.isEnabled()) {
                                MoveUtil.strafe(LagBoostSpeed.getValue());
                                MoveUtil.setmotY(0.42);
                                lagBoost = true;
                                if (lagFlag) {
                                    if (LagAutoToggle.isEnabled()) {
                                        this.state = false;
                                        this.onDisable();
                                    } else {
                                        lagDoBoost = false;
                                    }
                                }
                            } else {
                                // silent mode: stop movement and reset to last tick position
                                MoveUtil.stop3();
                                mc.player.setPos(mc.player.xOld, mc.player.yOld, mc.player.zOld);
                            }
                        }

                        // timer manipulation - exactly like original
                        TimerUtil.setTimerspeed(
                                lagRelease ? (lagFlag ? LagTimer2.getValue() : LagTimer1.getValue()) : 1.0f);
                        break;
                }
        }
    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {
        PacketHelper.Values pc = Client.instance.packet;
        switch (Mode.getCurrentMode()) {
            case ("Grim"):
                switch (GMode.getCurrentMode()) {
                    case ("Glide"):
                        e = GrimFlightGlide.onEventPreMotion(e);
                        break;
                }

                break;
            case ("Vanilla"):
                switch (VMode.getCurrentMode()) {
                    case ("Motion"):
                        e = MotionFlight.onEventPreMotion(e);
                        break;
                }
                break;
            case ("Matrix"):
                boolean needJump = false;
                switch (MMode.getCurrentMode()) {
                    case ("DamageFloat"):
                        if (flydelay < 4) {
                            e.setOnGround(false);
                        }

                        break;
                    case ("Duel"):
                        e = MatrixRiksFlight.onEventPreMotion(e);

                        break;

                }
                break;
        }

    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPre e) {
        switch (Mode.getCurrentMode()) {
            case ("Grim"):
                switch (GMode.getCurrentMode()) {
                    case ("Glide"):
                        e = GrimFlightGlide.onEventReceivePacketPre(e);
                        break;
                }
                break;
            case ("Polar"):
                switch (PMode.getCurrentMode()) {
                    case ("MishaDuels"), ("Explosion"):
                        if (e.getPacket() instanceof ClientboundPlayerPositionPacket) {
                            ClientboundPlayerPositionPacket s08 = (ClientboundPlayerPositionPacket) e.getPacket();
                            PacketHelper.Values pc = Client.instance.packet;
                            index2 = 1;
                        }
                        break;
                }
                break;
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("Jump7.16.0"), ("OldJump"), ("DamageFloat"), ("Duel"), ("Vanilla"), ("Vanilla2"),
                            ("Timerless7.16.2"):
                        if ((e.getPacket() instanceof ClientboundExplodePacket)
                                || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket)
                                        && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player
                                                .getId())) {
                            e.cancel();
                        }
                        break;
                    case ("Lag"):
                        if (e.getPacket() instanceof ClientboundPlayerPositionPacket) {
                            if (!lagDoBoost) {
                                lagFlag = true;
                            } else if (lagBoost) {
                                lagFlag = true;
                            }
                            lagCanStrafe = true;
                            lagTicksSinceFlag = 0; // reset flag timer
                        }
                        break;
                }
                break;
        }
    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost e) {
        switch (Mode.getCurrentMode()) {

        }
    }

    @EventTarget
    public void onblink(EventSendPacketBlink e) {
        Packet<?> packetp = e.getPacket();

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("Timerless7.16.2"):
                        e = MatrixTimerlessFlight.onEventSendPacketBlink(e);
                        break;
                }
                break;
        }

    }

    @EventTarget
    public void oncancel(EventSendPacketCancel e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();
        switch (Mode.getCurrentMode()) {
            case ("Vanilla"):
                switch (VMode.getCurrentMode()) {
                    case ("AirWalk"):
                        if (SpoofGround.isEnabled() && packetp instanceof ServerboundMovePlayerPacket) {
                            ((base.mixin.client.accessors.ServerboundMovePlayerPacketAccessor) packetp)
                                    .setOnGround(OnGround.isEnabled());
                        }
                        break;
                }
                break;
            case ("Grim"):
                switch (GMode.getCurrentMode()) {
                    case ("Glide"):
                        e = GrimFlightGlide.onEventSendPacketCancel(e);
                        break;
                }

                break;
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {

                    case ("Vanilla"), ("Vanilla2"):

                        if (packetp instanceof ServerboundMovePlayerPacket) {
                            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;
                            if (c03.hasRotation()) {
                                flytype = 1;
                                fixedyaw = c03.getYRot(pc.LastYaw);
                                fixedpitch = c03.getXRot(pc.LastPitch);

                            } else {
                                flytype = 0;
                            }
                            e.setCancelled(true);
                        }
                        if (packetp instanceof ServerboundPlayerCommandPacket) {
                            e.setCancelled(true);
                        }
                        if (packetp instanceof ServerboundUseItemPacket) {
                            ServerboundUseItemPacket c082 = (ServerboundUseItemPacket) packetp;
                            e.setPacket(new ServerboundUseItemPacket(c082.getHand(), c082.getSequence(), pc.LastYaw,
                                    pc.LastPitch));
                        }
                        break;

                    case ("Lag"):
                        if (LagSilent.isEnabled() && packetp instanceof ServerboundMovePlayerPacket) {
                            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;
                            // strip rotation packets if NoHeadMove enabled
                            if (c03.hasRotation() && LagNoHeadMove.isEnabled()) {
                                e.setCancelled(true);
                                if (c03.hasPosition()) {
                                    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(
                                            c03.getX(pc.LastPosX), c03.getY(pc.LastPosY), c03.getZ(pc.LastPosZ),
                                            c03.isOnGround(), mc.player.horizontalCollision), 10, true);
                                }
                            }
                            // silent boost handling
                            if (lagDoBoost) {
                                if (!lagFlag) {
                                    e.setCancelled(true);
                                    double yawRad = Math.toRadians(MoveUtil.getdir());
                                    double x = mc.player.getX() + -Math.sin(yawRad) * LagBoostSpeed.getValue();
                                    double z = mc.player.getZ() + Math.cos(yawRad) * LagBoostSpeed.getValue();
                                    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(x, mc.player.getY() + 0.42, z,
                                            false, false), 10, true);
                                    lagBoost = true;
                                }
                                if (lagFlag && c03.hasPosition() && c03.hasRotation()) {
                                    MoveUtil.strafe(LagBoostSpeed.getValue());
                                    MoveUtil.setmotY(0.42);
                                    if (LagAutoToggle.isEnabled()) {
                                        this.state = false;
                                        this.onDisable();
                                    } else {
                                        lagDoBoost = false;
                                    }
                                }
                            }
                        }
                        break;

                }
                break;
        }

    }

    @EventTarget
    public void onTick(EventTick e) {
        // Process packet rate limiter
        PacketHelper.Values pc = Client.instance.packet;
        advanceTick();
        processPacketQueue(pc);

        if (groundstate == 1) {
            mc.player.setOnGround(false);
            groundstate = 0;
        } else if (groundstate == 2) {
            mc.player.setOnGround(true);
            groundstate = 0;
        }
    }

    public static void jump() {
        canjump = true;
        mc.player.jumpFromGround();
    }

    public static void polarjump() {
        canjump = true;
        MoveUtil.setmotY(1);
        MoveUtil.strafe(2.5);
    }

    public static void jump2() {
        canjump = true;
        MoveUtil.setmotY(0.69);
    }

}
