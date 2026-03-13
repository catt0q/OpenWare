package base.client.helpers.impl.inventory;

import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;

import java.util.ArrayList;

import static base.client.helpers.Helper.mc;

public class InventoryUtil {
    private ArrayList<Integer> getfreehotbarslots() {
        ArrayList<Integer> list=new ArrayList<Integer>();
        for(int i=0;i<9;i++) {
            if(mc.player.getInventory().getItem(i).getItem() instanceof AirItem){
                list.add(i);
            }
        }
        return list;
    }

    public static int getfirstfreehotbarslot() {
        for(int i=0;i<9;i++) {
            if(mc.player.getInventory().getItem(i).getItem() instanceof AirItem){
                return i;
            }
        }
        return -1;
    }
    public static int getblockhotbarslot() {
        for(int i=0;i<9;i++) {
            if(mc.player.getInventory().getItem(i).getItem() instanceof BlockItem ){
                return i;
            }
        }
        return -1;
    }

    public static int upgetfirstfreeinventoryslot(ItemStack is) {
        for(int i=mc.player.containerMenu.getItems().size()-1;i>=mc.player.containerMenu.getItems().size()-36;i--) {
            ItemStack ins=mc.player.containerMenu.getSlot(i).getItem();

            if(Item.getId(ins.getItem())==Item.getId(is.getItem()) && ins.getMaxStackSize()-ins.getCount()>=is.getCount()){
                return i;
            }
        }
        for(int i=mc.player.containerMenu.getItems().size()-36;i<mc.player.containerMenu.getItems().size();i++) {
            if(mc.player.containerMenu.getSlot(i).getItem().getItem() instanceof AirItem){
                return i;
            }
        }
        return -1;
    }
    public static int downgetfirstfreeinventoryslot(ItemStack is) {
        for(int i=mc.player.containerMenu.getItems().size()-1;i>=mc.player.containerMenu.getItems().size()-36;i--) {
            ItemStack ins=mc.player.containerMenu.getSlot(i).getItem();
            if(Item.getId(ins.getItem())==Item.getId(is.getItem()) && ins.getMaxStackSize()-ins.getCount()>=is.getCount()){
                return i;
            }
        }

        for(int i=mc.player.containerMenu.getItems().size()-1;i>=mc.player.containerMenu.getItems().size()-36;i--) {
            if(mc.player.containerMenu.getSlot(i).getItem().getItem() instanceof AirItem){
                return i;
            }
        }


        return -1;
    }


}
