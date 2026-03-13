package base.client.feature.impl.combat;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventAttackEntity;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

public class AttributeSwap extends Module {

    private BooleanSetting shieldBreaker = new BooleanSetting("Shield Breaker", false, () -> true);
    private BooleanSetting noSwap = new BooleanSetting("No Swap After Break", false, () -> shieldBreaker.isEnabled());
    private NumberSetting targetSlot = new NumberSetting("Target Slot", 1, 1, 9, 1,
            () -> !(noSwap.isEnabled() && shieldBreaker.isEnabled()));
    private BooleanSetting swapBack = new BooleanSetting("Swap Back", true,
            () -> !(noSwap.isEnabled() && shieldBreaker.isEnabled()));
    private NumberSetting swapDelay = new NumberSetting("Swap Back Delay", 1, 1, 20, 1,
            () -> swapBack.isEnabled() && !(noSwap.isEnabled() && shieldBreaker.isEnabled()));

    private int prevSlot = -1;
    private int delayTicks = 0;

    public AttributeSwap() {
        super("AttributeSwap", "Swaps attributes of main hand item with target slot on attack", Type.Combat);
        addSettings(shieldBreaker, noSwap, targetSlot, swapBack, swapDelay);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        prevSlot = -1;
        delayTicks = 0;
    }

    @EventTarget
    public void onAttack(EventAttackEntity event) {
        if (mc.player == null || mc.level == null)
            return;

        // save previous slot if swap back enabled
        if (swapBack.isEnabled()) {
            prevSlot = mc.player.getInventory().getSelectedSlot();
        }

        // determine swap slot
        if (shieldBreaker.isEnabled()) {
            if (event.getTarget() instanceof Player player && player.isBlocking()) {
                // find axe in hotbar
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.player.getInventory().getItem(i);
                    if (stack.getItem() instanceof AxeItem) {
                        swap(i);
                        break;
                    }
                }
            } else if (!noSwap.isEnabled()) {
                swap((int) targetSlot.getValue() - 1);
            }
        } else {
            swap((int) targetSlot.getValue() - 1);
        }

        // set delay for swap back
        if (swapBack.isEnabled() && prevSlot != -1) {
            delayTicks = (int) swapDelay.getValue();
        }
    }

    private void swap(int slot) {
        if (slot >= 0 && slot < 9 && slot != mc.player.getInventory().getSelectedSlot()) {
            mc.player.getInventory().setSelectedSlot(slot);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    @EventTarget
    public void onPreMotion(EventPreMotion event) {
        // handle swap back countdown
        if (delayTicks > 0) {
            delayTicks--;
            if (delayTicks == 0 && prevSlot != -1) {
                mc.player.getInventory().setSelectedSlot(prevSlot);
                mc.player.connection.send(new ServerboundSetCarriedItemPacket(prevSlot));
                prevSlot = -1;
            }
        }
    }
}
