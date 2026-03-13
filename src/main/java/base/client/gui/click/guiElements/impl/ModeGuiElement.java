package base.client.gui.click.guiElements.impl;

import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.gui.click.GuiHelper;
import base.client.gui.click.guiElements.GuiElement;
import base.client.helpers.impl.render.FontHelper;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

import static base.client.gui.click.GuiHelper.bgColor2;
import static base.client.gui.click.GuiHelper.color;

public class ModeGuiElement extends GuiElement {

    ModeSetting setting;

    public List<AnimMode> modes = new ArrayList<>();

    public ModeGuiElement(ModeSetting setting) {
        this.setting = setting;
        height = 16;
        expand = false;
        for (String s : setting.getModes()) {
            modes.add(new AnimMode(s));
        }
    }

    float lerp = 0;
    boolean expand;
    private int colorIndex = 0;

    public void setColorIndex(int index) {
        this.colorIndex = index;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        lerp = GuiHelper.lerp(lerp, expand ? 1 : 0, 0.15f);
        GuiHelper.rectFilledRounded(graphics, x + 2, y + 1, width - 4, height - 2, GuiHelper.BORDER_RADIUS,
                GuiHelper.applyOpacity(bgColor2, alpha * 255f));
        renderScrollingString(graphics, setting.getName(), FontHelper.getFont(), (int) x + 6, (int) y, 0,
                GuiHelper.applyOpacity(-1, alpha * 255f));
        float yOffset = 16;
        if (lerp > 0.05) {
            for (int i = 0; i < modes.size(); i++) {
                AnimMode mode = modes.get(i);
                String s = mode.mode;
                mode.animation = GuiHelper.lerp(mode.animation, setting.currentMode.equalsIgnoreCase(s) ? 1 : 0, 0.15f);
                GuiHelper.rectFilledRounded(graphics, x + 2, y + yOffset + 1, width - 4, 14, GuiHelper.BORDER_RADIUS,
                        GuiHelper.applyOpacity(GuiHelper.interpolateColor(bgColor2, 0xFF3A3A3A, mode.animation),
                                alpha * lerp * 255f));
                renderScrollingString(graphics, s, FontHelper.getFont(), (int) x + 6, (int) (y + yOffset), 0,
                        GuiHelper.applyOpacity(-1, alpha * lerp * 255f));
                yOffset += 16f * (float) (lerp * alpha);
            }
        }
        height = yOffset;
        // outline
        base.client.helpers.impl.render.RectHelper.drawRoundedOutline(graphics, x + 2, y + 1, width - 4, height - 2,
                GuiHelper.BORDER_RADIUS, 1f, GuiHelper.applyOpacity(0x55000000, alpha * 255f));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (GuiHelper.isHovering(x, y, width, 16, mouseX, mouseY)) {
            expand = !expand;
        }
        if (expand) {
            float yOffset = 16;
            for (AnimMode mode : modes) {
                String s = mode.mode;
                if (GuiHelper.isHovering(x, y + yOffset, width, 16, mouseX, mouseY)) {
                    setting.setListMode(s);
                }
                yOffset += 16f * (float) (lerp * alpha);
            }
        }
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }

    public class AnimMode {
        public String mode;
        public float animation;

        public AnimMode(String node) {
            mode = node;
            animation = setting.currentMode.equalsIgnoreCase(node) ? 1 : 0;
        }

    }
}
