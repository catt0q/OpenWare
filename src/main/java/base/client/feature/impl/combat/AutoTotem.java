package base.client.feature.impl.combat;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {

    private ModeSetting mode = new ModeSetting("Mode", "Smart", () -> true, "Smart", "Strict");
    private NumberSetting delay = new NumberSetting("Delay", 0, 0, 20, 1, () -> true);
    private NumberSetting health = new NumberSetting("Health", 10, 0, 36, 1,
            () -> mode.getCurrentMode().equals("Smart"));
    private BooleanSetting elytra = new BooleanSetting("Elytra", true, () -> mode.getCurrentMode().equals("Smart"));
    private BooleanSetting fall = new BooleanSetting("Fall", true, () -> mode.getCurrentMode().equals("Smart"));

    public boolean locked;
    private int totems, ticks;

    public AutoTotem() {
        super("AutoTotem", "Automatically equips a totem in your offhand", Type.Combat);
        addSettings(mode, delay, health, elytra, fall);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        locked = false;
        totems = 0;
        ticks = 0;
    }

    @EventTarget
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.level == null)
            return;

        // count totems in inventory
        totems = 0;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                totems++;
            }
        }
        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            totems++;
        }

        this.setSuffix(String.valueOf(totems));

        if (totems <= 0) {
            locked = false;
        } else if (ticks >= delay.getValue()) {
            boolean low = mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.getValue();
            boolean ely = elytra.isEnabled() &&
                    mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) &&
                    mc.player.isFallFlying();
            boolean falling = fall.isEnabled() && mc.player.fallDistance > 3 &&
                    mc.player.getHealth() <= mc.player.fallDistance;

            locked = mode.getCurrentMode().equals("Strict") ||
                    (mode.getCurrentMode().equals("Smart") && (low || ely || falling));

            if (locked && !mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                int totemSlot = findTotemSlot();
                if (totemSlot != -1) {
                    swapToOffhand(totemSlot);
                }
            }

            ticks = 0;
            return;
        }

        ticks++;
    }

    @EventTarget
    public void onPacket(EventReceivePacketPre event) {
        if (!(event.getPacket() instanceof ClientboundEntityEventPacket packet))
            return;
        if (packet.getEventId() != 35)
            return;

        if (packet.getEntity(mc.level) == mc.player) {
            ticks = 0;
        }
    }

    private int findTotemSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }
        return -1;
    }

    private void swapToOffhand(int slot) {
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                slot < 9 ? slot + 36 : slot,
                0,
                net.minecraft.world.inventory.ClickType.PICKUP,
                mc.player);
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                45,
                0,
                net.minecraft.world.inventory.ClickType.PICKUP,
                mc.player);
        if (!mc.player.containerMenu.getCarried().isEmpty()) {
            mc.gameMode.handleInventoryMouseClick(
                    mc.player.containerMenu.containerId,
                    slot < 9 ? slot + 36 : slot,
                    0,
                    net.minecraft.world.inventory.ClickType.PICKUP,
                    mc.player);
        }
    }

    public boolean isLocked() {
        return getState() && locked;
    }
}
