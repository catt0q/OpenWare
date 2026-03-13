package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.world.item.Items;

public class ZamorozkaComponent extends AnarchyHelperComponent {

    public ZamorozkaComponent() {
        super("Снежок заморозка", "Снежок заморозка", Items.SNOWBALL);
    }

    @Override
    public int executeItem() {
        return super.simpleUse(getRenderingItem(), getItemName());
    }
}
