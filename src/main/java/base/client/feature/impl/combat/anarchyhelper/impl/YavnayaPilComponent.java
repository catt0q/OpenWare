package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.world.item.Items;

public class YavnayaPilComponent extends AnarchyHelperComponent {

    public YavnayaPilComponent() {
        super("Явная пыль", "Явная пыль", Items.SUGAR);
    }

    @Override
    public int executeItem() {
        return super.simpleUse(getRenderingItem(), getItemName());
    }
}
