package base.client.helpers.impl.render;

import base.client.helpers.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Minimal, Minecraft-font-only font helper. This is a backward-compat shim to keep
 * rendering functional while removing custom font support elsewhere.
 */
public class FontHelper implements Helper {
    public static Font getRenderer(Identifier id) {
        return Minecraft.getInstance().font;
    }

    public static Font getFont() {
        return Minecraft.getInstance().font;
    }

    public static void drawString(GuiGraphics dc, String text, int x, int y, int color, boolean shadow) {
        Font font = Minecraft.getInstance().font;
        if (shadow) {
            // basic drop shadow
            dc.drawString(font, Component.literal(text), x + 1, y + 1, 0x88000000, false);
        }
        dc.drawString(font, Component.literal(text), x, y, color, false);
    }

    public static int getStringWidth(String text) {
        return Minecraft.getInstance().font.width(Component.literal(text));
    }

    public static int getLineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }
}
