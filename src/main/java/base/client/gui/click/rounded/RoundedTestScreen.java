package base.client.gui.click.rounded;

import base.client.helpers.Helper;
import base.client.helpers.impl.render.RectHelper;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.awt.*;

import static base.client.gui.click.GuiHelper.*;

public class RoundedTestScreen extends Screen implements Helper {

    private static final float GUI_WIDTH = 400;
    private static final float GUI_HEIGHT = 300;
    private static final float CORNER_RADIUS = 12;

    private float anim = 0;
    private float anim2 = 0;
    private boolean closing = false;

    public RoundedTestScreen() {
        super(Component.literal("Rounded Corners Test"));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        // Render blur background
        renderBlurredBackground();
    }

    private void renderBlurredBackground() {
        // Use Minecraft's built-in blur effect
        if (mc.level != null) {
            // Apply blur shader - this is the vanilla 1.21 blur
            mc.gameRenderer.processBlurEffect();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Animate open/close
        anim = lerp(anim, closing ? 0 : 1, closing ? 0.15f : 0.2f);
        anim2 = lerp(anim2, closing ? 0.8f : 1, closing ? 0.15f : 0.2f);

        if (closing && anim <= 0.05f) {
            closing = false;
            anim = anim2 = 0;
            mc.setScreen(null);
            return;
        }

        // Render blur first
        renderBlurredBackground();

        Window window = mc.getWindow();
        float centerX = window.getGuiScaledWidth() / 2f;
        float centerY = window.getGuiScaledHeight() / 2f;

        float x = centerX - GUI_WIDTH / 2f;
        float y = centerY - GUI_HEIGHT / 2f;

        // Apply scale animation
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(centerX, centerY);
        guiGraphics.pose().scale(anim2);
        guiGraphics.pose().translate(-centerX, -centerY);

        // Draw rounded rectangle background
        int bgAlpha = (int) (anim * 240);
        int bgColor = new Color(15, 15, 18, bgAlpha).getRGB();

        RectHelper.drawRoundedRect(guiGraphics, x, y, GUI_WIDTH, GUI_HEIGHT, CORNER_RADIUS, bgColor);

        // Draw accent border using global color
        int accentColor = applyOpacity(getGlobalColor(0), anim * 255f);
        RectHelper.drawRoundedOutline(guiGraphics, x, y, GUI_WIDTH, GUI_HEIGHT, CORNER_RADIUS, 2f, accentColor);

        // Draw header area
        int headerBg = new Color(28, 28, 32, bgAlpha).getRGB();
        RectHelper.drawRoundedRect(guiGraphics, x + 2, y + 2, GUI_WIDTH - 4, 40, CORNER_RADIUS - 2, headerBg);

        // Draw title
        String title = "Rounded Corners Test";
        int titleColor = applyOpacity(0xFFFFFFFF, anim * 255f);
        int titleX = (int) (centerX - mc.font.width(title) / 2f);
        int titleY = (int) (y + 15);
        guiGraphics.drawString(mc.font, title, titleX, titleY, titleColor, false);

        // Draw some content boxes with rounded corners
        float contentY = y + 60;
        float boxWidth = (GUI_WIDTH - 30) / 2;
        float boxHeight = 80;

        // Left box
        int boxColor = new Color(35, 35, 40, bgAlpha).getRGB();
        RectHelper.drawRoundedRect(guiGraphics, x + 10, contentY, boxWidth, boxHeight, 8, boxColor);

        // Right box
        RectHelper.drawRoundedRect(guiGraphics, x + 20 + boxWidth, contentY, boxWidth, boxHeight, 8, boxColor);

        // Bottom large box
        RectHelper.drawRoundedRect(guiGraphics, x + 10, contentY + boxHeight + 15, GUI_WIDTH - 20, 100, 8, boxColor);

        // Draw close hint
        String hint = "Press ESC to close";
        int hintColor = applyOpacity(0xFF888888, anim * 255f);
        int hintX = (int) (centerX - mc.font.width(hint) / 2f);
        int hintY = (int) (y + GUI_HEIGHT - 25);
        guiGraphics.drawString(mc.font, hint, hintX, hintY, hintColor, false);

        guiGraphics.pose().popMatrix();
    }

    @Override
    public void onClose() {
        closing = true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
