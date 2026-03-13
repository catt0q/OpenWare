package base.client.feature.impl.combat;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.component.BlocksAttacks;

import java.util.List;
import java.util.Optional;

public class SwordBlockHit extends Module {


    public SwordBlockHit() {

        super("SwordBlockHit", "Чтобы блокхтить", Type.Misc);
        this.addSettings();
    }

    public void onEnable() {

        super.onEnable();
    }
    public void onDisable() {


        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {
        if(mc.player.getMainHandItem().is(ItemTags.SWORDS)
        ){
            final BlocksAttacks INSTANT_BLOCK = new BlocksAttacks(
                    0.0F,
                    1.0F,
                    List.of(
                            new BlocksAttacks.DamageReduction(
                                    90.0F,
                                    Optional.empty(),
                                    100.0F,
                                    0.0F
                            )
                    ),
                    BlocksAttacks.ItemDamageFunction.DEFAULT,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            mc.player.getMainHandItem().set(DataComponents.BLOCKS_ATTACKS, INSTANT_BLOCK);
        }
    }


}
