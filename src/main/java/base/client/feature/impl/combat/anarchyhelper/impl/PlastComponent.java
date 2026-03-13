package base.client.feature.impl.combat.anarchyhelper.impl;

import base.client.feature.impl.combat.anarchyhelper.AnarchyHelperComponent;
import net.minecraft.world.item.Items;

public class PlastComponent extends AnarchyHelperComponent {

    public PlastComponent() {
        super("Пласт", "Пласт", Items.DRIED_KELP);
    }
    @Override
    public int executeItem() {
        return super.simpleUse(getRenderingItem(), getItemName());
    }
}
