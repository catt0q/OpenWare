package base.mixin.client;

import base.client.helpers.utils.MoveUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WebBlock.class)
public abstract class WebBlockMixin {

    /*
     * @Redirect(
     * method = "entityInside",
     * at = @At(
     * value = "INVOKE",
     * target =
     * "Lnet/minecraft/world/entity/Entity;makeStuckInBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V"
     * )
     * )
     * private void redirectMakeStuckInBlock(Entity entity, BlockState state, Vec3
     * originalMultiplier) {
     * EventInWeb event=new EventInWeb(originalMultiplier);
     * EventManager.call(event);
     * 
     * entity.makeStuckInBlock(state, event.getVec());
     * }
     */
    @Inject(method = "entityInside", at = @At("TAIL"), cancellable = true)
    private void onEntityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity,
            InsideBlockEffectApplier insideBlockEffectApplier, boolean bl, CallbackInfo ci) {
        MoveUtil.lastInWeb = true;
    }
}