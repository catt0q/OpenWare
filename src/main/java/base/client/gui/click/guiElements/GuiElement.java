package base.client.gui.click.guiElements;

import base.client.feature.settings.Setting;
import base.client.helpers.Helper;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.impl.render.RenderHelper;
import net.minecraft.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Objects;

public class GuiElement implements Helper {

    public float x, y, width, height;
    public float alpha;

    public boolean isVisible() {
        return true;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public void keyPressed(int key) {
    }

    public void charTyped(char key) {
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, String s, Font font, int x, int y, int i, int j) {
        int k = (int) (x + i);
        int l = (int) (x + width - i);
        renderScrollingString(guiGraphics, font, s, k, (int) y, l, (int) (y + height), j);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, String component, int i, int j, int k,
            int l, int m) {
        renderScrollingString(guiGraphics, font, component, (i + k) / 2, i, j, k, l, m);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, String component, int i, int j, int k,
            int l, int m, int n) {
        int o = FontHelper.getStringWidth(component);
        int var10000 = k + m;
        Objects.requireNonNull(font);
        int p = (var10000 - 9) / 2 + 1;
        int q = l - j;
        if (o > q) {
            int r = o - q;
            double d = (double) Util.getMillis() / (double) 1000.0F;
            double e = Math.max((double) r * (double) 0.5F, (double) 3.0F);
            double f = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d / e)) / (double) 2.0F + (double) 0.5F;
            double g = Mth.lerp(f, (double) 0.0F, (double) r);
            guiGraphics.enableScissor(j, k, l, m);
            FontHelper.drawString(guiGraphics, component, j - (int) g, p, n, false);
            guiGraphics.disableScissor();
        } else {
            int r = Mth.clamp(i - (int) width / 2, j - (int) width / 2, l - o / 2);
            FontHelper.drawString(guiGraphics, component, r, p, n, false);
        }

    }
}
