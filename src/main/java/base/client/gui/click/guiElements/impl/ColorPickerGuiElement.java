package base.client.gui.click.guiElements.impl;

import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ColorSetting;
import base.client.gui.click.GuiHelper;
import base.client.gui.click.guiElements.GuiElement;
import base.client.helpers.impl.render.FontHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.awt.*;

import static base.client.gui.click.GuiHelper.bgColor2;
import static base.client.gui.click.GuiHelper.color;

public class ColorPickerGuiElement extends GuiElement {

    ColorSetting setting;
    public boolean hover = false;
    public boolean hoverHue = false;

    public ColorPickerGuiElement(ColorSetting setting) {
        this.setting = setting;
        height = 16;
        float[] hueta = Color.RGBtoHSB(GuiHelper.getRed(setting.getColorValue()),
                GuiHelper.getGreen(setting.getColorValue()), GuiHelper.getBlue(setting.getColorValue()), null);
        hue = hueta[0];
        saturation = hueta[1];
        brightness = hueta[2];
    }

    float brightness, saturation, hue;

    float lerpBrightness = 0, lerpSaturation = 0, lerpHue;
    private int colorIndex = 0;

    public void setColorIndex(int index) {
        this.colorIndex = index;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        height = 20 + 70;
        GuiHelper.rectFilledRounded(graphics, x + 2, y + 1, width - 4, height - 2, GuiHelper.BORDER_RADIUS,
                GuiHelper.applyOpacity(bgColor2, alpha * 255f));
        renderScrollingString(graphics, setting.getName(), FontHelper.getFont(), (int) x + 6, (int) y - 17 * 2 - 3, 0,
                GuiHelper.applyOpacity(-1, alpha * 255f));
        GuiHelper.rectFilledRounded(graphics, x + width - 13, y + 4, 10, 10, 2f, setting.getColorValue());
        GuiHelper.rectFilledGradient(graphics, x + 4, y + 16, width - 20, 66,
                GuiHelper.applyOpacity(-1, alpha * 255f),
                GuiHelper.applyOpacity(GuiHelper.rgba(0, 0, 0, 255), alpha * 255f),
                GuiHelper.applyOpacity(GuiHelper.rgba(0, 0, 0, 255), alpha * 255f),
                GuiHelper.applyOpacity(Color.HSBtoRGB(hue, 1, 1), alpha * 255f));
        for (int i = 0; i < 10; i++) {
            graphics.fillGradient((int) (x + width - 13), (int) y + i * 7 + 16, (int) (10 + x + width - 13),
                    (int) (7 + 16 + y + i * 7),
                    GuiHelper.applyOpacity(Color.HSBtoRGB(i / 10f, 1, 1), alpha * 255f),
                    GuiHelper.applyOpacity(Color.HSBtoRGB(i / 10f + 0.1f, 1, 1), alpha * 255f));
        }
        GuiHelper.rectFilled(graphics, x + width - 14, y + 16 + 70 * hue, 12, 1,
                GuiHelper.applyOpacity(-1, alpha * 255f));
        GuiHelper.rectFilled(graphics, x + 4 + (width - 20) * saturation - 1, y + 16 - 66 * brightness - 1 + 66, 2, 2,
                GuiHelper.applyOpacity(-1, alpha * 255f));
        if (hover) {
            saturation = (mouseX - x) / (width - 14);
            brightness = 1 - (mouseY - y - 16) / 70;
        } else if (hoverHue) {
            hue = (mouseY - y - 16) / 70;
        }
        hue = Mth.clamp(hue, 0, 1);
        saturation = Mth.clamp(saturation, 0, 1);
        brightness = Mth.clamp(brightness, 0, 1);
        setting.setColorValue(Color.HSBtoRGB(hue, saturation, brightness));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (GuiHelper.isHovering(x, y + 16, width - 14, 70, mouseX, mouseY)) {
            hover = true;
        }
        if (GuiHelper.isHovering(x + width - 11, y + 16, 10, 70, mouseX, mouseY)) {
            hoverHue = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        hover = false;
        hoverHue = false;
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}
