package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.game.EventOnSprint;
import base.client.event.events.impl.game.EventOnStopUsingItem;
import base.client.event.events.impl.motion.*;
import base.client.event.events.impl.packet.EventSendSprintUpdatePacket;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.FastSneak;
import base.client.feature.impl.movement.NoSlow;
import base.client.feature.impl.movement.Sprint;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.player.LocalPlayer.modifyInputSpeedForSquareMovement;

@Mixin(value = LocalPlayer.class, priority = 2000)
public abstract class LocalPlayerMixin {

    @Shadow
    public abstract boolean isUsingItem();

    float yaw = 0, pitch = 0;
    double posx = 0, posy = 0, posz = 0;
    boolean onground = false, colhor = false, wassprint = false;

    private static final Identifier SNEAKING_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath("minecraft",
            "sneakingsp");
    private static final AttributeModifier SNEAKING_SPEED_BOOST = new AttributeModifier(
            SNEAKING_SPEED_MODIFIER_ID, 0.3F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void sendMovementPackets(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        yaw = player.getYRot();
        pitch = player.getXRot();
        posx = player.getX();
        posy = player.getY();
        posz = player.getZ();
        onground = player.onGround();
        colhor = player.horizontalCollision;
        EventPreMotion eventPre = new EventPreMotion(player.getX(), player.getY(), player.getZ(), player.getYRot(),
                player.getXRot(), player.onGround(), player.horizontalCollision);
        EventManager.call(eventPre);

        // If event is cancelled, don't send position packet
        if (eventPre.isCancelled()) {
            ci.cancel();
            return;
        }

        EventMovePreModify eventPrepost = new EventMovePreModify();
        EventManager.call(eventPrepost);

        player.setYRot(eventPre.getYaw());
        player.setXRot(eventPre.getPitch());
        player.setPos(eventPre.getPosX(), eventPre.getPosY(), eventPre.getPosZ());
        player.setOnGround(eventPre.isOnGround());
        player.horizontalCollision = eventPre.isHorcol();
    }

    @Inject(at = @At("TAIL"), method = "sendPosition")
    private void injectEventPostMotion(CallbackInfo info) {

        LocalPlayer player = Minecraft.getInstance().player;

        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setPos(posx, posy, posz);
        player.setOnGround(onground);
        player.horizontalCollision = colhor;

        EventPostMotion eventPost = new EventPostMotion();
        EventManager.call(eventPost);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;", shift = At.Shift.BEFORE))
    private void beforeJumpableVehicleCheck(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        EventOnSprint evs = new EventOnSprint(player.isSprinting());
        EventManager.call(evs);

        player.setSprinting(evs.issprint());
    }

    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
    private void onMoveTowardsClosestSpace(double d, double e, CallbackInfo ci) {
        EventPushOutOfBlocks evs = new EventPushOutOfBlocks();
        EventManager.call(evs);
        if (evs.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyInput", at = @At("HEAD"), cancellable = true)
    private void onApplyInput(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        if (Minecraft.getInstance().getCameraEntity() == player) {
            float f1 = KeyboardInput.calculateImpulse(player.input.keyPresses.forward(),
                    player.input.keyPresses.backward());
            float g1 = KeyboardInput.calculateImpulse(player.input.keyPresses.left(), player.input.keyPresses.right());

            Vec2 vec2 = new Vec2(0, 0);

            if ((Client.instance.featureManager.getModuleByClass(Disabler.class).getState()
                    && Disabler.Mode.getCurrentMode().equals("Matrix"))) {
                vec2 = new Vec2(g1, f1).normalized().scale(0.98F);
                if (player.isUsingItem() && player.getVehicle() == null) {
                    EventOnSlow event = new EventOnSlow(0.2F);
                    EventManager.call(event);
                    vec2 = vec2.scale(event.mult);
                }
                if (player.isMovingSlowly()) {
                    float f = Client.instance.featureManager.getModuleByClass(FastSneak.class).getState()
                            ? FastSneak.mult.getValue()
                            : (float) player.getAttributeValue(Attributes.SNEAKING_SPEED);
                    vec2 = vec2.scale(f);
                }

                player.xxa = vec2.x;
                player.zza = vec2.y;
                player.setJumping(player.input.keyPresses.jump());
                player.yBobO = player.yBob;
                player.xBobO = player.xBob;
                player.xBob = player.xBob + (player.getXRot() - player.xBob) * 0.5F;
                player.yBob = player.yBob + (player.getYRot() - player.yBob) * 0.5F;

                ci.cancel();
            } else {
                /*
                 * vec2 = player.modifyInput(player.input.getMoveVector());
                 * player.xxa = vec2.x;
                 * player.zza = vec2.y;
                 * player.setJumping(player.input.keyPresses.jump());
                 * player.yBobO = player.yBob;
                 * player.xBobO = player.xBob;
                 * player.xBob = player.xBob + (player.getXRot() - player.xBob) * 0.5F;
                 * player.yBob = player.yBob + (player.getYRot() - player.yBob) * 0.5F;
                 */
            }

        } else {
            player.xxa = 0.98F;
            player.zza = 0.98F;
            ci.cancel();
        }
        // }

    }

    public final Vec2 custommodifyInput(Vec2 vec2) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (vec2.lengthSquared() == 0.0F) {
            return vec2;
        } else {
            vec2 = vec2.scale(0.98F);
            if (this.isUsingItem() && !player.isPassenger()) {
                EventOnSlow event = new EventOnSlow(0.2F);
                EventManager.call(event);
                vec2 = vec2.scale(event.mult);
            }

            if (player.isMovingSlowly()) {
                float f = Client.instance.featureManager.getModuleByClass(FastSneak.class).getState()
                        ? FastSneak.mult.getValue()
                        : (float) player.getAttributeValue(Attributes.SNEAKING_SPEED);
                vec2 = vec2.scale(f);
            }

            return modifyInputSpeedForSquareMovement(vec2);
        }
    }

    @Inject(method = "stopUsingItem()V", at = @At(value = "HEAD"))
    private void injectHelloAtStart(CallbackInfo ci) {
        EventOnStopUsingItem event = new EventOnStopUsingItem();
        EventManager.call(event);
    }

    @ModifyArg(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal = 1), index = 0)
    private float modifyUsingItemScale(float original) {
        EventOnSlow event = new EventOnSlow(0.2F);
        EventManager.call(event);
        return event.mult;

    }

    // todo: 0.2F constant may not exist in modifyInput in 1.21.11
    // @ModifyConstant(method =
    // "modifyInput(Lnet/minecraft/world/phys/Vec2;)Lnet/minecraft/world/phys/Vec2;",
    // constant = @Constant(floatValue = 0.2F))
    // private float modifyItemUseSlowdown(float original) {
    // EventOnSlow event = new EventOnSlow(0.2F);
    // EventManager.call(event);
    // return event.mult;
    // }

    @ModifyVariable(method = "modifyInput", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private float modifySneakingSpeedValue(float original) {
        return Client.instance.featureManager.getModuleByClass(FastSneak.class).getState() ? FastSneak.mult.getValue()
                : (float) ((LocalPlayer) (Object) this).getAttributeValue(Attributes.SNEAKING_SPEED);
    }

    /*
     * @ModifyVariable(
     * method = "modifyInput",
     * at = @At(
     * value = "STORE",
     * ordinal = 0
     * ),
     * name = "f"
     * )
     * private float modifySneakingSpeed(float original) {
     * return
     * Client.instance.featureManager.getModuleByClass(FastSneak.class).getState() ?
     * FastSneak.mult.getValue() : (float)((LocalPlayer) (Object)
     * this).getAttributeValue(Attributes.SNEAKING_SPEED);
     * }
     */

    @Inject(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V", shift = At.Shift.BEFORE))
    private void beforeSprintCheck(CallbackInfo ci) {
        wassprint = ((LocalPlayer) (Object) this).isSprinting();
    }

    @Inject(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V", shift = At.Shift.AFTER))
    private void afterSprintCheck(CallbackInfo ci) {
        if (!wassprint)
            return;
        LocalPlayer player = ((LocalPlayer) (Object) this);
        boolean nosprint = (Client.instance.featureManager.getModuleByClass(Sprint.class).getState()
                && Sprint.Mode.getCurrentMode().equals("NoSprint"));

        if (nosprint) {
            player.setSprinting(false);
        } else {
            boolean fastsneak = (Client.instance.featureManager.getModuleByClass(FastSneak.class).getState()
                    && MoveUtil.getspeed2() > 0.03);
            boolean isnoslow = (Client.instance.featureManager.getModuleByClass(NoSlow.class).getState()
                    && NoSlow.AllowSprint.isEnabled() && MoveUtil.getspeed2() > 0.03);

            player.setSprinting(!(player.isFallFlying()
                    || (player.hasEffect(MobEffects.BLINDNESS) || Sprint.canignoreBlind())
                    || (player.isMovingSlowly() && !fastsneak)
                    || player.isVehicle()
                            && !(player.getVehicle() != null && player.getVehicle().getType() == EntityType.CAMEL)
                    || (player.isUsingItem() && !isnoslow && !player.isVehicle() && !player.isUnderWater())
                    || !(Sprint.canusesprint())
                    || !(player.isPassenger()
                            || ((float) player.getFoodData().getFoodLevel() > 6.0F || Sprint.canignoreFood())
                            || player.getAbilities().mayfly)
                    || player.horizontalCollision && !player.minorHorizontalCollision));
        }
    }

    @Inject(method = "sendIsSprintingIfNeeded", at = @At("HEAD"), cancellable = true)
    public void sprintPacketEvent(CallbackInfo ci) {
        EventSendSprintUpdatePacket event = new EventSendSprintUpdatePacket();
        EventManager.call(event);
        if (event.isCanceled())
            ci.cancel();
    }

}
