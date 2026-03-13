package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.world.item.Items;

public class EnderPearlComponent extends AnarchyHelperComponent {

    public EnderPearlComponent() {
        super("EnderPearl", "Ender Pearl", Items.ENDER_PEARL);
    }

    @Override
    public int executeItem() {
        return super.simpleUse(getRenderingItem(), getItemName());
    }
}
