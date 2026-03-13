package base.client.feature.impl.player;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.items.EnchantmentUtil;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AutoArmor extends Module {

    public static BooleanSetting openInventory;
    private final NumberSetting delay;
    public TimerHelper timerUtils = new TimerHelper();

    public AutoArmor() {
        super("AutoArmor", "Автоматически одевает лучшую броню находящиеся в инвентаре", Type.Combat);
        delay = new NumberSetting("Equip Delay", 1, 0, 10, 1, () -> true);
        openInventory = new BooleanSetting("Open Inventory", true, () -> true);
        addSettings(delay, openInventory);
    }

    public static boolean isNullOrEmpty(ItemStack stack) {
        return !(stack != null && !stack.isEmpty());
    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {

        this.setSuffix("" + delay.getValue());

        if (!(mc.screen instanceof InventoryScreen) && (openInventory.isEnabled()))
            return;

        if (mc.screen instanceof ContainerScreen) {
            return;
        }

        AbstractContainerMenu inventory = mc.player.containerMenu;

        int[] bestArmorSlots = new int[4];
        int[] bestArmorValues = new int[4];
int qi=0;

        for (int type = 0; type < 4; type++) {
            bestArmorValues[type] = -1;
            bestArmorSlots[type] = -1;
            ItemStack stack = EntityUtil.armorItemInSlot(type);
            if (!isNullOrEmpty(stack) && !(stack.getItem() instanceof AirItem)) {

                Item item =  stack.getItem();
                bestArmorValues[type] = getArmorValue(item, stack);
            }
        }

        for (int slot = 0; slot < 45; slot++) {

            ItemStack stack = inventory.getSlot(slot).getItem();

            if (!stack.isEmpty() && !isNullOrEmpty(stack) && stack.get(DataComponents.EQUIPPABLE)!=null && stack.get(DataComponents.EQUIPPABLE).canBeEquippedBy(EntityType.PLAYER) && stack.get(DataComponents.EQUIPPABLE).slot().isArmor()) {

                Item item = stack.getItem();
                int armorType = stack.get(DataComponents.EQUIPPABLE).slot().getIndex();
                int armorValue = getArmorValue(item, stack);
             //   ChatHelper.addChatMessage(armorType+" "+armorValue+" "+bestArmorValues[armorType]);

                if (armorValue > bestArmorValues[armorType]) {
               //     ChatHelper.addChatMessage(armorValue+" "+bestArmorValues[armorType]);

                    bestArmorSlots[armorType] = slot;
                    bestArmorValues[armorType] = armorValue;
                }
            }
        }
        ArrayList<Integer> types = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(types);
         //ChatHelper.addChatMessage(""+bestArmorSlots[0]+" "+bestArmorSlots[1]+" "+bestArmorSlots[2]+" "+bestArmorSlots[3]);

        for (int i : types) {
            int j = bestArmorSlots[i];
            if (j == -1) {
                continue;
            }
            ItemStack oldArmor = EntityUtil.armorItemInSlot(i);
            if (!isNullOrEmpty(oldArmor) && EntityUtil.getFirstEmptyStack() == -1) {
                continue;
            }
            if (j < 9) {
                j += 36;
            }
            if (timerUtils.hasReached(delay.getValue() * 100)) {
                if (!isNullOrEmpty(oldArmor)) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 8-i,0, ClickType.QUICK_MOVE, mc.player);
   }
                mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, j,0, ClickType.QUICK_MOVE, mc.player);
                timerUtils.reset();
            }
            break;
        }

    }

    private int getArmorValue(Item item, ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        double armorPoints=0,armorToughness=0;
        for(ItemAttributeModifiers.Entry modi : modifiers.modifiers()){
            if(modi.attribute().is(Attributes.ARMOR)){
              armorPoints=modi.modifier().amount();
            }else if(modi.attribute().is(Attributes.ARMOR_TOUGHNESS)){
                  armorToughness=modi.modifier().amount();
            }
      }


        return (int) (armorPoints * 5 + EnchantmentUtil.getArmorEnchantmentScore(stack) * 3 + armorToughness);

    }

}
