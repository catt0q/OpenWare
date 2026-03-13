package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.world.item.Items;

public class GodAuraComponent extends AnarchyHelperComponent {

    public GodAuraComponent() {
        super("Божья аура", "Божья аура", Items.PHANTOM_MEMBRANE);
    }

    @Override
    public int executeItem() {
        return super.simpleUse(getRenderingItem(), getItemName());
    }
}
