package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;


public class AutoGapple extends Module {

    public static NumberSetting health;
    private ModeSetting Mode = new ModeSetting("Mode", "MatrixDuels",()->true,  "MatrixDuels","Default"  );
    private ModeSetting GroundCondition= new ModeSetting("Ground Condition", "Always", () -> Mode.getCurrentMode().equals("MatrixDuels"), "Only Ground", "Only Air", "Always");

    private boolean isActive;

    int index1,index2,newslot=0,oldslot=0;

    public AutoGapple() {
        super("AutoGApple", "Автоматически ест яблоко при опредленном здоровье", Type.Combat);
        health = new NumberSetting("Health Amount", 15, 1, 20, 1, () -> true);
        addSettings(Mode,health,GroundCondition);
    }
    @Override
    public void onEnable() {index1=0;
        index2=0; isActive=false;newslot=0;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
    @EventTarget
    public void onpem(EventPreMotion e) {
        PacketHelper.Values pc= Client.instance.packet;
        if (Mode.getCurrentMode().equals("Default")){
            this.setSuffix("" + (int) health.getValue());
            if (mc.player == null)
                return;
            if (isGoldenApple(mc.player.getOffhandItem()) && mc.player.getHealth() <= health.getValue()) {
                isActive = true;
                mc.options.keyUse.setDown(true);
            } else if (isActive) {
                mc.options.keyUse.setDown(true);
                isActive = false;
            }
        }else if (Mode.getCurrentMode().equals("MatrixDuels")){
            if(index1==10) {
                pc.sendPacket(new ServerboundSetCarriedItemPacket (newslot));
            }
            if(index1==9) {
                pc.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND,mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(),pc.LastYaw,pc.LastPitch));

                for(int j=0;j<35;j++) {
                    ACUtil.send117DuplPacket();
                }

            }
            if(index1==8) {
                pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
                pc.sendPacket(new ServerboundSetCarriedItemPacket(oldslot));
            }

        }


    }

    @EventTarget
    public void onpom(EventPostMotion e) {

        PacketHelper.Values pc=Client.instance.packet;
        if (Mode.getCurrentMode().equals("MatrixDuels")){
            boolean boosted=false;
            if (isCondition(pc)) {
                if (mc.player.getHealth() <= health.getValue() && index1 <= 6 && (pc.airpackets<20)) {

                    for (int i = 0; i < 9; i++) {

                        if (isGoldenApple(mc.player.getSlot(i).get())) {
                            oldslot = mc.player.getInventory().getSelectedSlot();
                            newslot = i;
                            boosted = true;
                            index1 = 11;
                            break;
                        }

                    }
                }
            }
            if(index1>0) {
                index1--;
            }
        }

    }




    private boolean isGoldenApple(ItemStack itemStack) {
        return (itemStack != null && !itemStack.isEmpty() && itemStack.getItem().toString().contains("gold") && itemStack.getItem().toString().contains("apple"));
    }

    private boolean isCondition(PacketHelper.Values pc) {
        if(GroundCondition.getCurrentMode().equals("Only Ground") && !pc.LastGround) { 	return false; 	}
        else if(GroundCondition.getCurrentMode().equals("Only Air") && pc.LastGround) { 	return false; 	}
        return true;
    }

}
