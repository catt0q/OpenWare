package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.input.EventSprint;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.feature.impl.combat.Animations;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.MoveFix;
import base.client.feature.impl.movement.Sprint;
import base.client.feature.impl.movement.Step;
import base.client.feature.impl.visual.Optimizator;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    float yaw = 0, prevyaw = 0;
    boolean cancelsprintevent = false;
    private static final Identifier SPRINTING_SPEED_MODIFIER_ID = Identifier
            .withDefaultNamespace("sprinting");
    private static final AttributeModifier SPRINTING_SPEED_BOOST = new AttributeModifier(
            SPRINTING_SPEED_MODIFIER_ID, 0.3F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    private void forceSwingDuration(CallbackInfoReturnable<Integer> cir) {
        int anum = Animations.animspeed();
        if (anum != -1) {
            cir.setReturnValue(anum);
        }
    }

    /*
     * @Redirect(
     * method = "aiStep",
     * at = @At(
     * value = "INVOKE",
     * target = "Lnet/minecraft/world/entity/LivingEntity;applyInput()V"
     * )
     * )
     * private void redirectApplyInput(LivingEntity instance) {
     * if(Minecraft.getInstance().player!=null &&
     * ((LivingEntity)(Object)this).getId()==Minecraft.getInstance().player.getId())
     * {
     * LocalPlayer player = Minecraft.getInstance().player;
     * 
     * float f1 = KeyboardInput.calculateImpulse(player.input.keyPresses.forward(),
     * player.input.keyPresses.backward());
     * float g1 = KeyboardInput.calculateImpulse(player.input.keyPresses.left(),
     * player.input.keyPresses.right());
     * Vec2 vec2 = new Vec2(g1, f1).normalized();
     * 
     * Vec2 vec22 = vec2.scale(0.98F);
     * if (player.isUsingItem() && player.getVehicle()==null) {
     * EventOnSlow event = new EventOnSlow(0.2F);
     * EventManager.call(event);
     * vec22 = vec22.scale(event.mult);
     * }
     * 
     * 
     * if (player.isCrouching()) {
     * 
     * float f =
     * Client.instance.featureManager.getModuleByClass(FastSneak.class).getState() ?
     * FastSneak.mult.getValue() :
     * (float)player.getAttributeValue(Attributes.SNEAKING_SPEED);
     * vec22 = vec22.scale(f);
     * }
     * Minecraft.getInstance().player.xxa = vec22.x;
     * Minecraft.getInstance().player.zza = vec22.y;
     * }
     * }
     */

    @Inject(method = "aiStep", at = @At("RETURN"), cancellable = true)
    private void redirectApplyInput1(CallbackInfo ci) {
        if (Minecraft.getInstance().player != null
                && ((LivingEntity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {

        }
    }

    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void overrideStepHeight(CallbackInfoReturnable<Float> cir) {
        // Применяем только к игроку
        if (Minecraft.getInstance().player != null
                && ((LivingEntity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {
            if (Client.instance.featureManager.getModuleByClass(Step.class).getState()) {
                cir.setReturnValue(Step.stepHeight);
            }
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void inject1(CallbackInfo cir) {
        if (Minecraft.getInstance().player != null
                && ((LivingEntity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {
            yaw = ((LivingEntity) (Object) this).getYRot();
            prevyaw = ((LivingEntity) (Object) this).getYRot();

            if (Client.instance.featureManager.getModuleByClass(Sprint.class).getState()
                    && Sprint.Mode.getCurrentMode().equals("AllDir") && Sprint.acceptAllDir()

            ) {
                yaw = MoveUtil.getdir();
            }

            EventOnJump event = new EventOnJump(
                    (Client.instance.featureManager.getModuleByClass(Disabler.class).getState()
                            && Disabler.Mode.getCurrentMode().equals("Matrix")) ? MoveUtil.getCurrMoveAngle() : yaw);
            EventManager.call(event);
            if (event.isCanceled()) {
                cir.cancel();
                return;
            }

            yaw = event.getYaw();

            yaw = MoveFix.getfixedyaw(yaw);

            ((LivingEntity) (Object) this).setYRot(event.getYaw());
        }
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void inject2(CallbackInfo cir) {
        if (Minecraft.getInstance().player != null
                && ((LivingEntity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {
            ((LivingEntity) (Object) this).setYRot(prevyaw);
        }
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (Client.instance.featureManager.getModuleByClass(Optimizator.class).getState()
                && Optimizator.GlowCancel.getState()) {
            cir.setReturnValue(false);
        }

    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void inject3(boolean sprinting, CallbackInfo cir) {
        if (Minecraft.getInstance().player != null
                && ((LivingEntity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {
            if (cancelsprintevent) {
                cancelsprintevent = false;
                return;
            }

            EventSprint event = new EventSprint(sprinting);
            EventManager.call(event);
            if (event.isCancelled()) {
                cir.cancel();
                return;
            }

            if (Client.instance.featureManager.getModuleByClass(MoveFix.class).getState() && MoveFix.needspoofyaw
                    && !MoveFix.lastcansprint) {
                event.setSprint(false);

            }
            if (event.isSprint() != sprinting) {
                cancelsprintevent = true;
                Minecraft.getInstance().player.setSprinting(event.isSprint());
                cancelsprintevent = false;
                cir.cancel();
            }

        }
    }

}
