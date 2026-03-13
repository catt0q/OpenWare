package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.input.EventKeyPress;
import base.client.event.events.impl.render.EventCameraPosUpdate;
import base.client.feature.impl.visual.CameraNoClip;
import base.client.feature.impl.visual.EntityESP;
import base.client.feature.impl.visual.PersonViewer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void onGetMaxZoom(float f, CallbackInfoReturnable<Float> cir) {
        if (Client.instance.featureManager.getModuleByClass(CameraNoClip.class).getState()) {
            cir.setReturnValue(shouldPV() ? PersonViewer.fovModifier.getValue() : f);
        }
    }

    @ModifyVariable(method = "getMaxZoom", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyDist(float dist) {
        return shouldPV() ? PersonViewer.fovModifier.getValue() : dist;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyYaw(float yaw) {
        return shouldPV() ? (yaw + PersonViewer.viewerYaw.getValue()) : yaw;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float modifyPitch(float pitch) {
        return shouldPV() ? (pitch + PersonViewer.viewerPitch.getValue()) : pitch;
    }

    @ModifyVariable(method = "setPosition(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Vec3 modifyPos(Vec3 pos) {
        EventCameraPosUpdate e = new EventCameraPosUpdate(pos);
        EventManager.call(e);
        return e.getPosition();
    }

    private boolean shouldPV() {
        return (!Minecraft.getInstance().options.getCameraType().isFirstPerson()
                && Client.instance.featureManager.getModuleByClass(PersonViewer.class).getState()

        );
    }

}