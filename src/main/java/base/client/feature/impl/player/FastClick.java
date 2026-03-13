package base.client.feature.impl.player;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import net.minecraft.world.item.*;
public class FastClick extends Module {
    int index1 = 0;
    public BooleanSetting IgnoreEmptyInteractionHand = new BooleanSetting("Ignore EmptyInteractionHand", true, () -> true);

    public BooleanSetting IgnoreBlock = new BooleanSetting("Ignore Block", false, () -> true);

    public BooleanSetting IgnoreFireCharge = new BooleanSetting("Ignore FireCharge", true, () -> true);

    public BooleanSetting IgnoreEnderPearl = new BooleanSetting("Ignore EnderPearl", true, () -> true);

    public BooleanSetting IgnoreFishingRod = new BooleanSetting("Ignore FishingRod", true, () -> true);

    public BooleanSetting IgnoreSwords = new BooleanSetting("Ignore Swords", true, () -> true);

    public BooleanSetting IgnoreSnowballs = new BooleanSetting("Ignore Snowballs", true, () -> true);

    public BooleanSetting IgnoreEggs = new BooleanSetting("Ignore Eggs", true, () -> true);

    public static NumberSetting delay = new NumberSetting("Delay", 1, 0, 10, 1, () -> true);

    public FastClick() {
        super("FastClick", "Убирает задержку на клики", Type.Player);
        addSettings(delay,
                IgnoreEmptyInteractionHand,
                IgnoreBlock,
                IgnoreFireCharge,
                IgnoreFishingRod,
                IgnoreEnderPearl,
                IgnoreSwords,
                IgnoreSnowballs,
                IgnoreEggs


        );
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }
    @Override
    public void onDisable() {
        mc.rightClickDelay = 0;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {

        if (!getState())
            return;


        if (isValid()) {
            if (index1 >= (int) delay.getValue()) {
                mc.rightClickDelay = 0;
                index1 = 0;
            }
        }
        index1++;


    }

    boolean isValid() {
        if (IgnoreEmptyInteractionHand.isEnabled() && mc.player.getMainHandItem().getItem() instanceof AirItem) {
            return false;
        }
        if (IgnoreBlock.isEnabled() && mc.player.getMainHandItem().getItem() instanceof BlockItem) {
            return false;
        }
        if (IgnoreFireCharge.isEnabled() && mc.player.getMainHandItem().getItem() instanceof FireChargeItem) {
            return false;
        }
        if (IgnoreEnderPearl.isEnabled() && mc.player.getMainHandItem().getItem() instanceof EnderpearlItem) {
            return false;
        }
        if (IgnoreFishingRod.isEnabled() && mc.player.getMainHandItem().getItem() instanceof FishingRodItem) {
            return false;
        }
        if (IgnoreSnowballs.isEnabled() && mc.player.getMainHandItem().getItem() instanceof SnowballItem) {
            return false;
        }
        if (IgnoreEggs.isEnabled() && mc.player.getMainHandItem().getItem() instanceof EggItem) {
            return false;
        }


        return true;
    }


}
