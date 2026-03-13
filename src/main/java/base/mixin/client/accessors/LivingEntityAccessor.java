package base.mixin.client.accessors;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("fallFlyTicks")
    int getFallFlyTicks();

    @Accessor("fallFlyTicks")
    void setFallFlyTicks(int ticks);
}
