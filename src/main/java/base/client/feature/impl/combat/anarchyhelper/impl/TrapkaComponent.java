package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.world.item.Items;

public class TrapkaComponent extends AnarchyHelperComponent {

    public TrapkaComponent() {
        super("Трапка", "Трапка", Items.NETHERITE_SCRAP);
    }
    @Override
    public int executeItem() {
        return super.simpleUse(getRenderingItem(), getItemName());
    }
}
