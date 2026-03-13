package base.client.feature.impl.combat.anarchyhelper;

import base.client.helpers.Helper;
import lombok.Getter;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Getter
public abstract class AnarchyHelperComponent implements Helper {

    private final String name, itemName;
    private final Item renderingItem;

    public AnarchyHelperComponent(String name, String itemName, Item renderingItem) {
        this.name = name;
        this.itemName = itemName;
        this.renderingItem = renderingItem;
    }

    public abstract int executeItem();

    protected int simpleUse(Item item, String itemName) {
        int slot = -1488;

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = mc.player.getInventory().getItem(i);

            if (itemStack.getItem() == item && itemStack.getItemName().getString().contains(itemName)) {
                slot = i;
                break;
            }
        }

        if (slot != -1488) {
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP,mc.player);
            mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
            mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), mc.player.getYRot(), mc.player.getXRot()));
        }

        return slot;
    }
}
