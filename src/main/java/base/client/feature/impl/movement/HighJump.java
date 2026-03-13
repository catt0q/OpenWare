package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.exploit.SelfDamage;
import base.client.feature.impl.movement.flights.matrix.MatrixTimerlessFlight;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.inventory.InventoryUtil;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import base.client.managers.TranslationManager;
import base.mixin.client.accessors.ServerboundMovePlayerPacketAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static base.client.feature.impl.movement.Speed.GrimMotY;
import static base.client.feature.impl.movement.Speed.groundstate;

public class HighJump extends Module {

    public ModeSetting hmode;
    public NumberSetting motionBoost;
    public boolean jump = false;
    public TimerHelper timerr = new TimerHelper();

    // HighHop state variables
    private boolean highHopJumping = false;
    private boolean highHopWasBoost = false;
    private boolean highHopDone = false;

    public static ModeSetting MHMode, GHMode, IHMode, PMode;
    double lastMotY = 0;
    int index1 = 0, index2 = 0, index3 = 0, index4 = 0, index5 = 0, index6 = 0, index7 = 0, index8 = 0;
    private int ticks = 0;
    private boolean timerEnable = false;
    boolean spoofground = false;
    public static int groundstate = 0;// 0=nothing 1=noground 2=ground
    public NumberSetting mdBoost = new NumberSetting("Matrix Default Motion", 0.999F, 0.0001F, 0.99999F, 0.001F,
            () -> MHMode.isVisible() && MHMode.getCurrentMode().equals("Default"));

    public NumberSetting vanillaMotionY = new NumberSetting("Vanilla MotionY", 2.999F, 0.0001F, 9.99999F, 0.01F,
            () -> hmode.currentMode.equals("Vanilla"));
    public BooleanSetting vanillaAllowAir = new BooleanSetting("Allow In Air", false,
            () -> hmode.currentMode.equals("Vanilla"));

    NumberSetting MatrixDamageVer = new NumberSetting("Matrix Damage Vertical", 4.0f, 0.1f, 9.99f, 0.001f,
            () -> hmode.getCurrentMode().equals("Matrix") && MHMode.getCurrentMode().equals("Damage"));
    BooleanSetting DamageBoostHor = new BooleanSetting("Boost Horizontal", false,
            () -> hmode.getCurrentMode().equals("Matrix") && MHMode.getCurrentMode().equals("Damage"));
    NumberSetting MatrixDamageHor = new NumberSetting("Matrix Damage Horizontal", 1.97f, 0.1f, 1.97f, 0.001f,
            () -> hmode.getCurrentMode().equals("Matrix") && MHMode.getCurrentMode().equals("Damage")
                    && DamageBoostHor.isEnabled());

    public static BooleanSetting AutoDamage, Ver188MM;

    public static BooleanSetting AutoDisable = new BooleanSetting("Server Join Auto Disable", true, () -> true);

    public HighJump() {
        super("HighJump", "Вы подлетаете на большую высоту", Type.Movement);
        hmode = new ModeSetting("Mode", "Matrix", () -> true, "Matrix", "Vanilla", "Intave", "Grim", "Polar");
        MHMode = new ModeSetting("Matrix Mode", "Default", () -> (hmode.getCurrentMode().equals("Matrix")), "Damage",
                "Default", "Block", "HighHop");
        GHMode = new ModeSetting("Grim Mode", "Exploit1", () -> (hmode.getCurrentMode().equals("Grim")), "Exploit1",
                "ExploitPre");
        IHMode = new ModeSetting("Intave Mode", "Damage1", () -> (hmode.getCurrentMode().equals("Intave")), "Damage1");
        PMode = new ModeSetting("Polar Mode", "Simple1", () -> (hmode.getCurrentMode().equals("Polar")), "Simple1");

        AutoDamage = new BooleanSetting("Auto Damage", false,
                () -> hmode.getCurrentMode().equals("Matrix") && MHMode.getCurrentMode().equals("Damage"));
        Ver188MM = new BooleanSetting("1.8.8", false,
                () -> MHMode.isVisible() && MHMode.getCurrentMode().equals("Damage"));

        addSettings(hmode, MHMode, AutoDamage,
                vanillaMotionY, vanillaAllowAir, mdBoost, GHMode, IHMode, PMode,

                MatrixDamageVer, Ver188MM, DamageBoostHor, MatrixDamageHor, motionBoost, AutoDisable);
    }

    @Override
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        ticks = 0;
        timerEnable = false;
        switch (hmode.getCurrentMode()) {
            case ("Matrix"):
                switch (MHMode.getCurrentMode()) {
                    case ("Block"):
                        if (pc.prevslotid != mc.player.getInventory().getSelectedSlot()) {
                            pc.sendPacket(
                                    new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                        }
                        break;
                    case ("HighHop"):
                        // Reset state on disable
                        break;
                }
                break;
        }

        super.onDisable();
    }

    @Override
    public void onEnable() {
        groundstate = 0;
        // Reset HighHop state
        highHopJumping = false;
        highHopWasBoost = false;
        highHopDone = false;

        switch (hmode.getCurrentMode()) {
            case ("Matrix"):
                switch (MHMode.getCurrentMode()) {
                    case ("Block"):
                        if (InventoryUtil.getblockhotbarslot() == -1) {

                            ChatHelper.addTranslatedMessage("hotbar.need_blocks");
                            NotificationManager.publicity("Error",
                                    ChatFormatting.RED + TranslationManager.get("hotbar.need_blocks"), 4,
                                    NotificationType.ERROR);

                            Client.instance.featureManager.getModuleByClass(HighJump.class).toggle();
                            return;

                        }

                        break;
                }
                break;
        }

        TimerUtil.setTimerspeed(1);
        spoofground = false;
        if (AutoDamage.isEnabled()
                && (hmode.getCurrentMode().equals("Matrix") && MHMode.getCurrentMode().equals("Damage"))) {
            Client.instance.featureManager.getModuleByClass(SelfDamage.class).onEnable();
            Client.instance.featureManager.getModuleByClass(SelfDamage.class).state = true;
        }
        ticks = 0;
        index1 = 0;
        index2 = 0;
        index3 = 0;
        index4 = 0;
        index5 = 0;
        index6 = 0;
        index7 = 0;
        index8 = 0;
        lastMotY = 0;

        super.onEnable();
    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost event) {
        PacketHelper.Values pc = Client.instance.packet;
        String mode = hmode.getCurrentMode();
        switch (mode) {
            case ("Grim"):
                String gmode = GHMode.getCurrentMode();
                switch (gmode) {
                    case ("ExploitPre"):
                        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                            index3 = 1;
                        }
                        break;
                }
                break;
        }
    }

    @EventTarget
    public void onJump(EventOnJump e) {
        // HighHop: cancel natural jumps, only allow when highHopJumping is true
        if (hmode.getCurrentMode().equals("Matrix") && MHMode.getCurrentMode().equals("HighHop")) {
            if (!highHopJumping) {
                e.cancel();
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (e.getPacket() instanceof ClientboundLoginPacket && AutoDisable.isEnabled()) {
            this.toggle();
        }
        String mode = hmode.getCurrentMode();

        switch (mode) {
            case ("Grim"):
                String gmode = GHMode.getCurrentMode();
                switch (gmode) {
                    case ("ExploitPre"):
                        if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                            if (((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()) {
                                ClientboundSetEntityMotionPacket s12 = (ClientboundSetEntityMotionPacket) e.getPacket();
                                if (s12.getMovement().y < 0) {
                                    this.toggle();
                                }

                            }
                        }
                        break;
                }
                break;
            case ("Intave"):
                String imode = IHMode.getCurrentMode();
                switch (imode) {
                    case ("Damage1"):
                        boolean iss12 = ((e.getPacket() instanceof ClientboundExplodePacket)
                                || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket)
                                        && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player
                                                .getId()));
                        if (iss12) {
                            if (index2 == 0) {
                                index2 = 1;
                                e.setCancelled(true);
                                TimerUtil.setTimerspeed(1);
                            }
                        }
                        break;
                }
                break;

            case ("Matrix"):
                String mmode = MHMode.getCurrentMode();
                switch (mmode) {
                    case ("Damage"):
                        boolean iss12 = ((e.getPacket() instanceof ClientboundExplodePacket)
                                || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket)
                                        && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player
                                                .getId()));

                        if (iss12) {
                            e.cancel();
                            index2 = 1;
                            MoveUtil.limit2speed(0.1);
                        } else if (e.getPacket() instanceof ClientboundPlayerPositionPacket) {
                            ClientboundPlayerPositionPacket s08 = (ClientboundPlayerPositionPacket) e.getPacket();
                            if (index2 == 1) {
                                e.cancel();
                                mc.player.setPos(s08.change().position().x, s08.change().position().y,
                                        s08.change().position().z);

                                if (Ver188MM.isEnabled()) {
                                    pc.sendPacket(
                                            new ServerboundMovePlayerPacket.Pos(s08.change().position().x,
                                                    s08.change().position().y, s08.change().position().z, false, false),
                                            10, true);
                                } else {
                                    pc.LastTpNum++;
                                    pc.sendPacket(new ServerboundAcceptTeleportationPacket(s08.id()));
                                }

                                if (lastMotY != 0) {
                                    MoveUtil.setmotY(lastMotY);
                                }

                                index5 = 0;
                                mc.player.setOnGround(false);

                                MoveUtil.minstrafe(0.1);
                                if (DamageBoostHor.isEnabled()) {
                                    MoveUtil.smartstrafe(MatrixDamageHor.getValue());
                                    if (index3 == 1) {
                                        MoveUtil.mult2ds(0.9D);
                                    }
                                }

                                index4 = 0;
                                index3++;
                            }
                            if (index3 >= 9) {
                                this.toggle();
                            }

                        }
                        break;
                }
                break;

        }

    }

    @EventTarget
    public void Move(EventOnMovePost eventMove) {
        String mode = hmode.getCurrentMode();
        PacketHelper.Values pc = Client.instance.packet;
        double yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {
            xt = 0;
            zt = 0;
        }

        switch (mode) {

            case ("Vanilla"):
                // allow air bypasses ground check
                if (vanillaAllowAir.isEnabled() || (mc.player.onGround() && mc.player.verticalCollision)) {
                    MoveUtil.setmotY(vanillaMotionY.getValue());
                    this.toggle();
                }
                break;
            case ("Intave"):
                String imode = IHMode.getCurrentMode();
                switch (imode) {
                    case ("Damage1"):
                        if (index2 == 1) {
                            if (ticks == 0) {
                                TimerUtil.setTimerspeed(1);
                                MoveUtil.stop2();
                                if (mc.player.onGround() && mc.player.verticalCollision) {
                                    MoveUtil.setmotY(0.42);
                                }

                            }
                            if (ticks == 13) {
                                TimerUtil.setTimerspeed(1F);
                            }
                            ticks++;
                            // ChatHelper.addChatMessage("" + ticks);

                        }
                        break;
                }
                break;

            case ("Grim"):
                String gmode = GHMode.getCurrentMode();
                switch (gmode) {
                    case ("Exploit1"):
                        if (ticks == 0) {
                            mc.player.jumpFromGround();
                        }
                        TimerUtil.setTimerspeed(1.1F);
                        if (ticks > 0) {

                            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false, false), 10, true);
                            pc.sendPacket(new ServerboundClientTickEndPacket(), 10);

                            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,
                                    ServerboundPlayerCommandPacket.Action.START_FALL_FLYING, 0), 5, true);
                            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,
                                    ServerboundPlayerCommandPacket.Action.START_FALL_FLYING, 0), 10, true);

                            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false, false), 10, true);
                            pc.sendPacket(new ServerboundClientTickEndPacket(), 10);

                            MoveUtil.stop3();

                        }
                        ticks++;

                        break;
                    case ("ExploitPre"):
                        if (index1 == 0) {
                            mc.player.jumpFromGround();
                        }
                        if (index1 == 1) {
                            index2 = 1;
                            for (int i = 0; i < 20; i++) {
                                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,
                                        mc.player.horizontalCollision), 10);
                            }
                            index3 = 1;
                        }

                        index1++;

                        break;
                }
                break;
            case ("Polar"):
                String pmode = PMode.getCurrentMode();
                switch (pmode) {
                    case ("Simple1"):
                        if (ticks > 3 && mc.player.verticalCollision && mc.player.onGround()) {
                            this.toggle();
                            return;
                        }

                        if (ticks == 0) {
                            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX() + xt * 1,
                                    mc.player.getY() + 0.6, mc.player.getZ() + zt * 1, 0, 0, true,
                                    mc.player.horizontalCollision), 10);

                            TimerUtil.setTimerspeed(0.1);
                        } else if (ticks == 1) {
                            mc.player.jumpFromGround();
                            MoveUtil.setmotY(0.6);
                            TimerUtil.setTimerspeed(1);
                        } else {

                            double bstn = 0.00249;
                            // MoveUtil.addmotXZ(xt * bstn,zt * bstn);

                        }

                        ticks++;
                        break;
                }
                break;
            case ("Matrix"):
                String mmode2 = MHMode.getCurrentMode();
                if (mmode2.equals("HighHop")) {
                    // HighHop: check for landing after high hop is done
                    if (highHopDone && mc.player.onGround() && mc.player.verticalCollision) {
                        this.toggle();
                    }
                }
                break;

        }

    }

    @EventTarget
    public void onTick(EventTick e) {
        String mode = hmode.getCurrentMode();
        String mmode = MHMode.getCurrentMode();
        PacketHelper.Values pc = Client.instance.packet;

        if (mode.equals("Matrix")) {
            if (mmode.equals("Default")) {
                if (spoofground) {
                    mc.player.setOnGround(false);
                    MoveUtil.setmotY(mdBoost.getValue());
                    this.toggle();
                }
            } else if (mmode.equals("Damage")) {
                if (spoofground) {
                    mc.player.setOnGround(false);
                    spoofground = false;
                }
            } else if (mmode.equals("HighHop")) {
                // Ground state spoofing for HighHop
                if (groundstate == 1) {
                    mc.player.setOnGround(false);
                    groundstate = 0;
                } else if (groundstate == 2) {
                    mc.player.setOnGround(true);
                    groundstate = 0;
                }
            }
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
    public void onPreMotion(EventPreMotion e) {
        String mode = hmode.getCurrentMode();
        String mmode = MHMode.getCurrentMode();
        PacketHelper.Values pc = Client.instance.packet;
        switch (mode) {
            case ("Matrix"):
                switch (mmode) {

                    case ("Block"):
                        if (mc.player.getY() - ACUtil.findnearestgroundY10() > 4
                                || (!ACUtil.isground() && mc.player.getDeltaMovement().y < 0)) {

                            toggle();
                            return;
                        }
                        int slott = mc.player.getInventory().getSelectedSlot();
                        if (index1 % 2 == 0) {

                            groundstate = 2;

                            mc.player.jumpFromGround();

                            e.setOnGround(true);
                        }
                        boolean selected = mc.player.getInventory().getSelectedItem().getItem() instanceof BlockItem;
                        if (!selected && InventoryUtil.getblockhotbarslot() != -1
                                && pc.prevslotid != InventoryUtil.getblockhotbarslot()) {
                            pc.sendPacket(new ServerboundSetCarriedItemPacket(InventoryUtil.getblockhotbarslot()));
                        }
                        BlockPos bpp = new BlockPos(mc.player.getBlockX(), (int) ACUtil.findnearestgroundY10(),
                                mc.player.getBlockZ());

                        HitResult hr = TraceUtil.ClientpickNew(12, 1, false, mc.player.getYRot(), 90);
                        BlockHitResult bhr = (BlockHitResult) hr;
                        bhr = bhr.withPosition(bpp).withDirection(Direction.UP);
                        // mc.player.swing(InteractionHand.MAIN_HAND);

                        pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,
                                bhr, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));

                        index1++;
                        break;

                    case ("Default"):
                        if (!(mc.player.verticalCollision && mc.player.onGround())) {
                            ticks = -2;
                        }
                        if (ticks >= 0) {
                            spoofground = true;
                            e.setOnGround(false);
                        }
                        ticks++;
                        break;
                    case ("Damage"):
                        if (index2 == 1) {
                            e.setOnGround(false);
                            spoofground = true;
                            if (ticks == 0) {
                                MoveUtil.setmotY(MatrixDamageVer.getValue());
                                lastMotY = 0;
                                MoveUtil.minstrafe(0.1);
                                if (DamageBoostHor.isEnabled()) {
                                    MoveUtil.smartstrafe(1.97);
                                }

                                index4 = 0;
                                TimerUtil.setTimerspeed(0.3f);
                                index5 = 1;
                            }
                            if (ticks == 1 && index4 == 0) {
                                lastMotY = mc.player.getDeltaMovement().y();
                            }

                            if (index4 == 2) {
                                TimerUtil.reset();
                            }
                            if (index4 >= 7) {
                                this.toggle();
                            }

                            index4++;
                            ticks++;
                        }
                        break;
                    case ("HighHop"):
                        // Main HighHop logic - must be in PreMotion like Speed's version
                        highHopWasBoost = false;

                        if (highHopDone) {
                            break; // Waiting to land, handled in Move event
                        }

                        // First jump from ground
                        if (mc.player.onGround() && mc.player.verticalCollision) {
                            highHopWasBoost = true;
                            index2 = 1;
                            groundstate = 2;
                            highHopJumping = true;
                            mc.player.jumpFromGround();
                            highHopJumping = false;
                            index1 = 0;
                            if (!ACUtil.matrixisusingitem() && !mc.player.isShiftKeyDown()) {
                                MoveUtil.minsmartstrafe(0.3);
                            }
                        }

                        // Second jump when matrix detects ground mid-air
                        if (!highHopWasBoost && ACUtil.ismatrixonground()
                                && mc.player.getDeltaMovement().y() >= 0
                                && (index2 != 1 || !pc.LastGround)
                                && (!mc.player.horizontalCollision || index1 != 2 || MoveUtil.getspeed2() > 0.04)) {
                            e.setOnGround(true);
                            groundstate = 2;
                            CombatUtil.fallDistance = 0;
                            highHopJumping = true;
                            mc.player.jumpFromGround();
                            highHopJumping = false;
                            if (!ACUtil.matrixisusingitem() && !mc.player.isShiftKeyDown()) {
                                MoveUtil.minsmartstrafe(0.3);
                            }
                            index2++;
                            highHopDone = true;
                        }

                        index1++;
                        break;

                }

                break;
            case ("Grim"):
                String gmode = GHMode.getCurrentMode();
                switch (gmode) {
                    case ("ExploitPre"):
                        e.setPitch((float) (pc.LastPitch + Math.random() * 0.001));
                        e.setYaw((float) (pc.LastYaw + Math.random() * 0.001));

                        if (index2 == 0)
                            return;
                        TimerUtil.setTimerspeed(10);
                        MoveUtil.setmotY(0.42);

                        if (index3 == 1) {
                            index3 = 2;
                            TimerUtil.setTimerspeed(1);
                            index6 = 0;
                            index7 = 0;
                        } else if (index3 == 2) {
                            for (int i = 0; i < 2; i++) {
                                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,
                                        mc.player.horizontalCollision), 10);
                            }
                            index5 = 1;
                            index7 = 1;
                            MoveUtil.stop3();
                            index3 = 0;
                        }

                        else {
                            MoveUtil.stop3();
                            index5 = 1;
                            index6++;
                            if (index6 > 6 * TimerUtil.getTimerspeed()) {
                                this.toggle();
                            }
                            index7 = 1;
                        }
                        break;
                }
                break;

        }

        String highJumpMode = hmode.currentMode;
        this.setSuffix(highJumpMode);

    }

    @EventTarget
    public void onEMI(EventMoveInput e) {
        String mode = hmode.getCurrentMode();
        String mmode = MHMode.getCurrentMode();
        switch (mode) {
            case ("Matrix"):
                switch (mmode) {
                    case ("Damage"):
                        if (index2 == 1) {
                            if (index4 < 5) {
                                e.backward = false;
                                e.forward = false;
                                e.jump = false;
                                e.sneak = false;
                                e.sprint = false;
                                e.left = false;
                                e.right = false;
                            }
                        }
                        break;
                }
                break;
            case ("Grim"):
                switch (mmode) {
                    case ("ExploitPre"):
                        if (index7 == 1) {

                            e.backward = false;
                            e.forward = false;
                            e.jump = false;
                            e.sneak = false;
                            e.sprint = false;
                            e.left = false;
                            e.right = false;

                        }
                        break;
                }
                break;
        }

    }

    @EventTarget
    public void onSendPacket(EventSendPacketCancel e) {
        if (e.isCancelled())
            return;
        String mode = hmode.getCurrentMode();
        Packet<?> packetp = e.getPacket();
        switch (mode) {
            case ("Intave"):
                String imode = IHMode.getCurrentMode();
                switch (imode) {
                    case ("Intave"):
                        if (packetp instanceof ServerboundPongPacket) {
                            if (index2 == 1 && ticks < 11) {
                                e.setCancelled(true);
                            }

                        }
                        break;
                }
                break;

            case ("Matrix"):
                String mmode = MHMode.getCurrentMode();
                switch (mmode) {
                    case ("Damage"):
                        if (packetp instanceof ServerboundMovePlayerPacket) {
                            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;
                            if (index2 == 1) {

                                ServerboundMovePlayerPacketAccessor accessor = (ServerboundMovePlayerPacketAccessor) packetp;
                                accessor.setOnGround(false);
                            }

                        }
                        break;
                }
                break;
            case ("Grim"):
                String gmode = GHMode.getCurrentMode();
                switch (gmode) {
                    case ("ExploitPre"):
                        if (e.getPacket() instanceof ServerboundMovePlayerPacket) {
                            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;
                            if (index5 == 1) {
                                index5 = 0;
                                e.cancel();
                            }

                        }
                        break;
                }
                break;

        }

    }

}
