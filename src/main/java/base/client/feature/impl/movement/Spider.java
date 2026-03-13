package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.SelfDamage;
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

public class Spider extends Module {

    public ModeSetting Mode;
    public boolean jump = false;
    public TimerHelper timerr = new TimerHelper();

    double lastMotY = 0;
    int index1 = 0, index2 = 0, index3 = 0, index4 = 0, index5 = 0, index6 = 0, index7 = 0, index8 = 0;
    private int ticks = 0;
    private boolean timerEnable = false;
    boolean spoofground = false;
    public static int groundstate = 0;// 0=nothing 1=noground 2=ground

    public NumberSetting vanillaMotionY, matrixticks;

    public Spider() {
        super("Spider", "Лазать по стенам", Type.Movement);
        Mode = new ModeSetting("Mode", "Matrix", () -> true, "Matrix", "IntaveBlock", "Vanilla");

        NumberSetting vanillaMotionY = new NumberSetting("Vanilla Motion", 0.42F, 0.0001F, 9.99999F, 0.01F,
                () -> Mode.currentMode.equals("Vanilla"));

        NumberSetting matrixticks = new NumberSetting("Matrix Ticks", 3, 1, 10, 1,
                () -> Mode.currentMode.equals("MatrixLatest"));

        addSettings(Mode, matrixticks, vanillaMotionY);

    }

    @Override
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        ticks = 0;
        timerEnable = false;

        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                if (pc.prevslotid != mc.player.getInventory().getSelectedSlot()) {
                    pc.sendPacket(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                }
                break;
        }

        super.onDisable();
    }

    @Override
    public void onEnable() {
        groundstate = 0;
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):

                if (InventoryUtil.getblockhotbarslot() == -1) {

                    ChatHelper.addTranslatedMessage("hotbar.need_blocks");
                    NotificationManager.publicity("Error",
                            ChatFormatting.RED + TranslationManager.get("hotbar.need_blocks"), 4,
                            NotificationType.ERROR);

                    Client.instance.featureManager.getModuleByClass(Spider.class).toggle();
                    return;

                }

                break;
            case ("IntaveBlock"):

                if (InventoryUtil.getblockhotbarslot() == -1) {

                    ChatHelper.addTranslatedMessage("hotbar.need_blocks");
                    NotificationManager.publicity("Error",
                            ChatFormatting.RED + TranslationManager.get("hotbar.need_blocks"), 4,
                            NotificationType.ERROR);

                    Client.instance.featureManager.getModuleByClass(Spider.class).toggle();
                    return;

                }

                break;
        }

        TimerUtil.setTimerspeed(1);
        spoofground = false;

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
        String mode = Mode.getCurrentMode();

    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre e) {
        PacketHelper.Values pc = Client.instance.packet;

        String mode = Mode.getCurrentMode();

    }

    @EventTarget
    public void Move(EventOnMovePost eventMove) {
        String mode = Mode.getCurrentMode();
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
                if (mc.player.horizontalCollision && !mc.player.onClimbable()) {
                    MoveUtil.setmotY(vanillaMotionY.getValue());
                }
                break;

        }

    }

    @EventTarget
    public void onTick(EventTick e) {
        String mode = Mode.getCurrentMode();

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
        String mode = Mode.getCurrentMode();

        switch (mode) {
            case ("Matrix"):
                if (!mc.player.horizontalCollision || mc.player.onClimbable()) {
                    index1 = 0;
                    if (pc.prevslotid != mc.player.getInventory().getSelectedSlot()) {
                        pc.sendPacket(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                    }
                    return;
                }

                Direction npd = Direction.UP;
                BlockPos blockPos = BlockPos.ZERO;
                BlockPos bpp = new BlockPos(mc.player.getBlockX(), mc.player.getBlockY() - 1, mc.player.getBlockZ());

                if (!mc.level.getBlockState(bpp.below()).isAir()) {
                    blockPos = bpp.below();
                } else if (!mc.level.getBlockState(bpp.south()).isAir()) {
                    blockPos = bpp.south();
                    npd = Direction.NORTH;
                } else if (!mc.level.getBlockState(bpp.north()).isAir()) {
                    blockPos = bpp.north();
                    npd = Direction.SOUTH;
                } else if (!mc.level.getBlockState(bpp.east()).isAir()) {
                    blockPos = bpp.east();
                    npd = Direction.WEST;
                } else if (!mc.level.getBlockState(bpp.west()).isAir()) {
                    blockPos = bpp.west();
                    npd = Direction.EAST;
                }
                if (blockPos.equals(BlockPos.ZERO)) {
                    index1 = 0;
                    if (pc.prevslotid != mc.player.getInventory().getSelectedSlot()) {
                        pc.sendPacket(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                    }
                    return;
                }

                int slott = mc.player.getInventory().getSelectedSlot();
                if (index1 % matrixticks.getValue() == 0) {

                    groundstate = 2;

                    mc.player.jumpFromGround();

                    e.setOnGround(true);
                }
                boolean selected = mc.player.getInventory().getSelectedItem().getItem() instanceof BlockItem;
                if (!selected && InventoryUtil.getblockhotbarslot() != -1
                        && pc.prevslotid != InventoryUtil.getblockhotbarslot()) {
                    pc.sendPacket(new ServerboundSetCarriedItemPacket(InventoryUtil.getblockhotbarslot()));
                }

                if (!ACUtil.ismatrixonground()) {
                    HitResult hr = TraceUtil.ClientpickNew(12, 1, false,
                            npd.equals(Direction.UP) ? mc.player.getYRot() : npd.toYRot(),
                            npd.equals(Direction.UP) ? 90 : mc.player.getXRot());
                    BlockHitResult bhr = (BlockHitResult) hr;
                    bhr = bhr.withPosition(blockPos).withDirection(npd);
                    // mc.player.swing(InteractionHand.MAIN_HAND);

                    pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,
                            bhr, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
                }

                index1++;

                break;

        }

    }

    @EventTarget
    public void onEMI(EventMoveInput e) {
        String mode = Mode.getCurrentMode();

    }

    @EventTarget
    public void onSendPacket(EventSendPacketCancel e) {
        if (e.isCancelled())
            return;
        String mode = Mode.getCurrentMode();
        Packet<?> packetp = e.getPacket();

    }

}
