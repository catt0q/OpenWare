package base.client.helpers.utils;

import base.client.Client;
import base.client.feature.impl.combat.Criticals;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

public class CombatUtil {
    public static float ofallDistance=0;
    public static float fallDistance=0;


    static Minecraft mc=Minecraft.getInstance();


    public static boolean canCrit(boolean skipblind){
        return canCrit(skipblind,false);
     }
    public static boolean canCrit(boolean skipblind,boolean skipsprint){
        return Client.instance.featureManager.getModuleByClass(Criticals.class).getState() ? Criticals.canCrit() : (fallDistance > 0.01 && !mc.player.onGround() && !Client.instance.packet.LastGround)
                && !mc.player.onClimbable() && (!mc.player.hasEffect(MobEffects.BLINDNESS) || skipblind)
                && !mc.player.isInWater()
                && !mc.player.isVehicle() && (!mc.player.isSprinting() || skipsprint);
    }
    public static boolean canCrit(){
        return canCrit(false,false);
    }
    public static boolean passcooldown(){
        return mc.player.getAttackStrengthScale(0.5f) >= 1f;
    }
    public static void resetfd(){
        ofallDistance=0;fallDistance=0;
    }
    public static void resetfd(float val){
        ofallDistance=val;fallDistance=val;
    }
}
