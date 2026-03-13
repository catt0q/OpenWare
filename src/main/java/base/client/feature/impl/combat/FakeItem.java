package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class FakeItem extends Module {


    public TimerHelper timerr;
    int index1;
    int index2;
    int index3;
    float yawf2;
    float pitchf2;
    public NumberSetting FirstItem;
    public NumberSetting SecondItem;

    public NumberSetting relodaticks;
    public NumberSetting relodams;
    public NumberSetting tickstochange;

    public ModeSetting SSMode;

    public FakeItem() {
        super("FakeItem", "Заменяет предмет в руке на нужный и обратно", Type.Combat);

        this.FirstItem = new NumberSetting("First Slot", 1, 0, 9, 1,() -> true);
        this.SSMode = new ModeSetting("Second Slot Mode", "Effective", () -> true, "Static", "Effective");


        this.SecondItem = new NumberSetting("Second Slot", 4, 1, 9, 1,() -> SSMode.getCurrentMode().equals(SSMode.getCurrentMode()));

        this.relodaticks = new NumberSetting("Reload Ticks", 0, 0, 60, 1,() -> true);
        this.relodams = new NumberSetting("Reload ms", 1000, 0, 1500, 50,() -> true);
        this.tickstochange = new NumberSetting("Change Time Ticks", 0, 0, 20, 1,() -> true);


        this.timerr = new TimerHelper();

        this.addSettings(FirstItem,SSMode,SecondItem, tickstochange,relodams );


    }
    @Override
    public void onEnable() {
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onPrePacket(EventSendPacketCancel event) {

        if(!event.isCancelled()) {
            Packet<?> packetp=event.getPacket();
            if (packetp instanceof ServerboundSetCarriedItemPacket) {
                if(index1<tickstochange.getValue() && index2!=0) {
                    event.setCancelled(true);
                }
            }
            if (packetp instanceof ServerboundInteractPacket) {

                ServerboundInteractPacket c02=(ServerboundInteractPacket) packetp;

                base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
                ServerboundInteractPacket.Action action = ac2.getAction();
                if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK) && timerr.hasTimeElapsed((long) relodams.getValue(), false) ){

                    if((mc.player.getInventory().getSelectedSlot()==FirstItem.getValue()-1 || FirstItem.getValue()==0) && isValid()) {
                        index1=0;
                            pc.sendPacket(new ServerboundSetCarriedItemPacket(getsecondslot()));

                        timerr.reset();

                        index2=1;



                    }
                }




            }



        }




    }



    @EventTarget
    public void onPostPacket(EventSendPacketPost event) {

        Packet<?> packetp=event.getPacket();
        if (packetp instanceof ServerboundInteractPacket) {
            PacketHelper.Values pc=Client.instance.packet;
            if(tickstochange.getValue()==0 && index2==1) {
                if(FirstItem.getValue()!=0) {
                    mc.player.getInventory().setSelectedSlot((int) (FirstItem.getValue()-1));

                    index3=(int) (FirstItem.getValue()-1);          } else {  index3=mc.player.getInventory().getSelectedSlot();   }

                pc.sendPacket(new ServerboundSetCarriedItemPacket(index3));
                index2=0;
                timerr.reset();
            }

        }



    }



    @EventTarget
    public void onPostPacket(EventPreMotion event) {

        if (isValid()) {
            PacketHelper.Values pc=Client.instance.packet;
            if(index1==tickstochange.getValue() && index2==1) {
                if(FirstItem.getValue()!=0) {
                    mc.player.getInventory().setSelectedSlot((int) (FirstItem.getValue()-1));
                    index3=(int) (FirstItem.getValue()-1);          } else {
                    index3=mc.player.getInventory().getSelectedSlot();   }

                pc.sendPacket(new ServerboundSetCarriedItemPacket(index3));
                index2=0;
                timerr.reset();
            }
            index1++;

        }

    }



    boolean isValid() {
        int slot=getsecondslot();
if((slot==-1)
|| (mc.player==null)
|| (mc.player.getInventory().getSelectedSlot()==slot)
){
return false;
}

        if(FirstItem.getValue()==0 && mc.player.getInventory().getSelectedSlot()!=slot) {   	return true;    	}
        if(FirstItem.getValue()!=(slot+1) && mc.player.getInventory().getItem(slot)!=null &&  !((mc.player.getInventory().getItem(slot)).getItem() instanceof AirItem)) {
            return true;
        }
   return false;
    }

    int getsecondslot(){

        if(SSMode.getCurrentMode().equals("Static")){
            return ((int)(SecondItem.getValue()-1));
        }else  if(SSMode.getCurrentMode().equals("Effective")){
            return (findKnockbackOrFireAspectInHotbar());
        }



return -1;
    }



    int findKnockbackOrFireAspectInHotbar() {
        ItemStack currentItem = mc.player.getInventory().getSelectedItem();
        boolean currentHasEnchant = hasKnockbackOrFireAspect(currentItem);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (currentHasEnchant && ItemStack.isSameItem(currentItem, stack)) {
                continue;
            }

            if (hasKnockbackOrFireAspect(stack)) {
                return i;
            }
        }
        return -1;
    }

   boolean hasKnockbackOrFireAspect(ItemStack stack) {
        if (stack.isEmpty()) return false;

        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) return false;

        for (var entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey().value();
            if (enchant.equals(Enchantments.KNOCKBACK) || enchant.equals(Enchantments.FIRE_ASPECT)) {
                return true;
            }
        }
        return false;
    }



}
