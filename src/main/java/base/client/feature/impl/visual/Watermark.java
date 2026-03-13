package base.client.feature.impl.visual;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.impl.render.RectHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;


public class Watermark extends Module {

        public Watermark() {
                super("Watermark", "Ватермарка клиента", Type.Visuals);
        }

        @EventTarget
        public void onRenderGui(EventRenderGui e) {
                GuiGraphics dc = e.dc();

                // Render image watermark in the top-left corner (5,5) as a small icon
                int x = 5;
                int y = 5;
                int imgW = 128;
                int imgH = 89;

                // Use Identifier.fromNamespaceAndPath to load the texture
                Identifier texture = Identifier.fromNamespaceAndPath("quantum", "textures/gui/sprite.png");
                // Draw the watermark image; render at fixed size without full-screen usage
                dc.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, imgW, imgH, imgW, imgH, 0xFFFFFFFF);
        }
}
