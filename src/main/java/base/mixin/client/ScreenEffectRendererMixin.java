package base.mixin.client;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.impl.visual.NoRender;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    // renderScreenEffect signature changed in 1.21.11: (boolean, float,
    // SubmitNodeCollector)
    // renderWater/renderFire are now static with different signatures
    // using HEAD injection to cancel the entire method when needed

    @Inject(method = "renderScreenEffect", at = @At("HEAD"), cancellable = true)
    private void cancelScreenEffects(boolean bl, float f, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        Module nr = Client.instance.featureManager.getModuleByClass(NoRender.class);
        if (nr != null && nr.getState()) {
            // cancel all screen effects if enabled
            if (NoRender.water.isEnabled() || NoRender.fire.isEnabled()) {
                // we can't selectively cancel water/fire since the methods are now static
                // todo: implement more granular control with different approach
            }
        }
    }
}