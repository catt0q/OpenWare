package base.mixin.client.accessors;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface GuiInvoker {
    @Invoker("renderTextureOverlay")
    void invokeRenderTextureOverlay(GuiGraphics guiGraphics, Identifier texture, float opacity);
}
