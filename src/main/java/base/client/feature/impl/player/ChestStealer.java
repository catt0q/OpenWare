package base.client.feature.impl.player;

import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventRunGameLoop;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.inventory.InventoryUtil;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;

public class ChestStealer extends Module {
    private final NumberSetting delay;
    private final NumberSetting startdelay;
    public TimerHelper timer = new TimerHelper();
    public TimerHelper conttimer= new TimerHelper();
    boolean wascontopened=false;
    public static BooleanSetting AutoClose = new BooleanSetting("AutoClose", true,() -> true);
    public static BooleanSetting IgnoreTrash = new BooleanSetting("IgnoreTrash", true,() -> true);
    ModeSetting ClickMode = new ModeSetting("Click Mode", "QuickMove", () -> true, "QuickMove", "DoubleClick", "Throw");

    public ChestStealer() {
        super("ChestStealer", "Автоматически забирает вещи из сундуков", Type.Player);
        startdelay = new NumberSetting("Start Delay", 0, 0,500, 1, () -> true);
        delay = new NumberSetting("Delay", 10, 0, 300, 1, () -> true);
        addSettings(ClickMode,startdelay,delay, IgnoreTrash,AutoClose);
    }


    @Override
    public void onEnable() {
        wascontopened=false; conttimer.reset();
        super.onEnable();

    }

    @Override
    public void onDisable() {

        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventRunGameLoop event) {
        this.setSuffix("" + (int) delay.getValue());

        float delay = this.delay.getValue();

        if (mc.player.containerMenu instanceof ChestMenu) {
            if(!wascontopened) {
                conttimer.reset();
                wascontopened=true;
            }
            if(!conttimer.hasTimeElapsed((long) startdelay.getValue(), false)) return;


            ChestMenu container = (ChestMenu) mc.player.containerMenu;
            if (isEmpty(container) && AutoClose.isEnabled()) {  mc.player.closeContainer();      }
            boolean wasclick=false;
            for (int index = 0; index < container.getContainer().getContainerSize(); ++index) {
                if ((IgnoreTrash.isEnabled() ? (isWhiteItem(container.getSlot(index).getItem())) : !(container.getContainer().getItem(index).getItem() instanceof AirItem)) && timer.hasReached((delay))) {
                    switch (ClickMode.getCurrentMode()){
                        case ("QuickMove"):
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, index,0, ClickType.QUICK_MOVE, mc.player);
     break;
                        case ("DoubleClick"):
                            ItemStack is=container.getContainer().getItem(index).copy();
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, index,0, ClickType.PICKUP, mc.player);
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, InventoryUtil.downgetfirstfreeinventoryslot(is),0, ClickType.PICKUP, mc.player);
                            break;
                        case ("Throw"):
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, index,0, ClickType.PICKUP, mc.player);
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, index,0, ClickType.THROW, mc.player);

                             break;
                    }



                    timer.reset();  continue;
                }



            }


        }else {
            wascontopened=false;
        }

    }

    public boolean isWhiteItem(ItemStack itemStack) {

        return ((itemStack.get(DataComponents.TOOL)!=null || itemStack.getItem() instanceof EnderpearlItem || itemStack.is(ItemTags.SWORDS)) || (itemStack.get(DataComponents.EQUIPPABLE)!=null && itemStack.get(DataComponents.EQUIPPABLE).canBeEquippedBy(EntityType.PLAYER)) || (itemStack.get(DataComponents.FOOD) != null) || itemStack.getItem() instanceof PotionItem || itemStack.getItem() instanceof BlockItem || itemStack.getItem() instanceof ArrowItem || itemStack.getItem() instanceof CompassItem);
    }

    private boolean isEmpty(ChestMenu container) {
        for (int index = 0; index < container.getContainer().getContainerSize(); index++) {


            if (IgnoreTrash.isEnabled()) {
                if(container.getSlot(index).getItem()!=null && isWhiteItem(container.getSlot(index).getItem())) {
                  return false;
                }
            }else if (!(container.getSlot(index).getItem().getItem() instanceof AirItem)) {    	// ChatHelper.addChatMessage(index+" "+container.getSlot(index).getStack().getItem().getUnlocalizedName());
                return false;
            }
        }
        return true;
    }
}
