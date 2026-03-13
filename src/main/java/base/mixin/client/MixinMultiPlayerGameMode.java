package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.game.EventBlockBreaking;
import base.client.feature.impl.exploit.PacketFixer;
import base.client.feature.impl.player.NoSlowBreak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    // prevent crash when player is null during server transfer
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) {
            ci.cancel();
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void modifyUseItemOn(
            LocalPlayer localPlayer,
            InteractionHand interactionHand,
            BlockHitResult blockHitResult,
            CallbackInfoReturnable<InteractionResult> cir) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        MultiPlayerGameMode instance = (MultiPlayerGameMode) (Object) this;

        if (!mc.level.getWorldBorder().isWithinBounds(blockHitResult.getBlockPos())
                && PacketFixer.CancelMissPlacement.isEnabled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }

        if (mc.player.getMainHandItem().getItem() instanceof BlockItem
                && PacketFixer.CancelMissPlacement.isEnabled()
                && Client.instance.featureManager.getModuleByClass(PacketFixer.class).getState()) {
            MutableObject<InteractionResult> mutableObject = new MutableObject<>();
            mutableObject.setValue(instance.performUseItemOn(localPlayer, interactionHand, blockHitResult));
            if (mutableObject.getValue().consumesAction()) {
                instance.startPrediction(mc.level,
                        i -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, i));
                cir.setReturnValue(mutableObject.getValue());
            } else {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }

    @Inject(method = "continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;ensureHasSentCarriedItem()V", shift = At.Shift.AFTER))
    private void onContinueDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        EventBlockBreaking eventBlockInteract = new EventBlockBreaking();
        EventManager.call(eventBlockInteract);
    }

    @Redirect(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;sameDestroyTarget(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean forceSameDestroyTarget(MultiPlayerGameMode instance, BlockPos blockPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null)
            return false;

        ItemStack itemStack = mc.player.getMainHandItem();
        return blockPos.equals(mc.gameMode.destroyBlockPos)
                && (ItemStack.isSameItemSameComponents(itemStack, mc.gameMode.destroyingItem)
                        || (Client.instance.featureManager.getModuleByClass(NoSlowBreak.class).getState()
                                && NoSlowBreak.HC.isEnabled()));
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(net.minecraft.world.entity.player.Player player, net.minecraft.world.entity.Entity target,
            CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) {
            ci.cancel();
            return;
        }
        base.client.event.events.impl.motion.EventAttackEntity event = new base.client.event.events.impl.motion.EventAttackEntity(
                target);
        EventManager.call(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}