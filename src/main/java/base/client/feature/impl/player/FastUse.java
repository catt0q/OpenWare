package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventRunGameLoop;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.*;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

public class FastUse extends Module {

    TimerHelper timerr=new TimerHelper();

    private ModeSetting Mode= new ModeSetting("Mode", "DuplPacket2", () -> true, "DuplPacket","Polar","DuplPacket2", "Vanilla", "AAC", "NCP", "CEPacket");
    private NumberSetting PacketCount=new NumberSetting("Packet Count" ,5, 0, 40, 1,()->(!Mode.getCurrentMode().equals("NCP") && !Mode.getCurrentMode().equals("AAC") && !Mode.getCurrentMode().equals("Polar")));
    private NumberSetting MinUseDuration=new NumberSetting("MinUseDuration" ,0, 0, 35, 1,()->(!Mode.getCurrentMode().equals("NCP") && !Mode.getCurrentMode().equals("AAC") && !Mode.getCurrentMode().equals("Polar")));

    private ModeSetting GroundCondition= new ModeSetting("Ground Condition", "Always", () -> true, "Only Ground", "Only Air", "Always");
    private ModeSetting MoveCondition= new ModeSetting("Move Condition", "Always", () -> true, "Only Moving", "Only NoMove", "Always");
    public BooleanSetting AutoComplete = new BooleanSetting("AutoComplete", true, () -> true);
    public BooleanSetting Drinks = new BooleanSetting("Drinks", true, () -> true);
    public BooleanSetting Food = new BooleanSetting("Food", true, () -> true);

//CE = Client Eight
    public FastUse() {
        super("FastUse", "Позволяет быстро использовать еду", Type.Player);
        addSettings(Mode,PacketCount,MinUseDuration,AutoComplete,GroundCondition,MoveCondition,Drinks,Food);
    }
    @EventTarget
    public void onUpdatee(EventPreMotion e) {
        PacketHelper.Values pc = Client.instance.packet;
        if(mc.player.useItemRemaining<=0 && AutoComplete.isEnabled() && mc.player.isUsingItem()){
           pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN), 10,true);

            mc.player.stopUsingItem();
        }

    }



    @EventTarget
    public void onTad(EventRunGameLoop e) {
        PacketHelper.Values pc= Client.instance.packet;
        switch (Mode.getCurrentMode()) {

            case ("Polar"):



            if (timerr.hasTimeElapsed(44, true)) {
                if( !isCondition(pc)) return;
                float py=pc.LastYaw;float pp=pc.LastPitch;
                //pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ZERO, Direction.DOWN), 10,true);

                //    pc.sendPacket(new ServerboundSetCarriedItemPacket((mc.player.getInventory().getSelectedSlot()+1)%8));
             //   pc.sendPacket(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                ACUtil.send117DuplPacket((float) (pc.LastYaw + 0.0001 * Math.random()), (float) (pc.LastPitch + 0.0001 * Math.random()));
                ACUtil.send117DuplPacket(py, pp);
                    ACUtil.send117DuplPacket((float) (pc.LastYaw + 0.0001 * Math.random()), (float) (pc.LastPitch + 0.0001 * Math.random()));
                ACUtil.send117DuplPacket(py, pp);


                    mc.player.useItemRemaining -= 1;

            }
                break;

        }
    }

    @EventTarget
    public void onUpdate(EventPostMotion event) {
        String mode = Mode.getCurrentMode();
        this.setSuffix(mode);
        PacketHelper.Values pc= Client.instance.packet;



        int amount=Math.min(mc.player.useItemRemaining,(int)PacketCount.getValue());

        switch (mode){
            case ("NCP"):
                if(14<mc.player.getTicksUsingItem() &&
                        isCondition(pc)) {
                    for(int i=0;i<amount;i++) {
                        pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(pc.LastGround,mc.player.horizontalCollision),10);
                        mc.player.useItemRemaining-=1;
                    }

                    mc.player.stopUsingItem();
                }

                break;
            case ("AAC"):
                if(isCondition(pc)) {
             TimerUtil.setTimerspeed(1.21987F);
                }else if(TimerUtil.getTimerspeed()==1.21987F){
                    TimerUtil.reset();
                }

                break;

            case ("DuplPacket2"):
                if(MinUseDuration.getValue()>mc.player.getTicksUsingItem() ||
                        !isCondition(pc)) return;
                for(int i=0;i<amount;i++) {
                    ACUtil.send117DuplPacket((float) (pc.LastYaw+0.0001*Math.random()), (float) (pc.LastPitch+0.0001*Math.random()));
                    mc.player.useItemRemaining-=1;
                }
                break;
            case ("DuplPacket"):
                if(MinUseDuration.getValue()>mc.player.getTicksUsingItem() ||
                        !isCondition(pc)) return;
                for(int i=0;i<amount;i++) {
                    ACUtil.send117DuplPacket();
                    mc.player.useItemRemaining -= 1;
                }
                break;
            case ("Vanilla"):
                if(MinUseDuration.getValue()>mc.player.getTicksUsingItem() ||
                        !isCondition(pc)) return;
                for(int i=0;i<amount;i++) {
                    pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(pc.LastGround,mc.player.horizontalCollision),10);
                    mc.player.useItemRemaining-=1;
       }
                break;
            case ("CEPacket"):
                if(MinUseDuration.getValue()>mc.player.getTicksUsingItem() ||
                        !isCondition(pc)) return;
                for(int i=0;i<amount;i++) {
                    pc.sendPacket(
                            new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), (float) (pc.LastYaw+0.0001*Math.random()), (float) (pc.LastPitch+0.0001*Math.random())), 10);
                    mc.player.useItemRemaining-=1;pc.airpackets++;
                }
                break;

        }







    }

    private boolean isCondition(PacketHelper.Values pc) {
        if(!(EntityUtil.isEating(mc.player) || EntityUtil.isDrinking(mc.player))) { 	return false;  }
        if(GroundCondition.getCurrentMode().equals("Only Ground") && !ACUtil.isground()) { 	return false; 	}
        else if(GroundCondition.getCurrentMode().equals("Only Air") && ACUtil.isground()) { 	return false; 	}

        if(MoveCondition.getCurrentMode().equals("Only Moving") && MoveUtil.getspeed2()<=0.001) { 	return false; 	}
        else if(MoveCondition.getCurrentMode().equals("Only NoMove") && MoveUtil.getspeed2()>0.001) { 	return false; 	}

        if(!Drinks.isEnabled() && EntityUtil.isEating(mc.player)) { 	return false; 	}
        if(!Food.isEnabled() && EntityUtil.isDrinking(mc.player)) { 	return false; 	}
        return true;
    }


    @Override
    public void onEnable() {timerr.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        TimerUtil.reset();
        super.onDisable();
    }

}
