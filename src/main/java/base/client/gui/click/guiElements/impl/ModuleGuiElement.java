package base.client.gui.click.guiElements.impl;

import base.client.feature.Module;
import base.client.feature.settings.Setting;
import base.client.feature.settings.impl.*;
import base.client.gui.click.GuiHelper;
import base.client.gui.click.guiElements.GuiElement;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.impl.render.RectHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import com.sun.jna.platform.KeyboardUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static base.client.gui.click.GuiHelper.bgColor2;
import static base.client.gui.click.GuiHelper.color;

public class ModuleGuiElement extends GuiElement {

    public Module module;

    public List<GuiElement> settings = new ArrayList<>();

    public boolean expand;
    public boolean binding;

    float expandAnim = 0;
    float lerp = 0;

    private static final float CORNER_RADIUS = 6;

    public ModuleGuiElement(Module module) {
        this.module = module;

        for (Setting s : module.getSettings()) {
            if (s instanceof BooleanSetting bs) {
                settings.add(new BooleanGuiElement(bs));
            } else if (s instanceof NumberSetting ns) {
                settings.add(new SliderGuiElement(ns));
            } else if (s instanceof ModeSetting ms) {
                settings.add(new ModeGuiElement(ms));
            } else if (s instanceof StringSetting ss) {
                settings.add(new StringGuiElement(ss));
            } else if (s instanceof ColorSetting ss) {
                settings.add(new ColorPickerGuiElement(ss));
            }
        }
        height = 20;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Module background with hover effect and rounded corners
        boolean hovered = GuiHelper.isHovering(x, y, width, 24, mouseX, mouseY);
        int bgCol = hovered ? GuiHelper.applyOpacity(GuiHelper.bgColor3, alpha)
                : GuiHelper.applyOpacity(GuiHelper.bgColor2, alpha);
        RectHelper.drawRoundedRect(guiGraphics, x, y, width, 24, CORNER_RADIUS, bgCol);

        // Smooth enable animation
        lerp = GuiHelper.lerp(lerp, module.state ? 1 : 0, 0.15f);

        // Active indicator (legacy): small left stripe
        if (lerp > 0.01f) {
            int stripe = GuiHelper.applyOpacity(0xFFE6E6E6, alpha * lerp);
            RectHelper.drawRoundedRect(guiGraphics, x + 2, y + 4, 2, 16, 1.0f, stripe);
        }

        // Module text with proper padding
        String displayText = binding ? "Bind: " + GuiHelper.getKeyName(module.getBind()).trim() : module.getLabel();
        int textColor = module.state ? GuiHelper.applyOpacity(-1, alpha)
                : GuiHelper.applyOpacity(GuiHelper.textSecondary, alpha);
        FontHelper.drawString(guiGraphics, displayText, (int) x + 8, (int) (y + (24 - FontHelper.getLineHeight()) / 2),
                textColor, false);

        // Settings expansion animation
        expandAnim = GuiHelper.lerp(expandAnim, expand ? 1 : 0, 0.15f);
        float yOffset = 0;

        if (expandAnim > 0.05) {
            // Settings container background with rounded bottom
            float settingsHeight = calculateSettingsHeight();
            RectHelper.drawRoundedRect(guiGraphics, x, y + 24, width, settingsHeight * expandAnim + 4, CORNER_RADIUS,
                    GuiHelper.applyOpacity(GuiHelper.bgColor, alpha * 0.5f));

            int colorIdx = 0;
            for (GuiElement element : settings) {
                if (!element.isVisible())
                    continue;
                element.alpha = expandAnim * alpha / 255f;
                element.x = x + 6;
                element.y = y + 28 + yOffset;
                element.width = width - 12;
                element.height = 18;
                // set color index for rainbow effect
                if (element instanceof BooleanGuiElement bge) {
                    bge.setColorIndex(colorIdx);
                } else if (element instanceof SliderGuiElement sge) {
                    sge.setColorIndex(colorIdx);
                } else if (element instanceof ModeGuiElement mge) {
                    mge.setColorIndex(colorIdx);
                } else if (element instanceof KeyBindGuiElement kge) {
                    kge.setColorIndex(colorIdx);
                } else if (element instanceof StringGuiElement sge) {
                    sge.setColorIndex(colorIdx);
                } else if (element instanceof ColorPickerGuiElement cge) {
                    cge.setColorIndex(colorIdx);
                }
                element.render(guiGraphics, mouseX, mouseY);
                yOffset += (element.height + 4) * expandAnim;
                colorIdx++;
            }
            yOffset += 8;
        }

        height = 24 + yOffset;
    }

    private float calculateSettingsHeight() {
        float total = 0;
        for (GuiElement element : settings) {
            if (element.isVisible()) {
                total += 22; // 18 height + 4 spacing
            }
        }
        return total;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (expand) {
            for (GuiElement element : settings) {
                if (!element.isVisible())
                    continue;
                element.mouseClicked(mouseX, mouseY, button);
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (expand) {
            for (GuiElement element : settings) {
                if (!element.isVisible())
                    continue;
                element.mouseReleased(mouseX, mouseY, button);
            }
        }
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(int key) {
        if (binding) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
                module.setBind(-1);
                NotificationManager.publicity("ClickGui", "Module " + module.getLabel() + " was unbounded", 3,
                        NotificationType.INFO);
            } else {
                module.setBind(key);
                NotificationManager.publicity("ClickGui",
                        "Module " + module.getLabel() + " was bound to key "
                                + GuiHelper.getKeyName(key)
                                        .substring(GuiHelper.getKeyName(key).startsWith(" ") ? 1 : 0),
                        3, NotificationType.INFO);
            }
            binding = false;
        } else {
            if (expand) {
                for (GuiElement element : settings) {
                    if (!element.isVisible())
                        continue;
                    element.keyPressed(key);
                }
            }
        }
        super.keyPressed(key);
    }

    @Override
    public void charTyped(char key) {
        if (!binding) {
            if (expand) {
                for (GuiElement element : settings) {
                    if (!element.isVisible())
                        continue;
                    element.charTyped(key);
                }
            }
        }
        super.charTyped(key);
    }
}
