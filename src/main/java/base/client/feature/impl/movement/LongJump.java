package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.render.EventCameraPosUpdate;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.math.MathematicHelper;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;

public class LongJump extends Module {

    int index1 = 0;
    int index2 = 0;
    int index4 = 0;
    int index8 = 0;
    int index5 = 0;
    int groundstate = 0;
    Vec3 startpos;
    Vec3 startposcamera;
    double lastMotX = 0;
    double lastMotY = 0;
    double lastMotZ = 0;

    float prevYaw, prevPitch;

    ArrayDeque<Packet> LatePacketss = new ArrayDeque<Packet>();
    public static TimerHelper timerfromlastflaglong = new TimerHelper();
    int OldFlagskip = 0;

    boolean blinkpackets = false;

    public ModeSetting Mode;
    public NumberSetting boostMultiplier, FFLT, BHT, MatrixBowBoost, DashTicks, DashTimer, DashPackets, NewFlagMotion,
            NF2BoostSpeed;

    public static ModeSetting MMode;

    public static BooleanSetting FUGB;
    public static BooleanSetting FUG;
    public static BooleanSetting LF;
    public static BooleanSetting Ver188MM, SaveTpAngle, cameraspoof, onlyforward;

    public static BooleanSetting AutoDisable = new BooleanSetting("Server Join Auto Disable", true, () -> true);

    public static BooleanSetting MotionReset;

    public static NumberSetting BstTicks1, BstTicks2;

    // NewFlag2 settings
    public static ModeSetting NF2MotionMode, NF2BypassMethod;
    public static BooleanSetting NF2Test;

    // NewFlag2 state
    private double nf2X, nf2Y, nf2Z;
    private boolean nf2ReceivedFlag, nf2CanBoost, nf2Boosted, nf2TouchGround;

    public LongJump() {
        super("LongJump", "Позволяет прыгать на большую длинну", Type.Movement);
        Mode = new ModeSetting("Mode", "Matrix", () -> true, "Matrix");
        boostMultiplier = new NumberSetting("Boost Speed", 0.3F, 0.1F, 1F, 0.1F,
                () -> Mode.currentMode.equals("Matrix Pearle"));

        MMode = new ModeSetting("Matrix Mode", "NewFlag", () -> Mode.currentMode.equals("Matrix"), "OldFlag", "NewFlag",
                "NewFlag2", "OldHighJump", "Bow", "Dash", "NewBoost");

        NewFlagMotion = new NumberSetting("NewFlagMotion", 3, 1, 10, 0.1F,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("NewFlag"));
        SaveTpAngle = new BooleanSetting("SaveTpAngle", true,
                () -> MMode.isVisible() && (MMode.currentMode.equals("NewFlag")));
        cameraspoof = new BooleanSetting("CameraSpoof", true,
                () -> MMode.isVisible() && (MMode.currentMode.equals("NewFlag")));

        onlyforward = new BooleanSetting("Only Forward", true, () -> MMode.isVisible()
                && ((MMode.currentMode.equals("NewFlag") || (MMode.currentMode.equals("NewBoost")))));

        BstTicks1 = new NumberSetting("Boost Ticks 1", 10, 1, 20f, 1,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("NewBoost"));
        BstTicks2 = new NumberSetting("Boost Ticks 2", 4, 1, 20f, 1,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("NewBoost"));

        Ver188MM = new BooleanSetting("1.8.8", false,
                () -> MMode.isVisible() && (MMode.getCurrentMode().equals("OldHighJump")
                        || MMode.currentMode.equals("OldFlag") ||
                        MMode.currentMode.equals("NewFlag") || MMode.currentMode.equals("NewBoost")));

        FUGB = new BooleanSetting("Ground Boost", true,
                () -> (MMode.currentMode.equals("OldFlag") && Mode.currentMode.equals("Matrix")));
        FUG = new BooleanSetting("Glide", true,
                () -> (MMode.currentMode.equals("OldFlag") && Mode.currentMode.equals("Matrix")));
        FFLT = new NumberSetting("Low Timer", 0.5f, 0.01f, 4, 0.01f,
                () -> ((MMode.currentMode.equals("NewBoost")
                        || (MMode.currentMode.equals("OldFlag") || MMode.currentMode.equals("NewFlag"))
                                && Mode.currentMode.equals("Matrix"))));

        BHT = new NumberSetting("Boost Timer", 2f, 1f, 8, 0.01f,
                () -> (((MMode.currentMode.equals("NewBoost") || (MMode.currentMode.equals("NewFlag")))
                        && Mode.currentMode.equals("Matrix"))));

        LF = new BooleanSetting("Less Flag", false,
                () -> (MMode.currentMode.equals("OldFlag") && Mode.currentMode.equals("Matrix")));

        MotionReset = new BooleanSetting("Motion Reset", true, () -> Mode.equals("Matrix") && MMode.equals("Bow"));
        MatrixBowBoost = new NumberSetting("Matrix Bow Boost", 0.3F, 0.05F, 0.35F, 0.01F,
                () -> Mode.currentMode.equals("Matrix Pearle"));

        DashTimer = new NumberSetting("DashTimer", 0.333f, 0.01f, 1f, 0.01f,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Dash"));
        DashTicks = new NumberSetting("DashTicks", 10, 1, 150f, 1,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Dash"));

        DashPackets = new NumberSetting("DashPackets", 30, 1, 200, 1,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("Dash"));

        // NewFlag2 settings
        NF2MotionMode = new ModeSetting("NF2 Motion Mode", "Stable",
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("NewFlag2"), "Stable", "Last");
        NF2BypassMethod = new ModeSetting("NF2 Bypass Method", "Fall",
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("NewFlag2"), "Fall", "NoGround");
        NF2BoostSpeed = new NumberSetting("NF2 Boost Speed", 2.1F, -3.0F, 8.0F, 0.01F,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("NewFlag2"));
        NF2Test = new BooleanSetting("NF2 Test", false,
                () -> MMode.isVisible() && MMode.getCurrentMode().equals("NewFlag2"));

        addSettings(Mode, MMode,

                BstTicks1, BstTicks2, onlyforward,

                Ver188MM, SaveTpAngle, NewFlagMotion, BHT, cameraspoof,

                FUGB, FUG, FFLT, LF, MatrixBowBoost, boostMultiplier, MotionReset,

                DashTimer, DashTicks, DashPackets,

                NF2MotionMode, NF2BypassMethod, NF2BoostSpeed, NF2Test,

                AutoDisable);
    }

    @EventTarget
    public void onEMI(EventMoveInput e) {
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                if (onlyforward.isEnabled()) {
                    switch (MMode.getCurrentMode()) {
                        case ("NewFlag"):
                            if (index2 == 5) {
                                e.setForward(true);
                                e.setBackward(false);
                                e.setSneak(false);
                                e.setLeft(false);
                                e.setRight(false);
                                e.setSprint(true);
                                e.setJump(false);
                            }
                            break;
                        case ("NewBoost"):
                            if (index2 == 3) {
                                e.setForward(true);
                                e.setBackward(false);
                                e.setSneak(false);
                                e.setLeft(false);
                                e.setRight(false);
                                e.setSprint(true);
                                e.setJump(false);
                            }
                            break;
                    }
                }
                break;
        }

    }

    @EventTarget
    public void onTick(EventTick e) {
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("NewFlag"):

                        if (mc.player == null) {
                            this.state = false;
                            this.onDisable();
                        }
                        break;

                }
                break;
        }
        if (groundstate == 1) {
            mc.player.setOnGround(false);
            groundstate = 0;
        } else if (groundstate == 2) {
            mc.player.setOnGround(true);
            groundstate = 0;
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre event) {
        PacketHelper.Values pc = Client.instance.packet;
        if (Mode.getCurrentMode().equals("Matrix")) {
            switch (MMode.getCurrentMode()) {

                case ("OldFlag"):
                    boolean iss121 = ((event.getPacket() instanceof ClientboundExplodePacket)
                            || ((event.getPacket() instanceof ClientboundSetEntityMotionPacket)
                                    && ((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player
                                            .getId()));
                    if (iss121) {
                        event.setCancelled(true);
                    }
                    if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                        ClientboundPlayerPositionPacket s08 = (ClientboundPlayerPositionPacket) event.getPacket();
                        if (mc.player == null) {
                            this.state = false;
                            this.onDisable();
                            return;
                        }

                        if (OldFlagskip == 1) {

                            event.cancel();
                            OldFlagskip = 2;
                            mc.player.setPos(s08.change().position().x, s08.change().position().y,
                                    s08.change().position().z);
                            timerfromlastflaglong.reset();
                            pc.sendPacket(new ServerboundAcceptTeleportationPacket(s08.id()));
                            if (Ver188MM.isEnabled()) {
                                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(s08.change().position().x,
                                        s08.change().position().y, s08.change().position().z, s08.change().yRot(),
                                        s08.change().xRot(), false, false), 10, true);
                            }
                            if (!LF.isEnabled() && FUGB.isEnabled()) {
                                MoveUtil.smartstrafe(2.02);
                                mc.player.setOnGround(false);
                                MoveUtil.setmotY(0.42349);
                            } else if (!LF.isEnabled() && !FUGB.isEnabled()) {
                                MoveUtil.smartstrafe(1.98);
                                MoveUtil.setmotY(0.42349);
                            } else if (LF.isEnabled() && !FUGB.isEnabled()) {
                                MoveUtil.ssmartstrafe(1.14);
                                mc.player.setOnGround(false);
                                MoveUtil.setmotY(0.42349);
                            } else if (LF.isEnabled() && FUGB.isEnabled()) {
                                MoveUtil.ssmartstrafe(1.2);
                                MoveUtil.setmotY(0.42349);
                            }
                            if (Ver188MM.isEnabled()) {
                                MoveUtil.mult2ds(0.98);
                            }

                            index1 = 5;
                        }
                    }
                    break;

                case ("NewBoost"):

                    boolean iss127 = ((event.getPacket() instanceof ClientboundExplodePacket)
                            || ((event.getPacket() instanceof ClientboundSetEntityMotionPacket)
                                    && ((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player
                                            .getId()));
                    if (iss127) {
                        event.setCancelled(true);
                    }

                    if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                        ClientboundPlayerPositionPacket s08 = (ClientboundPlayerPositionPacket) event.getPacket();
                        if (mc.player == null || index5 > 1) {
                            this.state = false;
                            this.onDisable();
                            return;
                        }

                        event.cancel();
                        index2 = 0;
                        timerfromlastflaglong.reset();
                        pc.sendPacket(new ServerboundAcceptTeleportationPacket(s08.id()));
                        if (Ver188MM.isEnabled()) {
                            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(s08.change().position().x,
                                    s08.change().position().y, s08.change().position().z, s08.change().yRot(),
                                    s08.change().xRot(), false, false), 10, true);
                        }
                        MoveUtil.setmotY(0.42);

                        double speed = MathematicHelper.getVecDist(s08.change().position(),
                                new Vec3(startpos.x, s08.change().position().y, startpos.z)) * 0.95D;
                        mc.player.setPos(s08.change().position().x, s08.change().position().y,
                                s08.change().position().z);

                        if (speed > 7) {
                            speed *= 0.95;
                        }

                        speed = Math.min(speed, 9.8);
                        MoveUtil.strafe(Ver188MM.isEnabled() ? speed * 0.97 : speed);
                        if (!onlyforward.isEnabled()) {
                            MoveUtil.smartstrafe();
                        }
                        index1 = 0;
                        TimerUtil.setTimerspeed(FFLT.getValue());
                        index5++;
                        if (index5 > 1 || BstTicks2.getValue() <= 0) {
                            index2 = 3;
                        }

                    }

                    break;

                case ("NewFlag"):

                    boolean iss122 = ((event.getPacket() instanceof ClientboundExplodePacket)
                            || ((event.getPacket() instanceof ClientboundSetEntityMotionPacket)
                                    && ((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player
                                            .getId()));
                    if (iss122) {
                        event.setCancelled(true);
                    }
                    if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                        ClientboundPlayerPositionPacket s08 = (ClientboundPlayerPositionPacket) event.getPacket();
                        if (mc.player == null) {
                            this.state = false;
                            this.onDisable();
                            return;
                        }

                        if (index2 == 4) {

                            event.cancel();
                            index2 = 5;

                            mc.player.setPos(s08.change().position().x, s08.change().position().y,
                                    s08.change().position().z);
                            timerfromlastflaglong.reset();
                            pc.sendPacket(new ServerboundAcceptTeleportationPacket(s08.id()));
                            if (Ver188MM.isEnabled()) {
                                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(s08.change().position().x,
                                        s08.change().position().y, s08.change().position().z, s08.change().yRot(),
                                        s08.change().xRot(), false, false), 10, true);
                            }
                            MoveUtil.setmotY(0.42);
                            if (SaveTpAngle.isEnabled()) {
                                mc.player.setYRot(prevYaw);
                                mc.player.setXRot(prevPitch);
                            }

                            double speed = NewFlagMotion.getValue();

                            MoveUtil.strafe(Ver188MM.isEnabled() ? speed * 0.97 : speed);
                            if (!onlyforward.isEnabled()) {
                                MoveUtil.smartstrafe();
                            }
                            index1 = 0;
                            TimerUtil.setTimerspeed(FFLT.getValue());
                        } else if (index2 < 4) {
                            LatePacketss.clear();
                            this.state = false;
                            this.onDisable();

                        }
                    }

                    break;

                case ("NewFlag2"):
                    // NewFlag2: flag-based longjump ported from loftily
                    if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                        nf2ReceivedFlag = true;
                        if (NF2MotionMode.getCurrentMode().equals("Last")) {
                            nf2CanBoost = false;
                            nf2X = mc.player.getDeltaMovement().x;
                            nf2Y = mc.player.getDeltaMovement().y;
                            nf2Z = mc.player.getDeltaMovement().z;
                        }
                    }
                    break;

                case ("Bow"):
                    boolean iss12 = ((event.getPacket() instanceof ClientboundExplodePacket)
                            || ((event.getPacket() instanceof ClientboundSetEntityMotionPacket)
                                    && ((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player
                                            .getId()));
                    if (iss12) {
                        event.cancel();
                    }
                    break;
                case ("Dash"):
                    boolean iss123 = ((event.getPacket() instanceof ClientboundExplodePacket)
                            || ((event.getPacket() instanceof ClientboundSetEntityMotionPacket)
                                    && ((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player
                                            .getId()));
                    if (iss123) {
                        event.cancel();
                    }
                    break;
            }

        }

    }

    @EventTarget
    public void onPreUpdate(EventPreMotion event) {
        String longMode = Mode.getOptions();
        this.setSuffix(longMode);

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("OldFlag"):
                        if (index1 == 2 && FUGB.isEnabled()) {
                            event.setOnGround(true);
                        }
                        if (index1 == 3 && !FUGB.isEnabled() && !LF.isEnabled()) {
                            event.setOnGround(false);
                        }
                        if (index1 == 4 && !FUGB.isEnabled() && OldFlagskip == 2) {
                            event.setOnGround(true);
                        }
                        break;
                    case ("NewFlag"):

                        if (index2 >= 1 && index2 <= 5) {
                            if (index1 >= 0) {
                                event.setOnGround(false);
                                groundstate = 1;
                            }

                        }

                        break;
                    case ("NewBoost"):

                        if (index1 >= 0) {
                            event.setOnGround(false);
                            groundstate = 1;
                        }

                        break;
                    case ("NewFlag2"):
                        // NoGround mode: spoof ground state until boost
                        if (NF2BypassMethod.getCurrentMode().equals("NoGround") && !nf2CanBoost) {
                            event.setOnGround(false);
                        }
                        break;
                    case ("Bow"):
                        event.setPitch(-89.9F);
                        break;
                }
                break;
        }

    }

    @EventTarget
    public void onPrePacket(EventSendPacketCancel e) {
        Packet<?> packetp = e.getPacket();
        PacketHelper.Values pc = Client.instance.packet;

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {

                    case ("NewBoost"):
                        if (!e.isCancelled() && index2 == 1 && index1 >= 2) {
                            if (packetp instanceof ServerboundMovePlayerPacket) {
                                e.cancel();
                            }
                        }
                        break;

                    case ("OldFlag"):
                        if (!e.isCancelled() && OldFlagskip == 1 && index8 >= 2) {
                            e.cancel();
                        }
                        break;
                    case ("NewFlag"):
                        if (!(packetp instanceof ServerboundKeepAlivePacket) && !e.isCancelled()
                                && ((index2 == 4 && index8 <= 2) || index2 == 3) && e.getPermissionidstate() < 5) {
                            e.cancel();
                        }
                        break;
                    case ("NewFlag2"):
                        // restore motion on PosRot if Last mode
                        if (packetp instanceof ServerboundMovePlayerPacket.PosRot
                                && NF2MotionMode.getCurrentMode().equals("Last") && nf2ReceivedFlag) {
                            MoveUtil.setmotXYZ(nf2X, nf2Y, nf2Z);
                            autoDisable();
                            nf2CanBoost = false;
                            nf2ReceivedFlag = false;
                        }
                        break;
                    case ("Bow"):
                        if (index4 == 0) {
                            if (packetp instanceof ServerboundMovePlayerPacket.PosRot) {
                                ServerboundMovePlayerPacket.PosRot c06 = (ServerboundMovePlayerPacket.PosRot) packetp;
                                e.setPacket(new ServerboundMovePlayerPacket.PosRot(c06.getX(pc.LastPosX),
                                        c06.getY(pc.LastPosY), c06.getZ(pc.LastPosZ),
                                        c06.getYRot(pc.LastYaw), (float) (-89.9 + Math.random()), c06.isOnGround(),
                                        c06.horizontalCollision()));
                            }
                            if (packetp instanceof ServerboundMovePlayerPacket.Rot) {
                                ServerboundMovePlayerPacket.Rot c05 = (ServerboundMovePlayerPacket.Rot) packetp;
                                e.setPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY,
                                        pc.LastPosZ, c05.getYRot(pc.LastYaw), (float) (-89.9 + Math.random()),
                                        c05.isOnGround(), c05.horizontalCollision()));

                            }
                            if (packetp instanceof ServerboundUseItemPacket) {
                                ServerboundUseItemPacket c082 = (ServerboundUseItemPacket) packetp;
                                e.setPacket(new ServerboundUseItemPacket(c082.getHand(), c082.getSequence(),
                                        c082.getYRot(), (float) (-89.9 + Math.random())));
                            }
                        }
                        break;
                    case ("Dash"):
                        if (packetp instanceof ServerboundMovePlayerPacket && e.getPermissionidstate() < 5) {
                            e.cancel();

                        }
                        if (packetp instanceof ServerboundUseItemPacket) {
                            ServerboundUseItemPacket c082 = (ServerboundUseItemPacket) packetp;
                            e.setPacket(new ServerboundUseItemPacket(c082.getHand(), c082.getSequence(), pc.LastYaw,
                                    pc.LastPitch));
                        }
                        break;

                }
                break;
        }

    }

    @EventTarget
    public void onPrePacket(EventSendPacketBlink e) {
        Packet<?> packetp = e.getPacket();
        PacketHelper.Values pc = Client.instance.packet;
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("NewFlag"):

                        if (!(packetp instanceof ServerboundKeepAlivePacket) && index2 < 4
                                && e.getPermissionidstate() < 5 && !e.isCanceled() && this.state
                                && LatePacketss.size() < 200) {

                            e.cancel();

                            LatePacketss.add(packetp);
                        }

                        break;
                }
        }
    }

    @EventTarget
    public void onMove(EventOnMovePost event) {
        PacketHelper.Values pc = Client.instance.packet;
        double yawt = Math.toRadians(Minecraft.getInstance().player.getYRot());
        if (MoveUtil.getdir() != -1) {
            yawt = Math.toRadians(MoveUtil.getdir());
        }
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        double mult = 0.06249;
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("Dash"):
                        TimerUtil.setTimerspeed(DashTimer.getValue());
                        for (int i = 0; i < DashPackets.getValue(); i++) {
                            EntityUtil.dtp(MoveUtil.getmovedir3(mc.player.getYRot(), mc.player.getXRot(), mult));
                            Disabler.savedabusepacket--;
                            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
                            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(),
                                    mc.player.getZ(), false, false), 10, true);

                        }
                        pc.LastPosX = mc.player.getX();
                        pc.LastPosY = mc.player.getY();
                        pc.LastPosZ = mc.player.getZ();
                        index1++;
                        MoveUtil.stop3();
                        if (index1 >= DashTicks.getValue()) {
                            toggle();
                        }
                        break;
                    case ("NewFlag"):
                        if (mc.player.verticalCollision && index2 == 0)
                            index2 = 1;
                        if (LatePacketss.size() > 180) {
                            mc.player.setPos(startpos);
                            LatePacketss.clear();
                            this.state = false;
                            this.onDisable();
                            return;
                        }
                        if (ACUtil.isground()) {
                            index5++;
                        } else {
                            index5 = 0;
                        }
                        boolean ongr = (index5 > 1);

                        if (index2 == 1) {

                            if (index1 == 0) {
                                startpos = mc.player.position();
                                startposcamera = mc.gameRenderer.getMainCamera().position();
                                mc.player.jumpFromGround();

                                if (SaveTpAngle.isEnabled()) {
                                    prevPitch = mc.player.getXRot();
                                    prevYaw = mc.player.getYRot();
                                }

                            } else {
                                MoveUtil.minsmartstrafe(0.2);
                            }

                            TimerUtil.setTimerspeed(BHT.getValue());

                            MoveUtil.addmotY(0.00345);
                            double diff = MathematicHelper.getVecDist(new Vec3(startpos.x, 0, startpos.z),
                                    new Vec3(mc.player.getX() - mc.player.getDeltaMovement().x, 0,
                                            mc.player.getZ() - mc.player.getDeltaMovement().z));
                            diff *= 0.95;

                            if (diff > NewFlagMotion.getValue() && (NewFlagMotion.getValue() < 3.1 || ongr)) {
                                MoveUtil.stop3();

                                index2 = 3;
                                pushPackets();
                                TimerUtil.reset();
                                if (LatePacketss.size() == 0) {
                                    index2 = 4;
                                    if (ongr) {
                                        pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true,
                                                mc.player.horizontalCollision), 10, true);
                                    }
                                    index4 = (ongr ? 1 : 2);
                                    double mts = pc.lastVeltimer.hasTimeElapsed(2000, false) ? 1 : 2.5;
                                    pc.sendPacket(
                                            new ServerboundMovePlayerPacket.Pos(mc.player.getX() + xt * mts,
                                                    mc.player.getY() + 0.42, mc.player.getZ() + zt * mts, false, false),
                                            10, true);
                                    TimerUtil.setTimerspeed(FFLT.getValue());
                                }

                            }
                            index1++;
                        } else if (index2 == 3) {
                            MoveUtil.stop3();
                            TimerUtil.reset();
                            pushPackets();
                            if (LatePacketss.size() == 0 && (NewFlagMotion.getValue() < 3.1 || ongr)) {
                                index2 = 4;
                                if (ongr) {

                                    pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true,
                                            mc.player.horizontalCollision), 10, true);
                                }
                                index4 = (ongr ? 1 : 2);
                                double mts = pc.lastVeltimer.hasTimeElapsed(2000, false) ? 1 : 2.5;
                                pc.sendPacket(
                                        new ServerboundMovePlayerPacket.Pos(mc.player.getX() + xt * mts,
                                                mc.player.getY() + 0.42, mc.player.getZ() + zt * mts, false, false),
                                        10, true);
                                TimerUtil.setTimerspeed(FFLT.getValue());
                            }

                        }

                        else if (index2 == 4) {
                            index8++;
                        } else if (index2 == 5) {
                            TimerUtil.setTimerspeed(FFLT.getValue());
                            if (index1 >= 40 || mc.player.verticalCollision) {
                                this.state = false;
                                this.onDisable();
                            }
                            if (onlyforward.isEnabled()) {
                                MoveUtil.strafe();
                            }
                            MoveUtil.addmotY(pc.lastVeltimer.hasTimeElapsed(2000, false) ? 0.00345 : 0);
                            index1++;
                        }

                        break;
                    case ("NewBoost"):
                        if (index2 == 0) {
                            if (index1 == 0 && mc.player.onGround() && ACUtil.isground()) {
                                mc.player.jumpFromGround();
                            }
                            TimerUtil.setTimerspeed(BHT.getValue());
                            double val = index5 > 0 ? BstTicks2.getValue() : BstTicks1.getValue();
                            if (index1 >= val) {
                                startpos = mc.player.position();
                                mc.player.jumpFromGround();
                                MoveUtil.strafe(5);
                                index2 = 1;
                                index1 = 0;
                            }
                        } else if (index2 == 1) {
                            MoveUtil.stop3();
                        } else if (index2 == 3) {
                            TimerUtil.setTimerspeed(FFLT.getValue());
                            if (index1 > 40 || mc.player.verticalCollision) {
                                this.state = false;
                                this.onDisable();
                                return;
                            }
                            if (onlyforward.isEnabled()) {
                                MoveUtil.strafe();
                            }
                            MoveUtil.addmotY(pc.lastVeltimer.hasTimeElapsed(2000, false) ? 0.00345 : 0);
                        }

                        index1++;
                        break;

                    case ("OldFlag"):
                        int tickstostart = 3;
                        if (index1 < tickstostart) {
                            if (FUGB.isEnabled() && index4 == 0) {
                                if (!ACUtil.ismatrixonground()) {
                                    index1 = tickstostart;
                                } else {
                                    if (index1 == 0) {
                                        MoveUtil.setmotY(0.42349);
                                    }
                                    if (LF.isEnabled() && index1 > 1) {
                                        index4 = 1;
                                        index1 = tickstostart;
                                    }
                                    if (index4 == 0) {
                                        if (index1 == 1) {
                                            MoveUtil.addmotY(0.00349);
                                            MoveUtil.strafe(0.04);
                                        }
                                        if (index1 == 2) {
                                            MoveUtil.setmotY(0.42349);
                                            mc.player.setOnGround(true);
                                            MoveUtil.strafe(0.02);
                                        }
                                    }
                                }
                            } else {
                                if (index1 <= 0) {
                                    index1 = tickstostart - 1;
                                }
                            }
                        }
                        if (index1 == tickstostart) {
                            MoveUtil.ssmartstrafe(1.999);
                            MoveUtil.setmotY(0.42);
                            OldFlagskip = 1;
                            pc.LastTpNum++;
                            if (!LF.isEnabled()) {
                                mc.player.setOnGround(false);
                            }
                            TimerUtil.setTimerspeed((float) FFLT.getValue());
                        }
                        if (index1 > tickstostart && OldFlagskip == 1) {
                            MoveUtil.stop3();
                        }
                        if (index1 == 15 && FUG.isEnabled()) {
                            MoveUtil.setmotY(0);
                        }
                        if (index1 >= (16 + tickstostart)) {
                            this.state = false;
                            this.onDisable();
                        }
                        if (OldFlagskip == 2 && !timerfromlastflaglong.hasTimeElapsed(1300, false)) {
                            MoveUtil.smartstrafe();
                        }
                        if (index1 != 4) {
                            index1++;
                        }
                        if (OldFlagskip == 1) {
                            index8++;
                        }
                        break;
                    case ("OldHighJump"):
                        if (index1 == 0) {
                            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX + xt * 1.999, pc.LastPosY,
                                    pc.LastPosZ + zt * 1.999, false, mc.player.horizontalCollision), 10);
                            Disabler.savedabusepacket--;
                            pc.LastTpNum++;
                            pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));
                            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                    pc.LastPosY, pc.LastPosZ, pc.LastTpNum, false, false));
                            mc.player.setPos(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ);
                            if (Ver188MM.isEnabled()) {
                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastGPosX, pc.LastGPosY,
                                        pc.LastGPosZ, false, mc.player.horizontalCollision), 10, true);
                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX,
                                        pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, true, false));
                            }
                            MoveUtil.setmotY(0.9995);
                            MoveUtil.smartstrafe(1.999);
                            TimerUtil.setTimerspeed(0.5f);
                            mc.player.setOnGround(false);
                            LongJump.timerfromlastflaglong.reset();
                            index2 = 1;
                        }
                        if (index1 == 5 && TimerUtil.getTimerspeed() == 0.5f) {
                            TimerUtil.reset();
                        }
                        if (mc.player.verticalCollision && index1 > 3) {
                            this.state = false;
                            this.onDisable();
                        }
                        index1++;
                        break;
                    case ("Bow"):

                        if (mc.player.onGround() && mc.player.verticalCollision) {
                            mc.player.jumpFromGround();
                        }

                        if (pc.lastVeltimer.hasTimeElapsed(110, false)
                                && !pc.lastVeltimer.hasTimeElapsed(1260, false)) {

                            MoveUtil.setmotY(
                                    mc.player.getDeltaMovement().y() < 0 ? -0.01 : mc.player.getDeltaMovement().y());

                            // MoveUtil.minstrafe(0.249);
                            if (index4 == 0) {
                                index2 = 0;
                                index4 = 1;
                                MoveUtil.setmotXZ(lastMotX * 0.9, lastMotZ * 0.9);
                            }
                            if (index2 > 1) {
                                MoveUtil.addmotX(xt * MatrixBowBoost.getValue());
                                MoveUtil.addmotZ(zt * MatrixBowBoost.getValue());
                            }

                            MoveUtil.smartstrafe();

                            lastMotX = mc.player.getDeltaMovement().x();
                            lastMotZ = mc.player.getDeltaMovement().z();
                            index2++;
                        } else if (mc.player.getDeltaMovement().y() < 0) {
                            index4 = 0;
                            MoveUtil.stop3();
                        }

                        break;

                    case ("NewFlag2"):
                        // NewFlag2: flag-based longjump - main update logic
                        // handle touchGround state
                        if (!mc.player.onGround() && nf2TouchGround) {
                            nf2TouchGround = false;
                        }
                        if (mc.player.onGround() && !nf2TouchGround) {
                            mc.player.jumpFromGround();
                            nf2Boosted = false;
                            if (NF2BypassMethod.getCurrentMode().equals("NoGround") && !nf2Boosted) {
                                nf2CanBoost = true;
                            }
                        }
                        // Fall mode: trigger boost on fall distance
                        if (mc.player.fallDistance >= 0.25f && !nf2Boosted
                                && NF2BypassMethod.getCurrentMode().equals("Fall")) {
                            nf2CanBoost = true;
                        }
                        // apply boost
                        if (nf2CanBoost) {
                            MoveUtil.strafe(NF2BoostSpeed.getValue());
                            MoveUtil.setmotY(0.42);
                            nf2Boosted = true;
                        }
                        // auto disable on flag after boost
                        if (nf2ReceivedFlag && nf2Boosted) {
                            autoDisable();
                            nf2CanBoost = false;
                            nf2ReceivedFlag = false;
                        }
                        break;

                }
                break;

        }

    }

    @EventTarget
    public void camerada(EventCameraPosUpdate e) {
        PacketHelper.Values pc = Client.instance.packet;
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("NewFlag"):
                        if (index2 != 5 && cameraspoof.isEnabled()) {
                            e.setPosition(new Vec3(startposcamera.x, startposcamera.y, startposcamera.z));
                        }

                        break;

                }
                break;
        }

    }

    public void pushPackets() {
        PacketHelper.Values pc = Client.instance.packet;

        int j = 0, sizee = LatePacketss.size() > 45 ? 15 : 10;

        for (Packet pp : LatePacketss) {
            if (j >= sizee) {
                break;
            }
            pc.sendPacket(pp, 10, true);
            j++;
        }
        for (int i = 0; i < sizee; i++) {
            if (LatePacketss.size() <= 0) {
                break;
            }
            LatePacketss.removeFirst();
        }

    }

    // helper to toggle off module
    public void autoDisable() {
        this.state = false;
        this.onDisable();
    }

    @Override
    public void onEnable() {
        blinkpackets = false;
        groundstate = 0;
        LatePacketss.clear();
        index4 = 0;
        index8 = 0;
        index1 = 0;
        index2 = 0;
        index5 = 0;
        OldFlagskip = 0;
        if (mc.player != null) {
            startpos = mc.player.position();

            lastMotX = mc.player.getDeltaMovement().x;
            lastMotY = mc.player.getDeltaMovement().y;
            lastMotZ = mc.player.getDeltaMovement().z;
        }
        if (mc.gameRenderer.getMainCamera() != null) {
            startposcamera = mc.gameRenderer.getMainCamera().position();
        }

        TimerUtil.reset();

        // NewFlag2 state reset
        nf2Boosted = false;
        nf2CanBoost = false;
        nf2ReceivedFlag = false;
        nf2TouchGround = false;
        // NoGround mode: auto-jump on enable
        if (MMode.getCurrentMode().equals("NewFlag2") && NF2BypassMethod.getCurrentMode().equals("NoGround")) {
            if (mc.player != null && mc.player.onGround()) {
                mc.player.jumpFromGround();
            }
            nf2TouchGround = true;
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        TimerUtil.reset();

        if (Mode.getCurrentMode().equals("Matrix")) {
            if (MMode.getCurrentMode().equals("Bow")) {
                if (MotionReset.isEnabled()) {
                    MoveUtil.stop3();
                }
            } else if (MMode.getCurrentMode().equals("Dash")) {
                MoveUtil.setmotXYZ(lastMotX, lastMotY, lastMotZ);
                /// MoveUtil.limit2speed(0.199); MoveUtil.setmotY(-0.078);
            } else if (MMode.getCurrentMode().equals("NewFlag")) {
                LatePacketss.clear();
            }

        }

        pushPackets();
        super.onDisable();
    }

}
