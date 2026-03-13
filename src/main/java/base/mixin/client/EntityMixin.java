package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.game.EventLook;
import base.client.event.events.impl.motion.EventCollideEntity;
import base.client.event.events.impl.motion.EventMove;
import base.client.event.events.impl.motion.EventOnMove;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventStep;
import base.client.feature.impl.exploit.Blink;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.MoveFix;
import base.client.feature.impl.movement.NoClip;
import base.client.feature.impl.player.FreeCam;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static base.client.feature.impl.exploit.Disabler.notimerbalance;
import static base.client.feature.impl.exploit.Disabler.savedabusepacket;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Redirect(method = "moveRelative", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 redirectMovementInputToVelocity(Vec3 movementInput, float speed, float yaw) {

        if (Minecraft.getInstance().player != null
                && ((Entity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {

            EventOnMove event = new EventOnMove(
                    (Client.instance.featureManager.getModuleByClass(Disabler.class).getState()
                            && Disabler.Mode.getCurrentMode().equals("Matrix")) ? MoveUtil.getCurrMoveAngle() : yaw,
                    speed);
            EventManager.call(event);
            yaw = event.getYaw();
            speed = event.getSpeed();

        }

        return Entity.getInputVector(movementInput, speed, yaw);
    }

    @Inject(method = "collide", at = @At("HEAD"))
    private void modifyCollisionForHigherStep(Vec3 vec3, CallbackInfoReturnable<Vec3> cir) {
        if (Minecraft.getInstance().player != null
                && ((Entity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {

            EventStep event = new EventStep();
            EventManager.call(event);
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMove(MoverType moverType, Vec3 vec3, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null
                && ((Entity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {
            // fire EventMove for modules to handle
            EventMove event = new EventMove(vec3);
            EventManager.call(event);

            if (event.isCancelled()) {
                ci.cancel();
                return;
            }

            // handle safeWalk - stop at edges
            if (event.isSafeWalk()) {
                // safeWalk is handled by maintaining position at edges
            }

            // noclip handling
            if (MoveUtil.NoCliping) {
                Minecraft.getInstance().player.noPhysics = true;
            }
        }
    }

    @Inject(method = "moveRelative", at = @At("TAIL"))
    private void inject2(float speed, Vec3 movementInput, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null
                && ((Entity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {
            EventOnMovePost event = new EventOnMovePost();
            EventManager.call(event);

            if (Client.instance.featureManager.getModuleByClass(Disabler.class).getState() && Disabler.MTNS.isVisible()
                    && Disabler.MTNS.isEnabled() && (TimerUtil.getTimerspeed() > 1 || savedabusepacket < 0)
                    && notimerbalance()) {
                MoveUtil.limit2speed(0.1995);
                if (TimerUtil.getTimerspeed() > 1) {
                    savedabusepacket = -2;
                }
            }

        }
    }

    @Inject(method = "push", at = @At("HEAD"), cancellable = true)
    private void onPush(Entity entity, CallbackInfo ci) {
        EventCollideEntity event = new EventCollideEntity();
        EventManager.call(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "setOnGroundWithMovement(ZZLnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    private void hookPlayerUtil(boolean onGround, boolean horizontalCollision, Vec3 movement, CallbackInfo ci) {
        if (!((Object) this instanceof LocalPlayer))
            return;
        if (onGround || movement.y >= 0) {
            CombatUtil.fallDistance = 0;
        } else {
            CombatUtil.fallDistance -= (float) movement.y;
        }
        if (onGround) {
            CombatUtil.ofallDistance = 0;
        } else if (movement.y <= 0) {
            CombatUtil.ofallDistance -= (float) movement.y;
        }
        if (((LocalPlayer) (Object) this).isFallFlying()) {
            CombatUtil.resetfd();
        }

    }

    @Inject(method = "calculateViewVector", at = @At("HEAD"), cancellable = true)
    private void inject2(float pitch, float yaw, CallbackInfoReturnable<Vec3> cir) {

        if (Minecraft.getInstance().player != null
                && ((Entity) (Object) this).getId() == Minecraft.getInstance().player.getId()) {
            EventLook event = new EventLook(yaw, pitch);
            EventManager.call(event);
            yaw = event.getYaw();
            pitch = event.getPitch();
        }

        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        cir.setReturnValue(new Vec3((double) (i * j), (double) (-k), (double) (h * j)));

    }

}