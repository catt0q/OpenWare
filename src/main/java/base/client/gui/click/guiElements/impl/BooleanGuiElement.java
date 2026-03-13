package base.client.gui.click.guiElements.impl;

import base.client.feature.settings.Setting;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.gui.click.GuiHelper;
import base.client.gui.click.guiElements.GuiElement;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.utils.ColorUtil;
import net.minecraft.client.gui.GuiGraphics;

import static base.client.gui.click.GuiHelper.*;

public class BooleanGuiElement extends GuiElement {

    BooleanSetting setting;

    public BooleanGuiElement(BooleanSetting setting) {
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
        lerp = GuiHelper.lerp(lerp, setting.getState() ? 1 : 0, 0.15f);
        // Subtle state feedback via transparency only (no extra indicator rect)
        int base = GuiHelper.applyOpacity(bgColor2, alpha * 230f);
        int accent = GuiHelper.applyOpacity(GuiHelper.interpolateColor(bgColor3, GuiHelper.getGlobalColor(colorIndex), 0.35f),
                alpha * 255f);
        GuiHelper.rectFilledRounded(graphics, x + 2, y + 1, width - 4, height - 2, GuiHelper.BORDER_RADIUS,
                GuiHelper.interpolateColor(base, accent, lerp));
        renderScrollingString(graphics, setting.getName() + ": " + setting.getState(), FontHelper.getFont(),
                (int) x + 6, (int) y, 0, GuiHelper.applyOpacity(-1, alpha * 255f));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (GuiHelper.isHovering(x, y, width, height, mouseX, mouseY)) {
            setting.setBoolValue(!setting.getState());
        }
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}
