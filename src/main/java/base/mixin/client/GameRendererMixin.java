package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.render.EventRenderWorld;
import base.client.feature.Module;
import base.client.feature.impl.visual.NoRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private DeltaTracker renderTickCounter;

    @Inject(method = "render", at = @At("HEAD"))
    private void setDeltaTracker(DeltaTracker renderTickCounter, boolean b, CallbackInfo ci) {
        this.renderTickCounter = renderTickCounter;
    }

    // renderLevel signature changed in 1.21.11 - no longer has Matrix4f local
    // variables accessible
    @Inject(method = "renderLevel", at = @At(value = "TAIL"))
    private void onRenderLevelEnd(DeltaTracker deltaTracker, CallbackInfo ci) {
        PoseStack matrixStack = new PoseStack();
        // create identity matrix since we can't access the projection matrix here
        EventManager.call(new EventRenderWorld(matrixStack, renderTickCounter));
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        Module nr = Client.instance.featureManager.getModuleByClass(NoRender.class);
        if (nr != null && nr.getState() && NoRender.hurt.isEnabled()) {
            ci.cancel();
        }
    }

}
