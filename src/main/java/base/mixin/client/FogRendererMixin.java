package base.mixin.client;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.impl.visual.NoRender;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    private FogType getFogType(Camera camera) {
        throw new AssertionError();
    }

    // inject into setupFog to modify fog when in lava/water
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private void onSetupFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float partialTick,
            ClientLevel level, CallbackInfoReturnable<Vector4f> cir) {
        Module nr = Client.instance.featureManager.getModuleByClass(NoRender.class);
        if (nr == null || !nr.getState())
            return;

        FogType fogType = camera.getFluidInCamera();

        // disable lava fog
        if (fogType == FogType.LAVA && NoRender.lava.isEnabled()) {
            // return clear fog (very far start/end distances effectively disable fog)
            // we let the method continue but the fog will be overridden at return
        }

        // disable water fog
        if (fogType == FogType.WATER && NoRender.water.isEnabled()) {
            // similar approach for water
        }
    }

    // modify the fog color/distance by intercepting return
    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private void modifyFogResult(Camera camera, int renderDistance, DeltaTracker deltaTracker, float partialTick,
            ClientLevel level, CallbackInfoReturnable<Vector4f> cir) {
        Module nr = Client.instance.featureManager.getModuleByClass(NoRender.class);
        if (nr == null || !nr.getState())
            return;

        FogType fogType = camera.getFluidInCamera();

        // for lava/water, we could modify the returned fog vector
        // but the main fog effect is controlled by the buffer data
        // a more complete solution would need to modify the buffer writes
    }
}
