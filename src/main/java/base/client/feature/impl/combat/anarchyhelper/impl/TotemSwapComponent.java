package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TotemSwapComponent extends AnarchyHelperComponent {

    public TotemSwapComponent() {
        super("TotemSwap", "Талисман Крушителя", Items.TOTEM_OF_UNDYING);
    }

    @Override
    public int executeItem() {
        int krushSlot = -1488;

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
             ItemStack itemStack = mc.player.getInventory().getItem(i);

            if (itemStack.getItem() == getRenderingItem() && itemStack.getItemName().toString().contains(getItemName())) {
                krushSlot = i;
                break;
            }
        }

        if (krushSlot == -1488) {
            return krushSlot;
        }

        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, krushSlot, Inventory.SLOT_OFFHAND, ClickType.SWAP, mc.player);
        mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
        mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), mc.player.getYRot(), mc.player.getXRot()));

        return -1337;
    }
}
