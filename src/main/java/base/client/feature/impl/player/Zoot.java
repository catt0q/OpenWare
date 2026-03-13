package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class Zoot extends Module {


    private ModeSetting PacketMode = new ModeSetting("Packet Mode", "NewPacket",()->true,  "NewPacket","Vanilla" );
    private NumberSetting PacketCount=new NumberSetting("Packet Count",8, 0, 40, 1,()->true);
    private NumberSetting BadPotionMaxDuration=new NumberSetting("Bad Potion Duration",60, 0, 600, 1,()->true);
    private BooleanSetting OnFire = new BooleanSetting("On Fire", true,()->true);
    private BooleanSetting OnBadPotion = new BooleanSetting("On Bad Potion", false,()->true);
    private BooleanSetting OnPositivePotion = new BooleanSetting("Save Positive Potion", false,()->true);
    private ModeSetting GroundCondition= new ModeSetting("Ground Condition", "Always", () -> true, "Only Ground", "Only Air", "Always");

    public Zoot() {
        super("Zoot", "Позволяет быстрее избавиться от негативных состояний", Type.Player);
        this.addSettings(PacketMode,GroundCondition,PacketCount,OnPositivePotion,OnFire,OnBadPotion,BadPotionMaxDuration);
    }

    @EventTarget
    public void onUpdate(EventPostMotion event) {
        PacketHelper.Values pc= Client.instance.packet;
        if(!needboost(pc)) {
            return;
        }

        switch(PacketMode.getCurrentMode()) {

            case("NewPacket"):
                for(int i=0;i<PacketCount.getValue();i++) {
                    ACUtil.send117DuplPacket();
                    mc.player.useItemRemaining-=1;
                }
                break;

            case("Vanilla"):
                for(int i=0;i<PacketCount.getValue();i++) {
                    ServerboundMovePlayerPacket duplc03=new ServerboundMovePlayerPacket.StatusOnly(pc.LastGround,mc.player.horizontalCollision);
                    pc.sendPacket(duplc03,10);
                    mc.player.useItemRemaining-=1;
                }
                break;


        }



    }



    private boolean isCondition(PacketHelper.Values pc) {
        if(!(EntityUtil.isEating(mc.player) || EntityUtil.isDrinking(mc.player))) { 	return false;  }
        if(GroundCondition.getCurrentMode().equals("Only Ground") && !pc.LastGround) { 	return false; 	}
        else if(GroundCondition.getCurrentMode().equals("Only Air") && pc.LastGround) { 	return false; 	}
        return true;
    }

    private boolean needboost(PacketHelper.Values pc) {
        isCondition(pc);
        int bpsz=0;
        int ppsz=0;


        if (mc.player.getActiveEffects().size()>0) {
            for(MobEffectInstance mobeffectinstance : mc.player.getActiveEffects()) {

                if(mobeffectinstance.getEffect().value().getCategory().equals(MobEffectCategory.BENEFICIAL)) {
                    ppsz++;
                }else if(mobeffectinstance.getDuration()/20<=BadPotionMaxDuration.getValue()){
                    bpsz++;
                }

            }
        }

        if(OnPositivePotion.isEnabled() && ppsz>0) {
            return false;
        }
        if((mc.player.isOnFire() && !mc.player.isInLava() && OnFire.isEnabled() && !mc.player.hasEffect(MobEffects.BLINDNESS) || (bpsz>0 && OnBadPotion.isEnabled()))) {
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        TimerUtil.reset();
        super.onDisable();
    }

}
