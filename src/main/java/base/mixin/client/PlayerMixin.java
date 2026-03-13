package base.mixin.client;

import base.client.Client;
import base.client.feature.impl.combat.KeepSprint;
import base.client.feature.impl.combat.Reach;
import base.client.feature.impl.player.NoSlowBreak;
import base.client.helpers.utils.MoveUtil;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerMixin {
    boolean skipsprint = false;

    // 1.21.11: sprint reset moved from attack() to causeExtraKnockback()
    @Redirect(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void redirectSetVelocity(Player instance, Vec3 velocity) {
        if (skipsprint) {
            skipsprint = false;
            return;
        }
        if (Client.instance.featureManager.getModuleByClass(KeepSprint.class).getState()) {
            skipsprint = true;
            KeepSprint.modifysprint(false, true);
            skipsprint = false;
        } else {
            MoveUtil.mult2ds(0.6D);
        }
    }

    @Redirect(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"))
    private void redirectSetSprinting(Player instance, boolean b) {
        if (skipsprint) {
            skipsprint = false;
            return;
        }
        if (Client.instance.featureManager.getModuleByClass(KeepSprint.class).getState()) {
            skipsprint = true;
            KeepSprint.modifysprint(true, false);
            skipsprint = false;
        } else {
            skipsprint = true;
            Minecraft.getInstance().player.setSprinting(false);
            skipsprint = false;
        }
    }

    @Redirect(method = "getDestroySpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean ignoreWaterCheck(Player instance, TagKey tagKey) {
        return (instance.isEyeInFluid(FluidTags.WATER)
                && (Client.instance.featureManager.getModuleByClass(NoSlowBreak.class).getState()
                        && NoSlowBreak.water.isEnabled()));
    }

    @Redirect(method = "getDestroySpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean ignoreOnGroundCheck(Player instance) {
        return (instance.onGround() || (Client.instance.featureManager.getModuleByClass(NoSlowBreak.class).getState()
                && NoSlowBreak.air.isEnabled()));
    }

    @ModifyReturnValue(method = "blockInteractionRange()D", at = @At("RETURN"))
    private double modifyBlockInteractionRange(double original) {
        return Reach.getinteractrange(original);
    }

    @ModifyReturnValue(method = "entityInteractionRange()D", at = @At("RETURN"))
    private double modifyEntityInteractionRange(double original) {
        return Reach.getattackrange(original);
    }
}