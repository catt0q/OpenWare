package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.AirStuck;
import base.client.feature.impl.movement.Flight;
import base.client.feature.impl.movement.HighJump;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;

import static base.client.feature.impl.exploit.Disabler.notimerbalance;

public class NoFall extends Module {
    boolean spoofgroundmotion = false;
    boolean spoofnextvel = false;
    int flagskip = 0;
    public static ModeSetting Mode, PMode, GMode, MMode, OMode, NCPMode, AACMode, SMode, IMode;
    public TimerHelper timerHelper = new TimerHelper();

    // NonPrediction saved HighJump settings
    private String savedHJMode = null;
    private float savedHJMotionY = 0;
    private boolean savedHJAllowAir = false;
    private boolean hjHijacked = false;
    int index1;
    int index2;
    int index3;
    int index4;
    int index5;
    int groundstate = 0;
    double indexd1;
    double indexd2;
    double indexd3;
    double indexd4;
    double indexd5;
    double indexd6;
    double indexd7;
    double indexd8;
    public static double totalfalldist = 0;
    public static BooleanSetting AutoDisable = new BooleanSetting("Server Join Auto Disable", true, () -> true);

    boolean modifyc03 = false;

    public NoFall() {
        super("NoFall", "Позволяет получить меньший дамаг при падении", Type.Player);
        Mode = new ModeSetting("Mode", "Matrix", () -> true, "Matrix", "Simple", "NCP", "AAC", "Intave", "Grim",
                "Other", "NonPrediction");

        PMode = new ModeSetting("Polar Mode", "Basic", () -> Mode.getCurrentMode().equals("Polar"), "Basic");
        GMode = new ModeSetting("Grim Mode", "PacketV1", () -> Mode.getCurrentMode().equals("Grim"), "PacketV1",
                "PacketV2", "NoGround", "NanoPacket");
        MMode = new ModeSetting("Matrix Mode", "NewPacket", () -> Mode.getCurrentMode().equals("Matrix"), "663",
                "OldPacket", "NewPacket", "NewSlow", "ML-SW");
        IMode = new ModeSetting("Intave Mode", "PacketSlow", () -> Mode.getCurrentMode().equals("Intave"), "PacketSlow",
                "FlagJump");
        SMode = new ModeSetting("Simple Mode", "OnlyGround", () -> Mode.getCurrentMode().equals("Simple"), "OnlyGround",
                "GroundCancel");
        AACMode = new ModeSetting("AAC Mode", "Flags", () -> Mode.getCurrentMode().equals("AAC"), "Flags", "AAC6Test");
        NCPMode = new ModeSetting("NCP Mode", "OldHypixel", () -> Mode.getCurrentMode().equals("NCP"), "OldHypixel");
        OMode = new ModeSetting("Other Mode", "OldSpartan", () -> Mode.getCurrentMode().equals("Other"), "OldSpartan");

        addSettings(Mode, GMode, MMode, IMode, AutoDisable);
    }

    @Override
    public void onEnable() {
        groundstate = 0;
        this.index1 = 0;
        this.index2 = 0;
        this.index3 = 0;
        this.index4 = 0;
        this.index5 = 0;
        totalfalldist = 0;
        this.indexd1 = 0;
        this.indexd2 = 0;
        this.indexd3 = 0;
        this.indexd4 = 0;
        this.indexd5 = 0;
        this.indexd6 = 0;
        this.indexd7 = 0;
        this.indexd8 = 0;
        flagskip = 0;

        spoofgroundmotion = false;

        super.onEnable();

    }

    @Override
    public void onDisable() {
        totalfalldist = 0;
        if (mc.player != null)
            TimerUtil.setTimerspeed(1);

        super.onDisable();

    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre event) {
        String mode = Mode.getCurrentMode();
        if (event.getPacket() instanceof ClientboundLoginPacket && AutoDisable.isEnabled()) {
            this.state = false;
            this.onDisable();
        }

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("NewPacket"):
                        if (mc.player.hurtTime > 0 && event.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                            if (((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player.getId()) {
                                if (((ClientboundSetEntityMotionPacket) event.getPacket()).getMovement().y < 0) {
                                    event.setCancelled(true);
                                    index2 = 1;
                                }
                            }
                        }
                        break;
                    case ("OldPacket"):
                        if (mc.player.hurtTime > 0 && event.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                            if (((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player.getId()) {
                                if (spoofnextvel) {
                                    spoofnextvel = false;
                                    event.setCancelled(true);
                                    MoveUtil.setmotY(-0.01);
                                }
                            }
                        }
                        break;
                    case ("ML-SW"):
                        if (event.getPacket() instanceof ClientboundSystemChatPacket) {
                            ClientboundSystemChatPacket s01 = (ClientboundSystemChatPacket) event.getPacket();
                            if (s01.content().getSiblings().get(0).getString()
                                    .contains("You cannot leave the arena.")) {
                                event.cancel();
                            }

                        }
                        break;

                }
                break;

            case ("Intave"):
                switch (IMode.getCurrentMode()) {
                    case ("PacketSlow"):
                        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                            if (((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player.getId()) {
                                if (index2 == 1) {
                                    ClientboundSetEntityMotionPacket s12 = (ClientboundSetEntityMotionPacket) event
                                            .getPacket();
                                    MoveUtil.setmotXYZ(s12.getMovement().x / 8000D, s12.getMovement().y / 8000D,
                                            s12.getMovement().z / 8000D);
                                    event.cancel();
                                    index2 = 2;
                                }
                            }
                        }
                        break;

                }
                break;

            case ("Grim"):
                break;

        }

        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket s08 = (ClientboundPlayerPositionPacket) event.getPacket();
            if (flagskip > 0) {
                event.setCancelled(true);
                flagskip--;

            }
        }

    }

    @EventTarget
    public void onTick(EventTick e) {
        if (groundstate == 1) {
            mc.player.setOnGround(false);
            groundstate = 0;
        } else if (groundstate == 2) {
            mc.player.setOnGround(true);
            groundstate = 0;
        }

        if (spoofgroundmotion) {
            mc.player.setOnGround(false);
            spoofgroundmotion = false;
        }
    }

    @EventTarget
    public void onPreMotion(EventPreMotion event) {
        PacketHelper.Values pc = Client.instance.packet;
        String mode = Mode.getCurrentMode();
        this.setSuffix(mode);

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("NewPacket"):
                        if ((mc.player.onGround() && mc.player.verticalCollision) && CombatUtil.fallDistance == 0) {
                            totalfalldist = 0;
                            if (TimerUtil.getTimerspeed() == 0.287F) {
                                TimerUtil.reset();
                            }
                        }
                        boolean f2 = true;
                        if (ACUtil.ismatrixonground()
                                || (Client.instance.featureManager.getModuleByClass(Flight.class).getState()
                                && Flight.Mode.getCurrentMode().equals("Matrix")
                                && Flight.MMode.getCurrentMode().equals("DamageFloat"))) {
                            f2 = false;
                            return;
                        }
                        if (CombatUtil.ofallDistance > 15) {
                            f2 = false;
                            return;
                        }

                        if (mc.player.getDeltaMovement().y > 0.1
                                || Client.instance.featureManager.getModuleByClass(AirStuck.class).getState()) {
                            f2 = false;
                            return;
                        }

                        if (f2 && (CombatUtil.fallDistance > 2.5 || (totalfalldist > 20))) {
                            totalfalldist += CombatUtil.fallDistance;
                            CombatUtil.resetfd(0.001F);
                            index2 = 2;
                            if (notimerbalance()) {
                                TimerUtil.setTimerspeed(0.287F);
                            }
                        } else {
                            if (TimerUtil.getTimerspeed() == 0.287F) {
                                TimerUtil.reset();
                            }
                        }

                        break;

                    case ("663"):

                        // if (CombatUtil.fallDistance - mc.player.getDeltaMovement().y > 3 ||
                        // (Math.abs((ACUtil.matrixisvoidcheckblocks()==-1 ? 0 :
                        // ACUtil.matrixisvoidcheckblocks()) - mc.player.getY()) < 3 &&
                        // CombatUtil.fallDistance - mc.player.getDeltaMovement().y > 2)) {
                        if ((mc.player.onGround() && mc.player.verticalCollision) && CombatUtil.fallDistance == 0) {
                            totalfalldist = 0;
                            if (TimerUtil.getTimerspeed() == 0.287F) {
                                TimerUtil.reset();
                            }
                        }
                        boolean f = true;
                        if (ACUtil.ismatrixonground()
                                || (Client.instance.featureManager.getModuleByClass(Flight.class).getState()
                                && Flight.Mode.getCurrentMode().equals("Matrix")
                                && Flight.MMode.getCurrentMode().equals("DamageFloat"))) {
                            f = false;
                            return;
                        }
                        if (CombatUtil.fallDistance > 15) {
                            f = false;
                            return;
                        }
                        if (f && (CombatUtil.fallDistance > 2.5 || (totalfalldist > 20))) {
                            totalfalldist += CombatUtil.fallDistance;
                            CombatUtil.resetfd();
                            index2 = 1;

                            if (notimerbalance()) {
                                TimerUtil.setTimerspeed(0.287F);
                            }
                        } else {
                            if (TimerUtil.getTimerspeed() == 0.287F) {
                                TimerUtil.reset();
                            }
                        }

                        break;

                    case ("OldPacket"):
                        if (CombatUtil.fallDistance > 3.5) {
                            CombatUtil.fallDistance = (float) (Math.random() * 1.0E-12);
                            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY + 0.00000000001,
                                    pc.LastPosZ, true, mc.player.horizontalCollision), 10, true);
                            MoveUtil.mult2ds(0.75);
                            spoofnextvel = true;
                        }
                        break;

                }
                break;

            case ("Intave"):
                switch (IMode.getCurrentMode()) {
                    case ("PacketSlow"):
                        if (index2 == 1) {
                            event.setYaw(pc.LastYaw);
                            event.setPitch(pc.LastPitch);
                        }
                        break;
                    case ("FlagJump"):
                        event.setOnGround(false);
                        if (mc.player.verticalCollision && MoveUtil.motYstate() < 1) {
                            mc.player.setOnGround(false);
                            spoofgroundmotion = true;
                        }
                        event.setPosY(event.getPosY() + 0.015);
                        break;
                }
                break;

            case ("Grim"):
                switch (GMode.getCurrentMode()) {
                    case ("PacketV1"):
                        if (CombatUtil.fallDistance >= 4) {
                            CombatUtil.resetfd();
                            index5++;
                        }
                        if (mc.player.onGround() && mc.player.verticalCollision && index5 > 0) {
                            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,
                                    ServerboundPlayerCommandPacket.Action.START_FALL_FLYING), 10);
                        }

                        break;

                    case ("PacketV2"):

                        if (((mc.player.onGround() && mc.player.verticalCollision) && CombatUtil.fallDistance == 0)
                                || mc.player.isFallFlying()) {
                            totalfalldist = 0;

                        }

                        if (index2 > 0) {
                            index2--;
                            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ,
                                    (float) (mc.player.getYRot() + 0.001 * Math.random()),
                                    (float) (mc.player.getXRot() + 0.0001 * Math.random()), true,
                                    mc.player.horizontalCollision), 10, true);
                            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,
                                    ServerboundPlayerCommandPacket.Action.START_FALL_FLYING), 10);

                        } else if ((CombatUtil.fallDistance > 2.5 || (totalfalldist > 10))
                                && !ACUtil.ismatrixonground()) {
                            totalfalldist += CombatUtil.fallDistance;

                            index2 = 5;
                            MoveUtil.stop3();
                            CombatUtil.resetfd();
                            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ,
                                    (float) (mc.player.getYRot() + 0.001 * Math.random()),
                                    (float) (mc.player.getXRot() + 0.0001 * Math.random()), true,
                                    mc.player.horizontalCollision), 10, true);
                            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,
                                    ServerboundPlayerCommandPacket.Action.START_FALL_FLYING), 10);

                        }

                        break;

                    case ("NoGround"):
                        event.setOnGround(false);

                        TimerUtil.setTimerspeed(0.5);

                        if (MoveUtil.motYstate() > 0 && mc.player.verticalCollision) {
                            mc.player.jumpFromGround();
                        } else {
                            mc.player.setOnGround(false);
                            spoofgroundmotion = true;
                            // pc.sendPacket(new ServerboundMovePlayerPacket(),true);
                            event.setPosY(event.getPosY() + 0.00001);
                        }

                        /*
                         * if(CombatUtil.fallDistance>=4) { CombatUtil.resetfd(); index5++; }
                         * if( mc.player.onGround && mc.player.verticalCollision && index5>0) {
                         *
                         *
                         * if(!pc.LastGround) { event.setOnGround(false);
                         * }
                         * mc.timer.timerSpeed=0.5f;
                         * }
                         */
                        break;
                    case ("NanoPacket"):
                        if (!pc.LastGround && !mc.player.onGround() && CombatUtil.fallDistance > 3) {
                            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY - 0.0002,
                                    pc.LastPosZ, pc.LastYaw, pc.LastPitch, pc.LastGround,
                                    mc.player.horizontalCollision), 10);
                            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY + 0.0002,
                                    pc.LastPosZ, pc.LastYaw, pc.LastPitch, pc.LastGround,
                                    mc.player.horizontalCollision), 10);
                            CombatUtil.resetfd(0.01F);
                        }
                        break;
                }
                break;

            case ("Simple"):
                switch (SMode.getCurrentMode()) {
                    case ("OnlyGround"):
                        if (CombatUtil.fallDistance > 3) {
                            event.setOnGround(true);
                            pc.sendPacket(
                                    new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision),
                                    10);
                        }
                        break;
                    case ("GroundCancel"):
                        event.setOnGround(false);
                        mc.player.setOnGround(false);
                        spoofgroundmotion = true;
                        break;
                }
                break;

            case ("NCP"):
                switch (NCPMode.getCurrentMode()) {
                    case ("OldHypixel"):
                        if (CombatUtil.fallDistance > 3.4) {
                            event.setOnGround(mc.player.tickCount % 2 == 0);
                        }
                        break;

                }
                break;

            case ("AAC"):
                switch (AACMode.getCurrentMode()) {
                    case ("Flags"):
                        MoveUtil.addmotY(-0.1);
                        event.setOnGround(true);
                        mc.player.getAbilities().invulnerable = true;
                        break;

                }
                break;

            case ("Other"):
                switch (OMode.getCurrentMode()) {
                    case ("OldSpartan"):
                        if (CombatUtil.fallDistance > 3.5f) {
                            if (timerHelper.hasReached(150L)) {
                                MoveUtil.stop2();
                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(),
                                        mc.player.getZ(), true, mc.player.horizontalCollision), 10);
                                timerHelper.reset();
                            } else {
                                mc.player.setOnGround(false);
                                spoofgroundmotion = true;
                            }
                        }
                        break;

                }
                break;

            case ("NonPrediction"):
                HighJump hj = (HighJump) Client.instance.featureManager.getModuleByClass(HighJump.class);

                if (hjHijacked && !hj.getState()) {
                    hj.hmode.setListMode(savedHJMode);
                    hj.vanillaMotionY.setValueNumber(savedHJMotionY);
                    hj.vanillaAllowAir.setState(savedHJAllowAir);
                    hjHijacked = false;
                }

                if (CombatUtil.fallDistance >= 3 && mc.player.getDeltaMovement().y < 0 && !hjHijacked) {
                    double groundY = ACUtil.findnearestgroundY10();
                    if (groundY != -1) {
                        double distanceToGround = mc.player.getY() - groundY - 1.0;
                        double velocity = Math.abs(mc.player.getDeltaMovement().y);
                        double triggerWindow = velocity + 0.05;
                        if (distanceToGround > 0 && distanceToGround <= triggerWindow) {
                            savedHJMode = hj.hmode.getCurrentMode();
                            savedHJMotionY = hj.vanillaMotionY.getValue();
                            savedHJAllowAir = hj.vanillaAllowAir.isEnabled();

                            hj.hmode.setListMode("Vanilla");
                            hj.vanillaMotionY.setValueNumber(1.0E-4f);
                            hj.vanillaAllowAir.setState(true);

                            if (!hj.getState()) {
                                hj.toggle();
                            }
                            hjHijacked = true;
                            CombatUtil.resetfd();
                        }
                    }
                }
                break;

        }

    }

    @EventTarget
    public void Move(EventOnMovePost eventMove) {
        PacketHelper.Values pc = Client.instance.packet;
        String mode = Mode.getCurrentMode();

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("NewSlow"):
                        if (mc.player.getY() > -64 && mc.player.getDeltaMovement().y() < 0) {
                            if (index2 == 1) {
                                index2 = 0;
                                // MoveUtil.minsmartstrafe(0.199);
                            }
                            boolean f = true;
                            if (Client.instance.featureManager.getModuleByClass(Flight.class).getState()
                                    && Flight.Mode.getCurrentMode().equals("Matrix")
                                    && Flight.MMode.getCurrentMode().equals("DamageFloat")) {
                                f = false;
                            }

                            if (f && CombatUtil.fallDistance > 2.5) {
                                pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                pc.LastTpNum++;
                                pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                        pc.LastPosY, pc.LastPosZ, pc.LastTpNum, true, true));

                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ,
                                        false, mc.player.horizontalCollision), 10, true);
                                Disabler.savedabusepacket--;

                                pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);
                                pc.LastTpNum++;
                                pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                        pc.LastPosY, pc.LastPosZ, pc.LastTpNum, true, true));
                                pc.sendPacket(
                                        new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision),
                                        10, true);
                                Disabler.savedabusepacket--;

                                mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
                                totalfalldist += CombatUtil.fallDistance;
                                CombatUtil.resetfd(0.01F);
                                index2 = 1;
                                MoveUtil.setmotY(-0.078);
                                MoveUtil.limit2speed(mc.player.isUsingItem() ? 0.1 : 0.15);

                            }

                            if ((mc.player.onGround() && mc.player.verticalCollision) && CombatUtil.fallDistance == 0) {
                                totalfalldist = 0;
                            }
                            index1++;
                        }
                        break;

                    case ("ML-SW"):
                        if (mc.player.getY() > -64 && mc.player.getDeltaMovement().y() < 0) {
                            boolean f1 = true;
                            if (Client.instance.featureManager.getModuleByClass(Flight.class).getState()
                                    && Flight.Mode.getCurrentMode().equals("Matrix")
                                    && Flight.MMode.getCurrentMode().equals("DamageFloat")) {
                                f1 = false;
                            }

                            if (f1 && CombatUtil.fallDistance > 2.5) {

                                pc.sendPacket(ACUtil.matrixflagpacket(), 10, true);

                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ,
                                        true, mc.player.horizontalCollision), 10, true);
                                ACUtil.send117DuplPacket();

                                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX,
                                        pc.LastPosY, pc.LastPosZ, pc.LastTpNum, false, false));
                                // Client.instance.flagsch.getFlags().addFirst(new
                                // FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY,
                                // pc.LastPosZ,pc.LastTpNum,false,false));

                                totalfalldist += CombatUtil.fallDistance;
                                CombatUtil.resetfd(0.01F);
                                mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
                                MoveUtil.setmotY(-0.078);
                                MoveUtil.limit2speed(mc.player.isUsingItem() ? 0.1 : 0.15);

                            }

                            if ((mc.player.onGround() && mc.player.verticalCollision) && CombatUtil.fallDistance == 0) {
                                totalfalldist = 0;
                            }
                            index1++;
                        }
                        break;

                }
                break;

            case ("Intave"):
                switch (IMode.getCurrentMode()) {
                    case ("PacketSlow"):
                        if (CombatUtil.fallDistance > 3 && !ACUtil.matrixisvoidcheck()) {
                            pc.sendPacket(
                                    new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision), 10,
                                    true);
                            MoveUtil.stop3();
                            TimerUtil.setTimerspeed(0.1);
                            CombatUtil.resetfd();
                            index1 = 0;
                            index2 = 1;
                        }

                        if ((index1 == 5 && index2 == 1) || index2 == 2) {
                            TimerUtil.setTimerspeed(1);
                        }
                        break;
                }
                break;

            case ("Grim"):
                switch (GMode.getCurrentMode()) {

                }
                break;

        }
    }

    @EventTarget
    public void oncand(EventSendPacketCancel e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("663"):
                        if (e.getPermissionidstate() > 3) {
                            return;
                        }

                        if (e.getPacket() instanceof ServerboundMovePlayerPacket) {
                            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;
                            if (index2 == 1) {
                                index2 = 0;

                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(c03.getX(pc.LastPosX),
                                        c03.getY(pc.LastPosY), c03.getZ(pc.LastPosZ),
                                        true, mc.player.horizontalCollision));
                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(c03.getX(pc.LastPosX),
                                        c03.getY(pc.LastPosY), c03.getZ(pc.LastPosZ),
                                        false, mc.player.horizontalCollision));

                                if (packetp instanceof ServerboundMovePlayerPacket.PosRot) {
                                    ServerboundMovePlayerPacket.PosRot c06 = (ServerboundMovePlayerPacket.PosRot) packetp;
                                    e.cancel();
                                    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(c06.x, c06.y, c06.z, c06.onGround,
                                            c06.horizontalCollision), 5);
                                }

                            }
                        }
                        break;
                    case ("NewPacket"):
                        if (e.getPermissionidstate() > 3) {
                            return;
                        }

                        if (e.getPacket() instanceof ServerboundMovePlayerPacket) {
                            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;
                            if (index2 > 0) {
                                if (index2 == 1) {
                                    index2 = 0;
                                }

                                if (packetp instanceof ServerboundMovePlayerPacket.PosRot) {
                                    ServerboundMovePlayerPacket.PosRot c06 = (ServerboundMovePlayerPacket.PosRot) packetp;
                                    e.cancel();
                                    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(c06.x, c06.y, c06.z, c06.onGround,
                                            c06.horizontalCollision), 5);
                                }

                            }

                        }
                        break;

                }
                break;

        }

    }

    @EventTarget
    public void oncand(EventSendPacketPost e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {

                    case ("NewPacket"):

                        if (e.getPacket() instanceof ServerboundMovePlayerPacket) {
                            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;
                            if (index2 == 2) {
                                index2 = 1;
                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(),
                                        mc.player.getZ(), false, mc.player.horizontalCollision), 10);

                                pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(),
                                        mc.player.getZ(), true, mc.player.horizontalCollision), 10);

                            }

                        }
                        break;

                }
                break;

        }

    }
}