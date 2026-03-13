package base.mixin.client;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.impl.visual.Brightness;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class LightTextureMixin {


    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Options;gamma()Lnet/minecraft/client/OptionInstance;"
            )
    )
    private OptionInstance<Double> forceGamma(Options instance) {
        Module nr = Client.instance.featureManager.getModuleByClass(Brightness.class);

        boolean bll = (nr != null && nr.getState());
        if (bll) {
            return new OptionInstance<>(
                    "options.gamma",
                    OptionInstance.cachedConstantTooltip(Component.translatable("options.gamma")),
                    (text, value) -> text.copy().append(": ").append(String.format("%.1f", 999.0)),
                    OptionInstance.UnitDouble.INSTANCE,
                    999.0,
                    value -> {}
            );
        }
        return instance.gamma();

    }


}