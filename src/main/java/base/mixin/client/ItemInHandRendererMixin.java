package base.mixin.client;

import base.client.Client;
import base.client.feature.impl.combat.Animations;
import base.client.feature.impl.combat.KillAuraNew;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static base.client.helpers.Helper.mc;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    private float oMainHandHeight;

    @Shadow
    private float mainHandHeight;

    @Shadow
    protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f);

    @Shadow
    protected abstract void renderItem(LivingEntity livingEntity, ItemStack itemStack,
            ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i);

    // inject before applyItemArmTransform to apply custom blocking animation
    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V", shift = At.Shift.BEFORE), cancellable = true)
    private void cancelBlockTransform(AbstractClientPlayer abstractClientPlayer, float p_187457_2_, float g,
            InteractionHand interactionHand, float p_187457_5_, ItemStack itemStack, float p_187457_7_, PoseStack ps,
            SubmitNodeCollector submitNodeCollector, int j, CallbackInfo ci) {

        if (itemStack.getUseAnimation() == ItemUseAnimation.BLOCK && !(itemStack.getItem() instanceof ShieldItem)) {
            boolean bl = interactionHand == InteractionHand.MAIN_HAND;
            HumanoidArm humanoidArm = bl ? abstractClientPlayer.getMainArm()
                    : abstractClientPlayer.getMainArm().getOpposite();
            boolean bl2 = humanoidArm == HumanoidArm.RIGHT;
            int q = bl2 ? 1 : -1;
            ItemInHandRenderer instance = (ItemInHandRenderer) (Object) this;
            if (Client.instance.featureManager.getModuleByClass(Animations.class).getState()
                    && Animations.animation.isEnabled()
                    && (!Animations.needtarget.isEnabled()
                            || (Client.instance.featureManager.getModuleByClass(KillAuraNew.class).getState()
                                    && KillAuraNew.lasttarget != null && Minecraft.getInstance().player != null
                                    && Minecraft.getInstance().player
                                            .distanceTo(KillAuraNew.lasttarget) <= KillAuraNew.range.getValue()))
                    && (!Animations.onlysword.isEnabled() || mc.player.getMainHandItem().is(ItemTags.SWORDS))) {
                Animations.generateswing(instance,
                        p_187457_5_,
                        p_187457_7_,
                        ps,
                        q,
                        humanoidArm, p_187457_2_, oMainHandHeight, mainHandHeight, bl2);

                this.renderItem(
                        abstractClientPlayer,
                        itemStack,
                        bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        ps,
                        submitNodeCollector,
                        j);
                ps.popPose();
                ci.cancel();
            }
        }
    }

    // redirect swingArm to apply custom swing animation
    // new signature: swingArm(float, PoseStack, int, HumanoidArm)
    @Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;swingArm(FLcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/entity/HumanoidArm;)V"))
    private void customSwing(
            ItemInHandRenderer instance,
            float swingProgress,
            PoseStack ps,
            int sided,
            HumanoidArm humanoidArm) {

        boolean bl2 = humanoidArm == HumanoidArm.RIGHT;
        int q = bl2 ? 1 : -1;

        if (Client.instance.featureManager.getModuleByClass(Animations.class).getState()
                && Animations.animation.isEnabled()
                && (!Animations.needtarget.isEnabled()
                        || (Client.instance.featureManager.getModuleByClass(KillAuraNew.class).getState()
                                && KillAuraNew.lasttarget != null && Minecraft.getInstance().player != null
                                && Minecraft.getInstance().player
                                        .distanceTo(KillAuraNew.lasttarget) <= KillAuraNew.range.getValue()))) {

            // custom animation - use Animations class for transforms
            Animations.generateswing(instance,
                    swingProgress,
                    0f, // equipProgress not available here, use 0
                    ps,
                    sided,
                    humanoidArm,
                    0f, oMainHandHeight, mainHandHeight, bl2);

        } else {
            // call shadowed swingArm
            this.swingArm(swingProgress, ps, q, humanoidArm);
        }
    }

    // inject at tail of applyItemArmAttackTransform for item scaling
    @Inject(method = "applyItemArmAttackTransform", at = @At("TAIL"))
    private void onApplyItemArmAttackTransform(
            PoseStack poseStack,
            HumanoidArm humanoidArm,
            float f,
            CallbackInfo ci) {
        if (Client.instance.featureManager.getModuleByClass(Animations.class).getState()) {
            if (Animations.smallItem.isEnabled()) {
                poseStack.scale(0.7f, 0.7f, 0.7f);
            } else if (Animations.swordAnim.currentMode.equals("Custom")) {
                poseStack.scale(Animations.scale.getValue(), Animations.scale.getValue(), Animations.scale.getValue());
            }
        }
    }

    // inject for item spin animations (360/Spin modes)
    @Inject(method = "applyItemArmAttackTransform", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void injectItemRotation(PoseStack poseStack, HumanoidArm humanoidArm, float f, CallbackInfo ci) {
        float angle = (float) (System.currentTimeMillis() / 3L % 360L);
        if (Client.instance.featureManager.getModuleByClass(Animations.class).getState()
                && Animations.itemAnimation.isEnabled()) {
            if (Animations.itemAnim.currentMode.equals("360")) {
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            } else if (Animations.itemAnim.currentMode.equals("Spin")) {
                Vector3f axis = new Vector3f(Animations.spin, 0, Animations.spin).normalize();
                Quaternionf rotation = new Quaternionf(
                        new AxisAngle4f(Animations.spin * Animations.spinSpeed.getValue() * 0.05F, axis));
                poseStack.mulPose(rotation);
                Animations.spin++;
            }
        }
    }

    @Shadow
    protected abstract void swingArm(float f, PoseStack poseStack, int i, HumanoidArm humanoidArm);
}