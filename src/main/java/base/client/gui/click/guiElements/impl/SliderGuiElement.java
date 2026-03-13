package base.client.gui.click.guiElements.impl;

import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.gui.click.GuiHelper;
import base.client.gui.click.guiElements.GuiElement;
import base.client.helpers.impl.math.MathematicHelper;
import base.client.helpers.impl.render.FontHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.util.Mth;

import static base.client.gui.click.GuiHelper.bgColor2;
import static base.client.gui.click.GuiHelper.color;

public class SliderGuiElement extends GuiElement {

    NumberSetting setting;
    public boolean dragging = false;

    public SliderGuiElement(NumberSetting setting) {
        this.setting = setting;
        height = 16;
    }

    float lerp = 0;
    private int colorIndex = 0;

    public void setColorIndex(int index) {
        this.colorIndex = index;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        float amount = Math.min(
                (setting.getValue() - setting.getMinValue()) / (setting.getMaxValue() - setting.getMinValue()), 1.1f);
        lerp = GuiHelper.lerp(lerp, amount, 0.15f);
        if (dragging) {
            float value = MathematicHelper.round(
                    (mouseX - x) / width * (setting.getMaxValue() - setting.getMinValue()) + setting.getMinValue(),
                    setting.getIncrement());
            value = Mth.clamp(value, setting.getMinValue(), setting.getMaxValue());
            setting.setValueNumber(value);
        }
        setting.setValueNumber(Mth.clamp(setting.getValue(), setting.getMinValue(), setting.getMaxValue()));
        // background
        GuiHelper.rectFilledRounded(graphics, x + 2, y + 1, width - 4, height - 2, GuiHelper.BORDER_RADIUS,
                GuiHelper.applyOpacity(bgColor2, alpha * 255f));
        // filled portion
        float fillWidth = Math.max((width - 4) * lerp, GuiHelper.BORDER_RADIUS * 2);
        GuiHelper.rectFilledRounded(graphics, x + 2, y + 1, fillWidth, height - 2, GuiHelper.BORDER_RADIUS,
                GuiHelper.applyOpacity(0xFF3A3A3A, alpha * 255f));
        renderScrollingString(graphics,
                setting.getName() + ": "
                        + ((setting.getValue() + "").endsWith(".0") ? (int) setting.getValue() : setting.getValue())
                        + setting.getType().getName(),
                FontHelper.getFont(), (int) x + 6, (int) y, 0, GuiHelper.applyOpacity(-1, alpha * 255f));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (GuiHelper.isHovering(x, y, width, height, mouseX, mouseY)) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}
