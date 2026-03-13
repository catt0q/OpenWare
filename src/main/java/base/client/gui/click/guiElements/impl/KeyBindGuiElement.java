package base.client.gui.click.guiElements.impl;

import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.KeyBindSetting;
import base.client.gui.click.ClickGuiScreen;
import base.client.gui.click.GuiHelper;
import base.client.gui.click.guiElements.GuiElement;
import base.client.helpers.impl.render.FontHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import static base.client.gui.click.GuiHelper.bgColor2;
import static base.client.gui.click.GuiHelper.color;

public class KeyBindGuiElement extends GuiElement {

    KeyBindSetting setting;

    public KeyBindGuiElement(KeyBindSetting setting) {
        this.setting = setting;
        height = 16;
    }

    float lerp = 0;
    boolean binding = false;
    private int colorIndex = 0;

    public void setColorIndex(int index) {
        this.colorIndex = index;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        lerp = GuiHelper.lerp(lerp, binding ? 1 : 0, 0.15f);
        GuiHelper.rectFilledRounded(graphics, x + 2, y + 1, width - 4, height - 2, GuiHelper.BORDER_RADIUS,
                GuiHelper.applyOpacity(GuiHelper.interpolateColor(bgColor2, 0xFF3A3A3A, lerp), alpha * 255f));
        String k = GuiHelper.getKeyName(setting.getKeyCode());
        renderScrollingString(graphics,
                setting.getName() + ": " + (binding ? "Binding... [" : "") + (k.startsWith(" ") ? k.substring(1) : k)
                        + (binding ? "]" : ""),
                FontHelper.getFont(), (int) x + 6, (int) y, 0, GuiHelper.applyOpacity(-1, alpha * 255f));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (GuiHelper.isHovering(x, y, width, height, mouseX, mouseY)) {
            binding = !binding;
        }
    }

    @Override
    public void keyPressed(int key) {
        if (binding) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
                setting.setKeyCode(-1);
                NotificationManager.publicity("ClickGui", "Setting " + setting.getName() + " was unbounded", 3,
                        NotificationType.INFO);
            } else {
                setting.setKeyCode(key);
                NotificationManager.publicity("ClickGui",
                        "Setting " + setting.getName() + " was bound to key "
                                + GuiHelper.getKeyName(key)
                                        .substring(GuiHelper.getKeyName(key).startsWith(" ") ? 1 : 0),
                        3, NotificationType.INFO);
            }
            binding = false;
        }
        super.keyPressed(key);
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}
