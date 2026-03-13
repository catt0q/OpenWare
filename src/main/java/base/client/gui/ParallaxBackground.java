package base.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * shared parallax background renderer for menu screens
 */
public class ParallaxBackground {

    private static final Identifier WHATSAPP_CAMEL = Identifier.fromNamespaceAndPath("quantum",
            "textures/gui/pizdec.png");
    private static final float WHATSAPP_CAMEL_ASPECT = 1280.0f / 720.0f;

    private static final Identifier EPSTEIN_ISLAND = Identifier.fromNamespaceAndPath("quantum",
            "textures/gui/epstein_island.png");
    private static final float EPSTEIN_ISLAND_ASPECT = 5184.0f / 3456.0f;

    private static final float PARALLAX_SCALE = 1.15f;
    private static final float PARALLAX_STRENGTH = 0.4f;

    /**
     * renders the parallax background
     * 
     * @return true if background was rendered and original should be cancelled
     */
    public static boolean render(GuiGraphics graphics, int mouseX, int mouseY) {
        // Menu background is locked to menu_background.png (no HUD option)
        Identifier texture = WHATSAPP_CAMEL;
        float imgAspect = WHATSAPP_CAMEL_ASPECT;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float screenAspect = (float) screenWidth / screenHeight;

        int baseWidth, baseHeight;
        if (screenAspect > imgAspect) {
            baseWidth = screenWidth;
            baseHeight = (int) (screenWidth / imgAspect);
        } else {
            baseHeight = screenHeight;
            baseWidth = (int) (screenHeight * imgAspect);
        }

        int drawWidth = (int) (baseWidth * PARALLAX_SCALE);
        int drawHeight = (int) (baseHeight * PARALLAX_SCALE);

        float normalizedMouseX = (mouseX - screenWidth / 2f) / (screenWidth / 2f);
        float normalizedMouseY = (mouseY - screenHeight / 2f) / (screenHeight / 2f);

        int maxOffsetX = (drawWidth - screenWidth) / 2;
        int maxOffsetY = (drawHeight - screenHeight) / 2;

        int offsetX = (int) (-normalizedMouseX * maxOffsetX * PARALLAX_STRENGTH) - (drawWidth - screenWidth) / 2;
        int offsetY = (int) (-normalizedMouseY * maxOffsetY * PARALLAX_STRENGTH) - (drawHeight - screenHeight) / 2;

        graphics.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                offsetX, offsetY, 0f, 0f, drawWidth, drawHeight, drawWidth, drawHeight, 0xFFFFFFFF);

        return true;
    }
}
