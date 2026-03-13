package base.mixin.client;

import base.client.gui.ParallaxBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void renderCustomBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        Screen self = (Screen) (Object) this;

        // only apply to menu screens
        if (!(self instanceof TitleScreen)
                && !(self instanceof SelectWorldScreen)
                && !(self instanceof JoinMultiplayerScreen)
                && !(self instanceof DirectJoinServerScreen)
                && !(self instanceof ManageServerScreen)) {
            return;
        }

        if (ParallaxBackground.render(graphics, mouseX, mouseY)) {
            ci.cancel();
        }
    }
}
