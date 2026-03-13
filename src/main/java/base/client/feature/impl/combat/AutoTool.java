package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventBlockBreaking;
import base.client.event.events.impl.packet.EventSendPacketModify;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class AutoTool extends Module {

    int bestdigitem=-1;

    int statechange=0;
    int statechanges=0;


    public ModeSetting DMode;
    public BooleanSetting InvCheck;
    public BooleanSetting StartBypass;
    public AutoTool() {
        super("AutoTool", "Автоматически берет лучший инструмент в руки при ломании блока", Type.Player);
        DMode = new ModeSetting("Mode", "Simple", () -> true, "Simple", "SilentV1");
        InvCheck = new BooleanSetting("Check Inventory",false,()-> DMode.getCurrentMode().equals("SilentV1"));
        StartBypass = new BooleanSetting("Start Bypass",false,()-> DMode.getCurrentMode().equals("SilentV1"));
        addSettings(DMode,InvCheck,StartBypass);
    }
    //также сделай моды для атаки, типо или доставать меч, или ничего не делать
    @Override
    public void onEnable() {statechanges=0;
        bestdigitem=-1; statechange=0; super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onBlockInteract(EventBlockBreaking event) {
        BlockPos blockPos = mc.gameMode.destroyBlockPos;
        Block block = mc.level.getBlockState(blockPos).getBlock();
        ItemStack current = mc.player.getInventory().getSelectedItem();
        float power = current.getDestroySpeed(block.defaultBlockState());
        int itemCount = -1;

        switch (DMode.getCurrentMode()) {
            case ("Simple"):

            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = mc.player.getInventory().getItem(i);

                if (itemStack.getDestroySpeed(block.defaultBlockState()) > power) {
                    power = itemStack.getDestroySpeed(block.defaultBlockState());
                    itemCount = i;
                }
            }
            if (itemCount != -1) {
                mc.player.getInventory().setSelectedSlot(itemCount);
            }
break;

            case ("SilentV1"):
  if(InvCheck.isEnabled()){
      int itemCount1 = -1;
      for (int i = 0; i < 9; i++) {
          ItemStack itemStack = mc.player.getInventory().getItem(i);
          if (itemStack.getDestroySpeed(block.defaultBlockState()) > power) {
              power = itemStack.getDestroySpeed(block.defaultBlockState());
              itemCount1 = i;
          }
      }
  for(int i=0;i<mc.player.containerMenu.slots.size();i++) {

          ItemStack itemStack = mc.player.containerMenu.getSlot(i).getItem();
          if (itemStack.getDestroySpeed(block.defaultBlockState()) > power) {
              power = itemStack.getDestroySpeed(block.defaultBlockState());
              itemCount = i;
   }
  }

      if (itemCount != -1) {
          statechange=2; bestdigitem=itemCount;
          mc.player.getInventory().setSelectedItem(mc.player.getInventory().getItem(bestdigitem));
          mc.gameMode.destroyProgress += (mc.level.getBlockState(blockPos).getDestroyProgress(mc.player, mc.player.level(), blockPos));
          mc.player.getInventory().setSelectedItem(current);
          mc.gameMode.destroyProgress -= (mc.level.getBlockState(blockPos).getDestroyProgress(mc.player, mc.player.level(), blockPos));
          //300iq compensetion
      }else if(itemCount1 != -1){
          statechange=1; bestdigitem=itemCount1;
          mc.player.getInventory().setSelectedItem(mc.player.getInventory().getItem(bestdigitem));
          mc.gameMode.destroyProgress += (mc.level.getBlockState(blockPos).getDestroyProgress(mc.player, mc.player.level(), blockPos));
          mc.player.getInventory().setSelectedItem(current);
          mc.gameMode.destroyProgress -= (mc.level.getBlockState(blockPos).getDestroyProgress(mc.player, mc.player.level(), blockPos));

      }


  }else{



       for (int i = 0; i < 9; i++) {
          ItemStack itemStack = mc.player.getInventory().getItem(i);
  if (itemStack.getDestroySpeed(block.defaultBlockState()) > power) {
              power = itemStack.getDestroySpeed(block.defaultBlockState());
              itemCount = i;
   }
  }



      if (itemCount!=-1 && itemCount != mc.player.getInventory().getSelectedSlot()) {
          statechange=1; bestdigitem=itemCount;
          mc.player.getInventory().setSelectedItem(mc.player.getInventory().getItem(bestdigitem));
          mc.gameMode.destroyProgress += (mc.level.getBlockState(blockPos).getDestroyProgress(mc.player, mc.player.level(), blockPos));
          mc.player.getInventory().setSelectedItem(current);
          mc.gameMode.destroyProgress -= (mc.level.getBlockState(blockPos).getDestroyProgress(mc.player, mc.player.level(), blockPos));
 //300iq compensetion
      }





  }








                break;




        }



    }

    @EventTarget
    public void oncancel(EventSendPacketModify e) {
        PacketHelper.Values pc= Client.instance.packet;
        Packet<?> packetp = e.getPacket();
        if(e.getPermissionidstate()<1) {
            if((e.getPacket() instanceof ServerboundPlayerActionPacket)) {
                ServerboundPlayerActionPacket c07 = (ServerboundPlayerActionPacket) packetp;

                if(DMode.getCurrentMode().equals("SilentV1")) {
                    if (c07.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                        if (statechange == 1) {
                            pc.sendPacket(new ServerboundSetCarriedItemPacket(bestdigitem));
                        } else if (statechange == 2) {
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, bestdigitem, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP, mc.player);
                            mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                        }

                    }

                   else if (c07.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                        if (StartBypass.isEnabled()){
////////////////////////////////////////////////////////////////
                            BlockPos blockPos = mc.gameMode.destroyBlockPos;
                            Block block = mc.level.getBlockState(blockPos).getBlock();
                            ItemStack current = mc.player.getInventory().getSelectedItem();
                            float power = current.getDestroySpeed(block.defaultBlockState());
                            int itemCount = -1;

                            if(InvCheck.isEnabled()){
                                int itemCount1 = -1;
                                for (int i = 0; i < 9; i++) {
                                    ItemStack itemStack = mc.player.getInventory().getItem(i);
                                    if (itemStack.getDestroySpeed(block.defaultBlockState()) > power) {
                                        power = itemStack.getDestroySpeed(block.defaultBlockState());
                                        itemCount1 = i;
                                    }
                                }
                                for(int i=0;i<mc.player.containerMenu.slots.size();i++) {

                                    ItemStack itemStack = mc.player.containerMenu.getSlot(i).getItem();
                                    if (itemStack.getDestroySpeed(block.defaultBlockState()) > power) {
                                        power = itemStack.getDestroySpeed(block.defaultBlockState());
                                        itemCount = i;
                                    }
                                }

                                if (itemCount != -1) {
                                    statechanges=2; bestdigitem=itemCount;
                                    mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, bestdigitem, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP, mc.player);
                                    mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                                }else if(itemCount1 != -1){
                                    statechanges=1; bestdigitem=itemCount1;
                                    pc.sendPacket(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                                }


                            }else{



                                for (int i = 0; i < 9; i++) {
                                    ItemStack itemStack = mc.player.getInventory().getItem(i);
                                    if (itemStack.getDestroySpeed(block.defaultBlockState()) > power) {
                                        power = itemStack.getDestroySpeed(block.defaultBlockState());
                                        itemCount = i;
                                    }
                                }



                                if (itemCount!=-1 && itemCount != mc.player.getInventory().getSelectedSlot()) {
                                    statechanges=1; bestdigitem=itemCount;
                                    pc.sendPacket(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                                }
    }
                            ////////////////////////////

                        }


                    }
                }

            }
        }
    }
    @EventTarget
    public void onpost(EventSendPacketPost e) {
        PacketHelper.Values pc= Client.instance.packet;
        Packet<?> packetp = e.getPacket();

            if((e.getPacket() instanceof ServerboundPlayerActionPacket)) {
                ServerboundPlayerActionPacket c07 = (ServerboundPlayerActionPacket) packetp;
                if(DMode.getCurrentMode().equals("SilentV1")) {
                    if (c07.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                        if (statechange == 1) {
                            pc.sendPacket(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                            statechange = 0;
                        } else if (statechange == 2) {
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, bestdigitem, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP, mc.player);
                            mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                            statechange = 0;
                        }
                    }         else if (c07.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                        if (StartBypass.isEnabled()){

                            if (statechanges == 1) {
                                pc.sendPacket(new ServerboundSetCarriedItemPacket(bestdigitem));
                                statechanges = 0;
                            } else if (statechanges == 2) {
                                mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, bestdigitem, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP, mc.player);
                                mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                                statechanges = 0;
                            }


                        }


                    }


                }


            }






    }




}
