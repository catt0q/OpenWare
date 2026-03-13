package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FastEXPComponent extends AnarchyHelperComponent {
    public FastEXPComponent() {
        super("Fast EXP", "Experience bottle", Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public int executeItem() {
        int slot = -1488;
        ItemStack bottleStack = null;

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = mc.player.getInventory().getItem(i);

            if (itemStack.getItem() == getRenderingItem()) {
                slot = i;
                bottleStack = itemStack;
                break;
            }
        }

        if (slot != -1488) {
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP,mc.player);
            mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
            for (int i = 0; i < bottleStack.getCount(); i++) {
                mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), mc.player.getYRot(), mc.player.getXRot()));
                mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }

        return slot;
    }
}
