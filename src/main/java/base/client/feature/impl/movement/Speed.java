package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.TeleportBack;
import base.client.feature.impl.movement.speeds.grim.GrimExploitSpeed;
import base.client.feature.impl.movement.speeds.grim.GrimGroundSpeed;
import base.client.feature.impl.movement.speeds.grim.GrimLowHopSpeed;
import base.client.feature.impl.movement.speeds.matrix.*;
import base.client.feature.impl.movement.speeds.other.OtherGroundSpeed;
import base.client.feature.impl.movement.speeds.other.OtherSimpleHopSpeed;
import base.client.feature.impl.movement.speeds.polar.PolarBasicSpeed;
import base.client.feature.impl.movement.speeds.polar.PolarGroundStrafeSpeed;
import base.client.feature.impl.movement.speeds.polar.PolarLowHopSpeed;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.LatePacket;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerHelper;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.level.block.Block;

import java.util.ArrayDeque;

public class Speed extends Module {
    public TimerHelper flyTimer = new TimerHelper();

    public static ArrayDeque<Packet> BlinkPackets = new ArrayDeque<>();
    ArrayDeque<LatePacket> SavedFlagPackets = new ArrayDeque<LatePacket>();
    boolean shouddelay = false;
    ArrayDeque<Packet> ServerPackets = new ArrayDeque<Packet>();

    public static ModeSetting Mode, PolarMode, GrimMode, MatrixMode, OMode, GrMode, HopHMode, HopVMode;
    public static NumberSetting THP, GrimMotY;

    public static NumberSetting mfhspeed = new NumberSetting("Matrix OldFastHop Speed", 1.32F, 0.5F, 2.047F, 0.001F,
            () -> Mode.getCurrentMode().equals("Matrix") && MatrixMode.getCurrentMode().equals("OldFastHop"));
    public static BooleanSetting NGSpeedBypass = new BooleanSetting("OldFastHop speed bypass", false,
            () -> mfhspeed.isVisible());
    public static BooleanSetting Ver188MM, GrimGroundNoAir, VerGroundModify, HorGroundModify;
    public static NumberSetting LowTimer = new NumberSetting("LowTimer", 0.5F, 0.01F, 1.5F, 0.01F,
            () -> Mode.getCurrentMode().equals("Matrix") && Mode.getCurrentMode().equals("OldFastHop"));

    public static NumberSetting GroundStrafe, HopStrafe, GroundBst, HopBst, GroundMult, HopMult, HopVBst, HopVMult;

    public static BooleanSetting AutoDisable = new BooleanSetting("Server Join Auto Disable", true, () -> true);

    public static int groundstate = 0;// 0=nothing 1=noground 2=ground
    public static int index1 = 0, index2 = 0, index3 = 0, index4 = 0, index5 = 0, index6 = 0, index7 = 0;

    public static boolean jumping;

    double indexd6 = 0;
    double indexd7 = 0;

    int flydelay = 0;
    int flytype = 0;

    double lasttpposX = 0;
    double lasttpposY = 0;
    double lasttpposZ = 0;

    int boostTick, flagskip, stage;

    double startposY = 0;

    public Speed() {
        super("Speed", "Позволяет более эффективно двигаться", Type.Movement);
        Mode = new ModeSetting("Mode", "Polar", () -> true, "Polar", "Grim", "Matrix", "Other");
        PolarMode = new ModeSetting("Polar Mode", "Basic", () -> Mode.getCurrentMode().equals("Polar"), "Basic",
                "LowHop", "GroundStrafe");
        GrimMode = new ModeSetting("Grim Mode", "LowHop", () -> Mode.getCurrentMode().equals("Grim"), "LowHop",
                "Ground", "FunnyPackets");
        MatrixMode = new ModeSetting("Matrix Mode", "BasicHop", () -> Mode.getCurrentMode().equals("Matrix"),
                "OldFastHop", "Basic", "SilentFlag", "BasicHop", "HighHop", "LowHop", "LowHop2", "YPort", "Ground",
                "BoostHop", "TravelHop");

        OMode = new ModeSetting("Other Mode", "Ground", () -> Mode.getCurrentMode().equals("Other"), "Ground", "Hop");
        GrMode = new ModeSetting("Ground Mode", "Strafe",
                () -> OMode.isVisible() && OMode.getCurrentMode().equals("Ground"), "Strafe", "MotionBoost",
                "MotionMult");
        HopHMode = new ModeSetting("Hop Hor Mode", "Strafe",
                () -> OMode.isVisible() && OMode.getCurrentMode().equals("Hop"), "Strafe", "MotionBoost", "MotionMult");
        HopVMode = new ModeSetting("Hop Ver Mode", "Boost",
                () -> OMode.isVisible() && OMode.getCurrentMode().equals("Hop"), "Boost", "Mult");

        GroundStrafe = new NumberSetting("Ground Strafe", 0.5F, 0.01F, 5F, 0.01F,
                () -> GrMode.isVisible() && GrMode.getCurrentMode().equals("Strafe"));
        HopStrafe = new NumberSetting("Hop Strafe", 0.5F, 0.01F, 5F, 0.01F,
                () -> HopHMode.isVisible() && HopHMode.getCurrentMode().equals("Strafe"));

        GroundBst = new NumberSetting("Ground Boost", 0.1F, 0.01F, 0.5F, 0.01F,
                () -> GrMode.isVisible() && GrMode.getCurrentMode().equals("MotionBoost"));
        HopBst = new NumberSetting("Hop Boost", 0.1F, 0.01F, 0.5F, 0.01F,
                () -> HopHMode.isVisible() && HopHMode.getCurrentMode().equals("MotionBoost"));
        GroundMult = new NumberSetting("Ground Mult", 1.1F, 1.01F, 4F, 0.01F,
                () -> GrMode.isVisible() && GrMode.getCurrentMode().equals("MotionMult"));
        HopMult = new NumberSetting("Hop Mult", 1.1F, 1.01F, 4F, 0.01F,
                () -> HopHMode.isVisible() && HopHMode.getCurrentMode().equals("MotionMult"));

        HopVBst = new NumberSetting("Hop Ver Boost", 0F, 0.0001F, 0.5F, 0.001F,
                () -> HopHMode.isVisible() && HopHMode.getCurrentMode().equals("MotionBoost"));
        HopVMult = new NumberSetting("Hop Ver Mult", 0.5F, 0.01F, 4F, 0.01F,
                () -> HopHMode.isVisible() && HopHMode.getCurrentMode().equals("MotionMult"));

        VerGroundModify = new BooleanSetting("Ver Ground Modify", false, () -> HopVMode.isVisible());
        HorGroundModify = new BooleanSetting("Hor Ground Modify", false, () -> HopHMode.isVisible());

        Ver188MM = new BooleanSetting("1.8.8", false,
                () -> mfhspeed.isVisible() || MatrixMode.getCurrentMode().equals("LowHop2"));
        GrimGroundNoAir = new BooleanSetting("GrimGroundNoAir", true,
                () -> GrimMode.isVisible() && GrimMode.getCurrentMode().equals("Ground"));

        THP = new NumberSetting("TravelHop Packets", 8, 1, 30, 1,
                () -> (MatrixMode.getCurrentMode().equals("TravelHop") && Mode.getCurrentMode().equals("Matrix")));
        GrimMotY = new NumberSetting("Grim MotY", 0, -0.03F, 0.03F, 0.001F,
                () -> (GrimMode.getCurrentMode().equals("FunnyPackets") && Mode.getCurrentMode().equals("Grim")));

        addSettings(Mode, PolarMode,

                /// //
                OMode, GrMode, HopHMode, HopStrafe, HopBst, HopMult, HopVMode, HopVBst, HopVMult,

                /// //

                ///////
                GrimMode, GrimMotY, GrimGroundNoAir,
                /// ///

                /////
                MatrixMode,
                THP, mfhspeed,
                NGSpeedBypass,
                /////

                VerGroundModify, HorGroundModify,

                Ver188MM,

                LowTimer, AutoDisable);
    }

    public void onEnable() {
        jumping = true;
        boostTick = 0;
        groundstate = 0;
        flagskip = 0;
        stage = 0;
        shouddelay = false;
        ServerPackets.clear();
        SavedFlagPackets.clear();
        BlinkPackets.clear();
        flyTimer.reset();
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
        lasttpposX = 0;
        lasttpposY = 0;
        lasttpposZ = 0;
        startposY = mc.player.getY();
        switch (Mode.getCurrentMode()) {
            case ("Grim"):
                switch (GrimMode.getCurrentMode()) {
                    case ("LowHop"):
                        GrimLowHopSpeed.onenable();
                        break;
                    case ("FunnyPackets"):
                        GrimExploitSpeed.onenable();
                        break;

                }
                break;
            case ("Polar"):
                switch (GrimMode.getCurrentMode()) {
                    case ("LowHop"):
                        PolarLowHopSpeed.onenable();
                        break;
                    case ("Basic"):
                        PolarBasicSpeed.onenable();
                        break;
                    case ("GroundStrafe"):
                        PolarGroundStrafeSpeed.onenable();
                        break;
                }
                break;

            case ("Matrix"):
                switch (MatrixMode.getCurrentMode()) {
                    case ("BasicHop"):
                        MatrixBasicHopSpeed.onenable();
                        break;
                    case ("YPort"):
                        MatrixYPortSpeed.onenable();
                        break;
                    case ("BoostHop"):
                        MatrixBoostHopSpeed.onenable();
                        break;
                    case ("Ground"):
                        MatrixGroundSpeed.onenable();
                        break;
                    case ("Basic"):
                        MatrixBasicSpeed.onenable();
                        break;
                    case ("OldFastHop"):
                        MatrixOldFastHopSpeed.onenable();
                        break;
                    case ("HighHop"):
                        MatrixHighHopSpeed.onenable();
                        break;
                    case ("LowHop"):
                        MatrixLowHopSpeed.onenable();
                        break;
                    case ("LowHop2"):
                        MatrixLowHop2Speed.onenable();
                        break;
                    case ("TravelHop"):
                        MatrixTravelHopSpeed.onenable();
                        break;
                    case ("SilentFlag"):
                        MatrixSilentFlagSpeed.onenable();
                        break;
                }
                break;

        }

        super.onEnable();
    }

    public void onDisable() {
        if (mc.player != null)
            TimerUtil.setTimerspeed(1);
        PacketHelper.Values pc = Client.instance.packet;
        while (BlinkPackets.size() > 0) {
            pc.sendPacket(BlinkPackets.pollFirst(), 10, true);
        }
        BlinkPackets.clear();

        super.onDisable();
    }

    @EventTarget
    public void onJump(EventOnJump e) {
        switch (Mode.getCurrentMode()) {
            case ("Other"):
                switch (OMode.getCurrentMode()) {
                    case ("Ground"):
                        e = OtherGroundSpeed.onEventOnJump(e);
                        break;
                    case ("Hop"):
                        e = OtherSimpleHopSpeed.onEventOnJump(e);
                        break;

                }
                break;

            case ("Grim"):
                switch (GrimMode.getCurrentMode()) {
                    case ("LowHop"):
                        e = GrimLowHopSpeed.onEventOnJump(e);
                        break;
                    case ("FunnyPackets"):
                        e = GrimExploitSpeed.onEventOnJump(e);
                        break;

                }
                break;
            case ("Matrix"):
                switch (MatrixMode.getCurrentMode()) {
                    case ("TravelHop"):
                        e = MatrixTravelHopSpeed.onEventOnJump(e);
                        break;
                    case ("LowHop2"):
                        e = MatrixLowHop2Speed.onEventOnJump(e);
                        break;
                    case ("LowHop"):
                        e = MatrixLowHopSpeed.onEventOnJump(e);
                        break;
                    case ("BasicHop"):
                        e = MatrixBasicHopSpeed.onEventOnJump(e);
                        break;
                    case ("YPort"):
                        e = MatrixYPortSpeed.onEventOnJump(e);
                        break;
                    case ("BoostHop"):
                        e = MatrixBoostHopSpeed.onEventOnJump(e);
                        break;
                    case ("Ground"):
                        e = MatrixGroundSpeed.onEventOnJump(e);
                        break;
                    case ("OldFastHop"):
                        e = MatrixOldFastHopSpeed.onEventOnJump(e);
                        break;
                    case ("HighHop"):
                        e = MatrixHighHopSpeed.onEventOnJump(e);
                        break;

                }
                break;
            case ("Polar"):
                switch (PolarMode.getCurrentMode()) {
                    case ("LowHop"):
                        e = PolarLowHopSpeed.onEventOnJump(e);
                        break;
                    case ("GroundStrafe"):
                        e = PolarGroundStrafeSpeed.onEventOnJump(e);
                        break;

                }
                break;

        }

        if (!jumping) {
            e.cancel();
        }
    }

    @EventTarget
    public void onTick(EventTick e) {
        // @obf-only start
        base.client.auth.TamperResponse.checkG();
        if (base.client.auth.TamperResponse.shouldDisable()) {
            return;
        }
        // @obf-only end

        if (groundstate == 1) {
            mc.player.setOnGround(false);
            groundstate = 0;
        } else if (groundstate == 2) {
            mc.player.setOnGround(true);
            groundstate = 0;
        }
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MatrixMode.getCurrentMode()) {
                    case ("OldFastHop"):
                        MatrixOldFastHopSpeed.onEventTick();
                        break;
                    case ("Ground"):
                        MatrixGroundSpeed.onEventTick();
                        break;
                    case ("SilentFlag"):
                        MatrixSilentFlagSpeed.onEventTick();
                        break;
                    case ("HighHop"):
                        MatrixHighHopSpeed.onEventTick();
                        break;

                }
        }

    }

    @EventTarget
    public void onPostUpdate(EventPostMotion e) {
        if (MoveUtil.isFreezing)
            return;
        switch (Mode.getCurrentMode()) {

            case ("Grim"):
                switch (GrimMode.getCurrentMode()) {
                    case ("FunnyPackets"):
                        e = GrimExploitSpeed.onEventPostMotion(e);
                        break;
                    case ("Ground"):
                        GrimGroundSpeed.onEventPostMotion(e);
                        break;
                }
                break;
        }
    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {
            xt = 0;
            zt = 0;
        }
        PacketHelper.Values pc = Client.instance.packet;
        if (MoveUtil.isFreezing)
            return;
        switch (Mode.getCurrentMode()) {

            case ("Grim"):
                switch (GrimMode.getCurrentMode()) {
                    case ("FunnyPackets"):
                        e = GrimExploitSpeed.onEventPreMotion(e);
                        break;

                }
                break;

            case ("Matrix"):
                if (Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState()) {
                    return;
                }
                switch (MatrixMode.getCurrentMode()) {

                    case ("Ground"):
                        e = MatrixGroundSpeed.onEventPreMotion(e);

                        break;
                    case ("SilentFlag"):
                        e = MatrixSilentFlagSpeed.onEventPreMotion(e);
                        break;

                    case ("HighHop"):
                        MatrixHighHopSpeed.onEventPreMotion(e, xt, zt);
                        break;

                }
                break;
        }

    }

    @EventTarget
    public void eom(EventOnMovePost e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (MoveUtil.isFreezing)
            return;
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {
            xt = 0;
            zt = 0;
        }

        switch (Mode.getCurrentMode()) {

            case ("Other"):
                switch (OMode.getCurrentMode()) {
                    case ("Ground"):
                        OtherGroundSpeed.onEventOnMovePost(xt, zt);
                        break;
                    case ("Hop"):
                        OtherSimpleHopSpeed.onEventOnMovePost(xt, zt);
                        break;
                }
                break;

            case ("Polar"):
                switch (PolarMode.getCurrentMode()) {
                    case ("Basic"):
                        PolarBasicSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("LowHop"):
                        PolarLowHopSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("GroundStrafe"):
                        PolarGroundStrafeSpeed.onEventOnMovePost(e, xt, zt);
                        break;

                }
                break;

            case ("Grim"):
                switch (GrimMode.getCurrentMode()) {
                    case ("LowHop"):
                        GrimLowHopSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("FunnyPackets"):
                        e = GrimExploitSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("Ground"):
                        e = GrimGroundSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                }

                break;

            case ("Matrix"):
                if (Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState()) {
                    return;
                }

                switch (MatrixMode.getCurrentMode()) {
                    case ("LowHop2"):
                        MatrixLowHop2Speed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("TravelHop"):
                        MatrixTravelHopSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("Basic"):
                        MatrixBasicSpeed.onEventOnMovePost(xt, zt);
                        break;
                    case ("HighHop"):
                        MatrixHighHopSpeed.onEventOnMovePost(e, xt, zt);
                        break;

                    case ("Ground"):
                        MatrixGroundSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("OldFastHop"):
                        MatrixOldFastHopSpeed.onEventOnMovePost(xt, zt);
                        break;

                    case ("YPort"):
                        e = MatrixYPortSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("BasicHop"):
                        e = MatrixBasicHopSpeed.onEventOnMovePost(e, xt, zt);
                        break;
                    case ("LowHop"):
                        MatrixLowHopSpeed.onEventOnMovePost(e, xt, zt);

                        break;
                    case ("BoostHop"):
                        e = MatrixBoostHopSpeed.onEventOnMovePost(e, xt, zt);
                        break;

                }

        }

    }

    @EventTarget
    public void oncancel(EventSendPacketBlink e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();
        switch (Mode.getCurrentMode()) {

            case ("Matrix"):
                switch (MatrixMode.getCurrentMode()) {
                    case ("SilentFlag"):
                        e = MatrixSilentFlagSpeed.onEventSendPacketBlink(e);
                        break;
                    case ("TravelHop"):
                        break;
                }
                break;
        }
    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost e) {
        switch (Mode.getCurrentMode()) {
            case ("Polar"):
                switch (PolarMode.getCurrentMode()) {
                    case ("GroundStrafe"):
                        e = PolarGroundStrafeSpeed.onEventReceivePacketPost(e);
                        break;

                }
                break;
            case ("Grim"):
                switch (GrimMode.getCurrentMode()) {
                    case ("LowHop"):
                        e = GrimLowHopSpeed.onEventReceivePacketPost(e);
                        break;
                    case ("FunnyPackets"):
                        e = GrimExploitSpeed.onEventReceivePacketPost(e);
                        break;
                    case ("Ground"):
                        e = GrimGroundSpeed.onEventReceivePacketPost(e);
                        break;
                }
                break;

        }
    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPre e) {
        if (e.getPacket() instanceof ClientboundLoginPacket && AutoDisable.isEnabled()) {
            this.state = false;
            this.onDisable();
        }

        switch (Mode.getCurrentMode()) {

            case ("Matrix"):
                switch (MatrixMode.getCurrentMode()) {
                    case ("OldFastHop"):
                        e = MatrixOldFastHopSpeed.onReceivePacketPost(e);
                        break;
                    case ("TravelHop"):
                        e = MatrixTravelHopSpeed.onReceivePacketPost(e);
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
            case ("Matrix"):
                switch (MatrixMode.getCurrentMode()) {

                    case ("TravelHop"):
                        e = MatrixTravelHopSpeed.onEventSendPacketCancel(e);
                        break;
                }
                break;
        }

    }

    public static void sjump() {
        jumping = true;
        mc.player.jumpFromGround();
        jumping = false;
        index1 = 0;
    }

}
