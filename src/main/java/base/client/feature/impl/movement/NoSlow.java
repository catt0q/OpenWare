package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnSlow;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.*;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import base.client.feature.Module;
public class NoSlow extends Module {
    int index1=0;
    int index2=0;
    int index3=0;
    int index4=0;
    int index5=0;
    int index6=0;
    int prevslotid=0;
    boolean nextspoof=false;
    boolean wasitemused=false;

    public NumberSetting percentage;
    public NumberSetting StrictAmount;
    public static ModeSetting motionMode,consumeMode,blockingMode,shieldMode,bowMode;
     public static BooleanSetting CancelSlotChange = new BooleanSetting("Cancel Slot Change", true,()->true   );
    public static BooleanSetting AllowSprint,MatrixCSD,MatrixSlowDiag;

    public NoSlow() {
        super("NoSlow", "Убирает замедление при использовании еды и других предметов", Type.Movement);

        motionMode = new ModeSetting("Motion Mode","Выбор этого мода на None позволит лучше настроить модуль", "Matrix1", () -> true,
                "Vanilla","Grim","Strict","Matrix1","Matrix2");


        consumeMode = new ModeSetting("Consume Mode", "Matrix1", () -> true, "Vanilla","Grim","Strict","Matrix1","Matrix2");
        blockingMode = new ModeSetting("Blocking Mode", "Matrix1", () -> true, "Vanilla","Grim","Strict","Matrix1","Matrix2");
        shieldMode = new ModeSetting("Shield Mode", "Matrix1", () -> true, "Vanilla","Grim","Strict","Matrix1","Matrix2");
        bowMode = new ModeSetting("Bow Mode", "Matrix1", () -> true, "Vanilla","Grim","Strict","Matrix1","Matrix2");


        percentage = new NumberSetting("Percentage", 100, 0, 100, 1, ()-> consumeMode.getCurrentMode().equals("Vanilla"));
        StrictAmount = new NumberSetting("StrictAmount", 2, 1, 10, 1, ()-> consumeMode.getCurrentMode().equals("Strict"));
        AllowSprint = new BooleanSetting("Allow Sprint", true,()->true   );
        MatrixCSD = new BooleanSetting("Damage", true,()->(consumeMode.getCurrentMode().contains("Matrix")));
        MatrixSlowDiag = new BooleanSetting("MatrixSlowDiag", false,()->(consumeMode.getCurrentMode().contains("Matrix")));
       addSettings(motionMode,consumeMode,blockingMode,shieldMode,bowMode,StrictAmount, percentage,AllowSprint,CancelSlotChange,MatrixCSD,MatrixSlowDiag);
    }

    @Override
    public void onEnable() {

        index2=0;  index1=0; index5=0; index6=0; index4=0;

        super.onEnable();
    }
    @Override
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }

    @EventTarget
    public void onSlow(EventOnSlow e) {
        PacketHelper.Values pc= Client.instance.packet;
switch (motionMode.currentMode) {

    case ("Polar"):
        if (mc.player.getTicksUsingItem() > 0 && mc.player.getUseItemRemainingTicks() != 0) {
            e.mult = (mc.player.getDeltaMovement().y() > 0 ? 0.28F : 0.21F);
        }


        break;
    case ("Grim"):
        if (mc.player.getTicksUsingItem() != 0 && mc.player.getTicksUsingItem() % 2 == 0) {
            e.mult = 1;
            mc.player.setSprinting(true);
        }
        break;
    case ("Vanilla"):
        e.mult = percentage.getValue() / 100;
        break;
    case ("Strict"):
        if (mc.player.getTicksUsingItem() != 0 && mc.player.getTicksUsingItem() % StrictAmount.getValue() == 0) {
            e.mult = 1;
        }
        break;
    case ("Matrix1"):
        if (ACUtil.matrixisusingitem()) {
            if (!mc.player.isUsingItem() && e.mult > 0.9) {
                e.mult = 0.2F; return;
            }


            if ((!pc.lastVeltimer.hasTimeElapsed(1300, false) && MatrixCSD.isEnabled()) ||
                    (nextspoof) || mc.player.isInWater() || mc.player.isCrouching()) {
                e.mult = 1;
                index1 = 0;

            } else {


                if (mc.player.onGround()) {
                    if (MoveUtil.motYstateh()) {
                        e.mult = 1F;
                    } else {
                        e.mult = 0.54F;
                    }
                    index2 = 0;
                } else {
                    e.mult = 0.694F;
                }


                if (index1 % 3 == 0 && index1 % 12 != 0) {
                    e.mult = 1;
                }


                if (MoveUtil.getmf() != 0 && MoveUtil.getms() != 0 && MatrixSlowDiag.isEnabled()) {
                    e.mult *= 0.7D;
                }


                e.mult = Math.min(e.mult, 1);

            }
            nextspoof = false;
            index1++;
            index2++;
        } else {
            index6 = 0;
            index5 = 0;
            index1 = 0;
            index2 = 0;
        }
        index6 = index4;
        index4 = mc.player.isUsingItem() ? 1 : 0;
        break;

    case ("Matrix2"):
        if (ACUtil.matrixisusingitem()) {
            if (!mc.player.isUsingItem() && e.mult > 0.9) {
                e.mult = 0.2F; return;
            }

            if (mc.player.onGround()) {
                if (MoveUtil.motYstateh()) {
                    e.mult = 1F;
                } else {
                    e.mult = 0.54F;
                }
            } else {
                e.mult = 0.694F;
            }


            if (MoveUtil.getmf() != 0 && MoveUtil.getms() != 0 && MatrixSlowDiag.isEnabled()) {
                e.mult *= 0.7D;
            }


            e.mult = Math.min(e.mult, 1);

        }
        break;

    case ("None"):













        break;


















}

























    }


}
