package base.client.gui.click;

import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.gui.click.guiElements.impl.ModuleGuiElement;
import base.client.helpers.Helper;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.impl.render.RectHelper;
import base.client.managers.ModuleManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Util;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static base.client.gui.click.GuiHelper.*;

public class ClickGuiScreen extends Screen implements Helper {

    private final List<List<ModuleGuiElement>> columns = new ArrayList<>();
    private static final int COLUMN_COUNT = 2;
    private static final float GUI_WIDTH = 560;
    private static final float GUI_HEIGHT = 360;
    private static final float SIDEBAR_WIDTH = 120;
    private static final float CONTENT_PADDING = 10;
    private static final float COLUMN_GAP = 10;
    private static final float MODULE_SPACING = 6;
    private static final float HEADER_HEIGHT = 0;
    private static final float CORNER_RADIUS = 8;

    private Type type = Type.Combat;
    private float anim = 0, anim2 = 0, scroll = 0, aScroll = 0;
    private boolean closing = false;

    private String search = "";

    public ClickGuiScreen(ModuleManager manager) {
        super(Component.literal("Ananas"));

        // initialize columns
        for (int i = 0; i < COLUMN_COUNT; i++) {
            columns.add(new ArrayList<>());
        }

        for (Module module : manager.modules) {
            int index = manager.modules.indexOf(module) % COLUMN_COUNT;
            columns.get(index).add(new ModuleGuiElement(module));
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        // Apply blur effect
        if (mc.level != null) {
            mc.gameRenderer.processBlurEffect();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pt) {

        Window sr = mc.getWindow();

        anim = lerp(anim, closing ? 0 : 1, closing ? 0.1f : 0.2f);
        anim2 = lerp(anim2, closing ? 4f / sr.getGuiScale() : 1, closing ? 0.1f : 0.2f);

        if (closing && anim <= 0.1f) {
            closing = false;
            anim = anim2 = 0;
            mc.setScreen(null);
        }

        // Apply blur
        renderBackground(guiGraphics, mouseX, mouseY, pt);

        // Dim background (less dark)
        guiGraphics.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 0x88000000);

        float x = sr.getGuiScaledWidth() / 2f - GUI_WIDTH / 2f;
        float y = sr.getGuiScaledHeight() / 2f - GUI_HEIGHT / 2f;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x + GUI_WIDTH / 2f, y + GUI_HEIGHT / 2f);
        guiGraphics.pose().scale(anim2);
        guiGraphics.pose().translate(-x - GUI_WIDTH / 2f, -y - GUI_HEIGHT / 2f);


        // Main panel (Sprite tint)
        RectHelper.drawRoundedRect(guiGraphics, x, y, GUI_WIDTH, GUI_HEIGHT, CORNER_RADIUS,
                applyOpacity(0xFF101615, anim * 245f));
        RectHelper.drawRoundedOutline(guiGraphics, x, y, GUI_WIDTH, GUI_HEIGHT, CORNER_RADIUS, 2.2f,
                applyOpacity(0xCC000000, anim * 255f));

        renderSidebar(guiGraphics, x, y, mouseX, mouseY);
        renderModules(guiGraphics, x, y, mouseX, mouseY);

        guiGraphics.pose().popMatrix();
        guiGraphics.disableScissor();
    }

    private void renderSidebar(GuiGraphics guiGraphics, float x, float y, int mouseX, int mouseY) {
        // Sidebar background
        RectHelper.drawRoundedRect(guiGraphics, x + 1, y + 1, SIDEBAR_WIDTH, GUI_HEIGHT - 2, CORNER_RADIUS - 1,
                applyOpacity(bgColor2, anim * 255f));

        // Divider
        guiGraphics.fill((int) (x + SIDEBAR_WIDTH), (int) y + 2, (int) (x + SIDEBAR_WIDTH) + 1, (int) (y + GUI_HEIGHT) - 2,
                applyOpacity(0x55000000, anim * 255f));

        // Search box
        float sbX = x + 8;
        float sbY = y + 10;
        float sbW = SIDEBAR_WIDTH - 16;
        float sbH = 18;
        RectHelper.drawRoundedRect(guiGraphics, sbX, sbY, sbW, sbH, 4,
                applyOpacity(bgColor3, anim * 200f));
        RectHelper.drawRoundedOutline(guiGraphics, sbX, sbY, sbW, sbH, 4, 1.0f,
                applyOpacity(0x55000000, anim * 255f));
        String shown = search.isEmpty() ? "Search" : search;
        int sCol = search.isEmpty() ? applyOpacity(0xFF7A7A7A, anim * 255f) : applyOpacity(0xFFE6E6E6, anim * 255f);
        FontHelper.drawString(guiGraphics, shown, (int) sbX + 6, (int) (sbY + 5), sCol, false);

        // Tabs list
        float ty = sbY + 26;
        int idx = 0;
        for (Type t : Type.values()) {
            float rowH = 18;
            boolean hovered = isHovering(x + 6, ty, SIDEBAR_WIDTH - 12, rowH, mouseX, mouseY);
            boolean active = t == type;

            int rowBg = active ? applyOpacity(0xFF0F0F0F, anim * 255f)
                    : hovered ? applyOpacity(0xFF0C0C0C, anim * 255f) : 0;
            if (rowBg != 0) {
                RectHelper.drawRoundedRect(guiGraphics, x + 6, ty, SIDEBAR_WIDTH - 12, rowH, 4, rowBg);
            }
            if (active) {
                guiGraphics.fill((int) (x + 6), (int) ty + 2, (int) (x + 6) + 2, (int) (ty + rowH) - 2,
                        applyOpacity(getGlobalColor(0), anim * 220f));
            }

            int tc = active ? applyOpacity(0xFFEAEAEA, anim * 255f)
                    : applyOpacity(0xFFA0A0A0, anim * 255f);
            FontHelper.drawString(guiGraphics, t.name, (int) (x + 16), (int) (ty + 5), tc, false);
            ty += rowH + 2;
            idx++;
        }
    }

    private void renderModules(GuiGraphics guiGraphics, float x, float y, int mouseX, int mouseY) {
        float xOffset = 0;
        float maxHeight = 0;
        float contentY = y + 8;
        float contentHeight = GUI_HEIGHT - 16;
        float contentX = x + SIDEBAR_WIDTH + CONTENT_PADDING;

        float contentW = GUI_WIDTH - SIDEBAR_WIDTH - CONTENT_PADDING * 2;
        float colW = (contentW - (COLUMN_COUNT - 1) * COLUMN_GAP) / COLUMN_COUNT;

        guiGraphics.enableScissor(
                (int) (contentX),
                (int) (y + 2),
                (int) (x + GUI_WIDTH - 2),
                (int) (y + GUI_HEIGHT - 2));

        for (List<ModuleGuiElement> column : columns) {
            float yOffset = MODULE_SPACING;
            for (ModuleGuiElement module : column) {
                if (module.module.type != type)
                    continue;

                if (!search.isEmpty()) {
                    String q = search.toLowerCase();
                    if (!module.module.getLabel().toLowerCase().contains(q)) {
                        continue;
                    }
                }

                module.alpha = anim * 255f;
                module.x = contentX + xOffset;
                module.y = contentY + yOffset + aScroll;
                module.width = colW;
                module.render(guiGraphics, mouseX, mouseY);
                yOffset += module.height + MODULE_SPACING;
            }
            maxHeight = Math.max(maxHeight, yOffset);
            xOffset += colW + COLUMN_GAP;
        }

        scroll = maxHeight > contentHeight ? Mth.clamp(scroll, -maxHeight + contentHeight, 0) : 0;
        aScroll = lerp(aScroll, scroll, 0.15f);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        Window sr = mc.getWindow();
        float guiX = sr.getGuiScaledWidth() / 2f - GUI_WIDTH / 2f;
        float guiY = sr.getGuiScaledHeight() / 2f - GUI_HEIGHT / 2f;

        if (!isHovering(guiX, guiY, GUI_WIDTH, GUI_HEIGHT, mouseX, mouseY))
            return super.mouseClicked(event, bl);

        // sidebar tabs + search focus
        float sbX = guiX + 8;
        float sbY = guiY + 10;
        float sbW = SIDEBAR_WIDTH - 16;
        float sbH = 18;
        if (isHovering(sbX, sbY, sbW, sbH, mouseX, mouseY)) {
            // focus implied; typing appends to search
        }

        float ty = sbY + 26;
        for (Type t : Type.values()) {
            float rowH = 18;
            if (isHovering(guiX + 6, ty, SIDEBAR_WIDTH - 12, rowH, mouseX, mouseY)) {
                type = t;
                scroll = 0;
            }
            ty += rowH + 2;
        }

        // modules
        for (List<ModuleGuiElement> column : columns) {
            for (ModuleGuiElement module : column) {
                if (module.module.type != type)
                    continue;
                if (isHovering(module.x, module.y, module.width, 24, mouseX, mouseY)) {
                    if (button == 0)
                        module.module.toggle();
                    else if (button == 1)
                        module.expand = !module.expand;
                    else if (button == 2)
                        module.binding = !module.binding;
                }
                module.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double x = event.x();
        double y = event.y();
        int button = event.button();
        columns.forEach(col -> col.forEach(m -> {
            if (m.module.type == type)
                m.mouseReleased(x, y, button);
        }));
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double hor, double vert) {
        scroll += (float) vert * 30f;
        return super.mouseScrolled(x, y, hor, vert);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char c = (char) event.codepoint();

        if (c >= 32 && c != 127) {
            search += c;
            scroll = 0;
        }

        columns.forEach(col -> col.forEach(m -> {
            if (m.module.type == type)
                m.charTyped(c);
        }));
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int c = event.key();

        if (c == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            scroll = 0;
            return true;
        }
        if (c == GLFW.GLFW_KEY_ESCAPE && !search.isEmpty()) {
            search = "";
            scroll = 0;
            return true;
        }

        columns.forEach(col -> col.forEach(m -> {
            if (m.module.type == type)
                m.keyPressed(c);
        }));
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        closing = true;
        lockCursor();
    }

    private void lockCursor() {
        if (!mc.isWindowActive() || mc.mouseHandler.isMouseGrabbed())
            return;
        if (Util.getPlatform() != Util.OS.OSX)
            KeyMapping.setAll();

        mc.mouseHandler.mouseGrabbed = true;
        mc.mouseHandler.xpos = mc.getWindow().getScreenWidth() / 2.0;
        mc.mouseHandler.ypos = mc.getWindow().getScreenHeight() / 2.0;
        InputConstants.grabOrReleaseMouse(mc.getWindow(), 212995, mc.mouseHandler.xpos,
                mc.mouseHandler.ypos);
        mc.missTime = 10000;
        mc.mouseHandler.ignoreFirstMove = true;
    }
}
