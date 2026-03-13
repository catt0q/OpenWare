package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.TeleportBack;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.AirItem;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class InvMove extends Module {

    boolean ismoving = false;

    private Queue<Packet> clickPackets = new LinkedList<>();
    private Queue<ServerboundContainerClickPacket> C0Es = new LinkedList<>();
    private Deque<KeyMapping> keys = new LinkedList<>();
    public static BooleanSetting OnlyInv = new BooleanSetting("Only none Inventory", false, () -> true);

    public static BooleanSetting Sprint = new BooleanSetting("Allow Sprint", true, () -> true);
    public static BooleanSetting Sneak = new BooleanSetting("Allow Sneak", true, () -> true);
    public static BooleanSetting Jump = new BooleanSetting("Allow Jump", true, () -> true);

    public ModeSetting mode = new ModeSetting("Mode", "Vanilla", () -> true, "PredictionAC", "Matrix", "Vanilla");

    public InvMove() {
        super("InvMove", "позволяет двигатсья с открытым инвенторем", Type.Movement);
        this.addSettings(mode, Sprint, OnlyInv);
    }

    @Override
    public void onEnable() {
        C0Es.clear();
        ismoving = false;
        clickPackets.clear();

        super.onEnable();
    }

    @Override
    public void onDisable() {

        sendc0es();
        super.onDisable();
    }

    @EventTarget
    public void onEMI(EventMoveInput e) {
        if (mode.getCurrentMode().equals("Matrix")) {

            if (C0Es.size() > 0) {
                e.jump = true;
            }
        }

        ismoving = (e.backward || e.forward || e.left || e.right || e.jump);

    }

    @EventTarget
    public void omp(EventTick e) {

        if (!ismovegui()) {
            return;
        }
        keys.add(mc.options.keyUp);
        keys.add(mc.options.keyDown);
        keys.add(mc.options.keyLeft);
        keys.add(mc.options.keyRight);

        if (Jump.isEnabled()) {
            keys.add(mc.options.keyJump);
        }
        if (Sneak.isEnabled()) {
            keys.add(mc.options.keyShift);
        }
        if (Sprint.isEnabled()) {
            keys.add(mc.options.keySprint);
        }

        switch (mode.getCurrentMode()) {
            case ("Grim"):

                if (mc.player.containerMenu.getCarried().getItem() instanceof AirItem) {
                    for (KeyMapping k : keys)
                        k.setDown(InputConstants.isKeyDown(mc.getWindow(),
                                InputConstants.getKey(k.saveString()).getValue()));
                } else {
                    for (KeyMapping key : keys) {
                        key.setDown(false);
                        mc.player.setSprinting(false);
                    }
                }

                break;
            case ("PredictionAC"):
                for (KeyMapping k : new KeyMapping[] { mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft,
                        mc.options.keyRight, mc.options.keyJump, mc.options.keySprint, mc.options.keyShift })
                    k.setDown(InputConstants.isKeyDown(mc.getWindow(),
                            InputConstants.getKey(k.saveString()).getValue()));

                break;
            case ("Matrix"):
                for (KeyMapping k : new KeyMapping[] { mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft,
                        mc.options.keyRight, mc.options.keyJump, mc.options.keySprint, mc.options.keyShift })
                    k.setDown(InputConstants.isKeyDown(mc.getWindow(),
                            InputConstants.getKey(k.saveString()).getValue()));

                break;
            case ("Vanilla"):
                for (KeyMapping k : new KeyMapping[] { mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft,
                        mc.options.keyRight, mc.options.keyJump, mc.options.keySprint, mc.options.keyShift })
                    k.setDown(InputConstants.isKeyDown(mc.getWindow(),
                            InputConstants.getKey(k.saveString()).getValue()));

                break;
        }
    }

    @EventTarget
    public void ops(EventSendPacketPost e) {
        if (!ismovegui()) {
            return;
        }
        switch (mode.getCurrentMode()) {
            case ("Grim"):
                if (mc.screen != null && mc.player.containerMenu.getCarried().getItem() instanceof AirItem
                        && e.getPacket() instanceof ServerboundContainerClickPacket p
                        && p.clickType() != ClickType.PICKUP && p.clickType() != ClickType.PICKUP_ALL) {
                    pc.sendCloseInventoryPacket();
                }
                break;
            case ("Matrix"):
                if (e.getPacket() instanceof ServerboundMovePlayerPacket) {
                    ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) e.getPacket();
                    if (!c03.onGround) {
                        sendc0es();
                    }
                }
                break;
        }
    }

    @EventTarget
    public void osp(EventSendPacketCancel e) {
        Packet packetp = e.getPacket();
        if (!ismovegui()) {
            return;
        }
        switch (mode.getCurrentMode()) {
            case ("PredictionAC"):
                if (e.getPacket() instanceof ServerboundContainerClickPacket csp) {
                    if (ismoving) {
                        C0Es.add(csp);
                        e.cancel();
                    }

                }

                if (packetp instanceof ServerboundMovePlayerPacket) {
                    if (!ismoving) {
                        sendc0es();
                    }

                }

                break;
            case ("Matrix"):

                if (packetp instanceof ServerboundContainerClickPacket) {

                    if (!e.isCancelled()) {

                        ServerboundContainerClickPacket c0e = (ServerboundContainerClickPacket) packetp;
                        if (mc.player.onGround() && mc.player.verticalCollision && MoveUtil.ismovingmfms()
                                && !Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState()) {
                            C0Es.add(c0e);
                            e.setCancelled(true);

                        }

                    }
                } else if (packetp instanceof ServerboundContainerButtonClickPacket) {
                    // ChatHelper.addChatMessage("yes");
                }

                break;

        }
    }

    boolean ismovegui() {
        if (mc.screen instanceof ChatScreen || mc.screen instanceof SignEditScreen || mc.screen == null)
            return false;
        if (OnlyInv.isEnabled() && (mc.screen instanceof ContainerScreen))
            return false;

        return true;
    }

    void sendc0es() {
        PacketHelper.Values pc = Client.instance.packet;
        for (ServerboundContainerClickPacket pa : C0Es) {
            pc.sendPacket(pa, 10, true);
        }
        C0Es.clear();

    }

}
