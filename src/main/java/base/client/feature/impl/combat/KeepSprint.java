package base.client.feature.impl.combat;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import net.minecraft.world.entity.LivingEntity;

public class KeepSprint extends Module {

    public static BooleanSetting KeepSmarrt = new BooleanSetting("KeepSprint Smart","Будет проверять действительно ли не нужно замедление после удара" ,false,() -> true);
    public static ModeSetting GroundCondition= new ModeSetting("Ground Condition", "Last-NextTick", () -> true, "LastTick","NextTick","Last-NextTick");

    public static NumberSetting MaxHurtTime = new NumberSetting("Max HurtTime","Сколько времени прошло с удара по противнику чтобы заработал KeepSprint", 10, 0, 10, 1, () -> true);
    public static NumberSetting HitChance = new NumberSetting("Hit Chance","Шанс, что игрок не будет замедлен после атаки", 100, 0, 100, 1, () -> true);



    public static ModeSetting KeepSmarrtCondition = new ModeSetting("KeepSprint Smart Condition","Motion проверяет если ты отдаляешся от противника то редьюсить", "Motion",() -> KeepSmarrt.isEnabled(), "HurtTime","Motion");

    public static NumberSetting SmartHurtTime = new NumberSetting("Smart HurtTime","Учитывает время от последней атаки по тебе, чтобы заредьюсить", 0, 0, 9, 1,() -> KeepSmarrtCondition.isVisible() && KeepSmarrtCondition.getCurrentMode().equals("HurtTime"));

    public static BooleanSetting GmodifierS = new BooleanSetting("Smart Ground modifier", true,() -> KeepSmarrt.isEnabled());
    public static BooleanSetting NoGmodifierS = new BooleanSetting("Smart NoGround modifier", true,() -> KeepSmarrt.isEnabled());

    public static  ModeSetting GsavesprintModeS = new ModeSetting("Smart Ground Sprint", "Save",() -> KeepSmarrt.isEnabled() && GmodifierS.isEnabled(), "Save","Sprint","NoSprint");
    public static  ModeSetting NoGsavesprintModeS = new ModeSetting("Smart NoGround Sprint", "Save",() -> KeepSmarrt.isEnabled() && NoGmodifierS.isEnabled(), "Save","Sprint","NoSprint");

    public static BooleanSetting GmodifymotionS = new BooleanSetting("Smart Ground modify motion", true,() -> KeepSmarrt.isEnabled() && GmodifierS.isEnabled());
    public static BooleanSetting NoGmodifymotionS = new BooleanSetting("Smart NoGround modify motion", true,() -> KeepSmarrt.isEnabled() && NoGmodifierS.isEnabled());

    public static NumberSetting GmultS = new NumberSetting("Smart Ground Multiply", 1F, 0F, 1F, 0.01F,() -> KeepSmarrt.isEnabled() && GmodifierS.isEnabled() && GmodifymotionS.isEnabled());
    public static NumberSetting NoGmultS = new NumberSetting("Smart NoGround Multiply", 1F, 0F, 1F, 0.01F,() -> KeepSmarrt.isEnabled() && NoGmodifierS.isEnabled() && NoGmodifymotionS.isEnabled());





    public static BooleanSetting Gmodifier = new BooleanSetting("Ground modifier","Если включено то можно настроить замедление на земле" ,true,() -> true);
    public static BooleanSetting NoGmodifier = new BooleanSetting("NoGround modifier","Если включено то можно настроить замедление не на земле" , true,() -> true);

    public static  ModeSetting GsavesprintMode = new ModeSetting("Ground Sprint", "Save",() -> Gmodifier.isEnabled(), "Save","Sprint","NoSprint");
    public static  ModeSetting NoGsavesprintMode = new ModeSetting("NoGround Sprint", "Save",() -> NoGmodifier.isEnabled(), "Save","Sprint","NoSprint");

    public static BooleanSetting Gmodifymotion = new BooleanSetting("Ground modify motion", true,() -> Gmodifier.isEnabled());
    public static BooleanSetting NoGmodifymotion = new BooleanSetting("NoGround modify motion", true,() -> NoGmodifier.isEnabled());

    public static NumberSetting Gmult = new NumberSetting("Ground Multiply", 1F, 0F, 1F, 0.01F,() -> Gmodifier.isEnabled() && Gmodifymotion.isEnabled());
    public static NumberSetting NoGmult = new NumberSetting("NoGround Multiply", 1F, 0F, 1F, 0.01F,() -> NoGmodifier.isEnabled() && NoGmodifymotion.isEnabled());




    public KeepSprint() {
        super("KeepSprint", "Повзоляет редактировать скорость игрока при ударе", Type.Combat);





        addSettings(KeepSmarrt,GroundCondition,
                MaxHurtTime,HitChance,

                KeepSmarrtCondition,SmartHurtTime,
                GmodifierS,GsavesprintModeS,GmodifymotionS,GmultS,NoGmodifierS,NoGsavesprintModeS,NoGmodifymotionS,NoGmultS,
                Gmodifier,GsavesprintMode,Gmodifymotion,Gmult,NoGmodifier,NoGsavesprintMode,NoGmodifymotion,NoGmult);
    }


    public static void modifysprint(boolean msprint,boolean mmot) {
         boolean smartmodify=false;
//idk what correct
boolean isground=mc.player.onGround();
if(GroundCondition.getCurrentMode().equals("LastTick")){
    isground=pc.LastGround;
}else if(GroundCondition.getCurrentMode().equals("Last-NextTick") && pc.LastGround){
    isground=true;
}
if(Math.random()*100>HitChance.getValue() || (pc.lastattackentity!=null && ((LivingEntity)pc.lastattackentity).hurtTime>MaxHurtTime.getValue())){

    if(msprint){
        mc.player.setSprinting(false);
    }else{
        MoveUtil.mult2ds(0.6D);
    }


    return;
}





          if(KeepSmarrt.isEnabled() && isgoodmotion()) {
            smartmodify=true;
            if(isground) {
                if(GmodifierS.isEnabled()){
                    if(msprint && !GsavesprintModeS.getCurrentMode().equals("Save")) {
                        mc.player.setSprinting(GsavesprintModeS.getCurrentMode().equals("Sprint"));
                    }
                    if(mmot) {
                        if (GmodifymotionS.isEnabled()) {
                            MoveUtil.mult2ds(GmultS.getValue());
                        }
                        // If GmodifymotionS disabled, don't apply any slowdown
                    }
                }else {
                    smartmodify=false;
                }


            }else {
                if(NoGmodifierS.isEnabled()){
                    if(msprint && !NoGsavesprintModeS.getCurrentMode().equals("Save")) {
                        mc.player.setSprinting(NoGsavesprintModeS.getCurrentMode().equals("Sprint"));
                    }
                    if(mmot) {
                        if (NoGmodifymotionS.isEnabled()) {
                            MoveUtil.mult2ds(NoGmultS.getValue());
                        }
                        // If NoGmodifymotionS disabled, don't apply any slowdown
                    }
                } else {
                    smartmodify=false;
                }
            }

        }



        if(!smartmodify) {
            if(isground) {
                if(Gmodifier.isEnabled()){
                    if(msprint && !GsavesprintMode.getCurrentMode().equals("Save")) {
                        mc.player.setSprinting(GsavesprintMode.getCurrentMode().equals("Sprint"));
                    }
                    if(mmot) {
                        if (Gmodifymotion.isEnabled()) {
                            MoveUtil.mult2ds(Gmult.getValue());
                        } else {
                            MoveUtil.mult2ds(0.6D);
                        }
                    }
                }
                // If Gmodifier disabled, do nothing (keep sprint, no slowdown)

            }else {
                if(NoGmodifier.isEnabled()){

                    if(msprint && !NoGsavesprintMode.getCurrentMode().equals("Save")) {
                        mc.player.setSprinting(NoGsavesprintMode.getCurrentMode().equals("Sprint"));
                    }
                    if(mmot) {
                        if (NoGmodifymotion.isEnabled()) {
                            MoveUtil.mult2ds(NoGmult.getValue());
                        }
                        // If NoGmodifymotion disabled, don't apply any slowdown
                    }
                }
                // If NoGmodifier disabled, do nothing (keep sprint, no slowdown)

            }
        }
    }

    static boolean isgoodmotion() {
        if(KeepSmarrtCondition.getCurrentMode().equals("Motion")) {
            if(    RotationUtils.yawdiff(MoveUtil.getdir(true), MoveUtil.getmovedir())<30) {
                return true;
            }
            return false;
        }else {
            if(mc.player.hurtTime<=SmartHurtTime.getValue()) {
                return true;
            }
            return false;
        }
    }


}
