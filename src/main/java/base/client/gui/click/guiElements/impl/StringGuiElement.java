package base.client.gui.click.guiElements.impl;

import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.StringSetting;
import base.client.gui.click.GuiHelper;
import base.client.gui.click.guiElements.GuiElement;
import base.client.helpers.impl.render.FontHelper;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import static base.client.gui.click.GuiHelper.bgColor2;
import static base.client.gui.click.GuiHelper.color;

public class StringGuiElement extends GuiElement {

    StringSetting setting;

    public StringGuiElement(StringSetting setting) {
        this.setting = setting;
        height = 16 * 2f;
    }

    float lerp = 0;

    boolean type = false;
    private int colorIndex = 0;

    public void setColorIndex(int index) {
        this.colorIndex = index;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        lerp = GuiHelper.lerp(lerp, type ? 1 : 0, 0.15f);
        GuiHelper.rectFilledRounded(graphics, x + 2, y + 17, width - 4, 14, GuiHelper.BORDER_RADIUS,
                GuiHelper.applyOpacity(GuiHelper.interpolateColor(bgColor2, 0xFF3A3A3A, lerp), alpha * 255f));
        renderScrollingString(graphics, setting.getName() + ": ", FontHelper.getFont(), (int) x + 6, (int) y, 0,
                GuiHelper.applyOpacity(-1, alpha * 255f));
        renderScrollingString(graphics, setting.currentText, FontHelper.getFont(), (int) x + 6, (int) y + 16, 0,
                GuiHelper.applyOpacity(-1, alpha * 255f));
        height = 32;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (GuiHelper.isHovering(x, y, width, height, mouseX, mouseY)) {
            type = !type;
        } else
            type = false;
    }

    @Override
    public void keyPressed(int key) {
        if (type) {
            if (key == GLFW.GLFW_KEY_ENTER) {
                type = false;
            } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!setting.currentText.isEmpty()) {
                    setting.currentText = setting.currentText.substring(0, setting.currentText.length() - 1);
                }
            }
        }
    }

    @Override
    public void charTyped(char key) {
        if (type) {
            setting.currentText += key;
        }
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}
