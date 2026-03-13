package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.world.item.Items;

public class DezorentComponent extends AnarchyHelperComponent {

    public DezorentComponent() {
        super("Дезориентация", "Дезориентация", Items.ENDER_EYE);
    }

    @Override
    public int executeItem() {
        return super.simpleUse(getRenderingItem(), getItemName());
    }
}
