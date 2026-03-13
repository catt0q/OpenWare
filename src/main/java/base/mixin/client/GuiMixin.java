package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.feature.Module;
import base.client.feature.impl.visual.NoRender;
import base.mixin.client.accessors.GuiInvoker;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    private static final Minecraft mc = Minecraft.getInstance();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderHudHook(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        // prevent crash during server transfer when player is null
        if (mc.player != null) {
            EventManager.call(new EventRenderGui(context, tickCounter));
        }
    }

    @Redirect(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;F)V", ordinal = 0))
    private void cancelPumpkinOverlay(Gui instance, GuiGraphics guiGraphics, Identifier texture, float opacity) {
        Module nr = Client.instance.featureManager.getModuleByClass(NoRender.class);
        if (!(texture.getPath().contains("pumpkinblur")
                && (nr != null && nr.getState() && NoRender.pumpkin.isEnabled()))) {
            // use invoker to call private method
            ((GuiInvoker) instance).invokeRenderTextureOverlay(guiGraphics, texture, opacity);
        }
    }

}
