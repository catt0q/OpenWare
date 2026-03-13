package base.client.helpers.impl.render;

import base.client.helpers.impl.render.render3d.RenderPresets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RectHelper {

    public static long delta = 0L;

    /*
     * public static void drawRoundedRect(GuiGraphics context, double x, double y,
     * double width, double height, float radius, Color color) {
     * float x2 = (float) (x + ((radius / 2F) + 0.5F));
     * float y2 = (float) (y + ((radius / 2F) + 0.5F));
     * float width2 = (float) (width - ((radius / 2F) + 0.5F));
     * float height2 = (float) (height - ((radius / 2F) + 0.5F));
     * 
     * drawRect(context, x2, y2, x2 + width2, y2 + height2, color.getRGB());
     * 
     * polygon(context, x, y, radius * 2, 360, true, color);
     * polygon(context, x + width2 - radius + 1.2, y, radius * 2, 360, true, color);
     * polygon(context, x + width2 - radius + 1.2, y + height2 - radius + 1, radius
     * * 2, 360, true, color);
     * polygon(context, x, y + height2 - radius + 1, radius * 2, 360, true, color);
     * 
     * setColor(color);
     * drawRect(context, x2 - radius / 2 - 0.5F, y2 + radius / 2, x2 + width2, y2 +
     * height2 - radius / 2, color.getRGB());
     * drawRect(context, x2, y2 + radius / 2, x2 + width2 + radius / 2 + 0.5f, y2 +
     * height2 - radius / 2, color.getRGB());
     * drawRect(context, x2 + radius / 2, y2 - radius / 2 - 0.5F, x2 + width2 -
     * radius / 2, y + height2 - radius / 2, color.getRGB());
     * drawRect(context, x2 + radius / 2, y2, x2 + width2 - radius / 2, y2 + height2
     * + radius / 2 + 0.5f, color.getRGB());
     * }
     * 
     * public static void drawVerticalGradientSmoothRect(GuiGraphics context, float
     * left, float top, float right, float bottom, int color, int color2) {
     * GL11.glEnable(GL11.GL_BLEND);
     * GL11.glEnable(GL11.GL_LINE_SMOOTH);
     * drawGradientRect(context, left, top, right, bottom, color, color2);
     * GL11.glScalef(0.5f, 0.5f, 0.5f);
     * drawGradientRect(context, left * 2 - 1, top * 2, left * 2, bottom * 2 - 1,
     * color, color2);
     * drawGradientRect(context, left * 2, top * 2 - 1, right * 2, top * 2, color,
     * color2);
     * drawGradientRect(context, right * 2, top * 2, right * 2 + 1, bottom * 2 - 1,
     * color, color2);
     * drawGradientRect(context, left * 2, bottom * 2 - 1, right * 2, bottom * 2,
     * color, color2);
     * GL11.glDisable(GL11.GL_LINE_SMOOTH);
     * GL11.glDisable(GL11.GL_BLEND);
     * GL11.glScalef(2F, 2F, 2F);
     * }
     * 
     * public static void drawVerticalGradientRect(GuiGraphics context, float left,
     * float top, float right, float bottom, int color, int color2) {
     * drawGradientRect(context, left, top, right, bottom, color, color2);
     * }
     * 
     * public static void drawVerticalGradientBetterRect(GuiGraphics context, float
     * x, float y, float width, float height, int color, int color2) {
     * drawGradientRect(context, x, y, x + width, y + height, color, color2);
     * }
     * 
     * public static void polygon(GuiGraphics context, double x, double y, double
     * sideLength, double amountOfSides, boolean filled, Color color) {
     * sideLength /= 2;
     * GL11.glEnable(GL11.GL_BLEND);
     * GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
     * GL11.glDisable(GL11.GL_TEXTURE_2D);
     * GL11.glDisable(GL11.GL_CULL_FACE);
     * GL11.glDisable(GL11.GL_DEPTH_TEST);
     * setColor(color);
     * 
     * if (!filled) {
     * GL11.glLineWidth(1);
     * }
     * 
     * GL11.glEnable(GL11.GL_LINE_SMOOTH);
     * 
     * VertexConsumer vertexConsumer =
     * context.bufferSource.getBuffer(RenderType.gui());
     * 
     * 
     * for (double i = 0; i <= amountOfSides; i++) {
     * double angle = i * (Math.PI * 2) / amountOfSides;
     * vertexConsumer.addVertex(context.pose().last().pose(), (float) (x +
     * (sideLength * Math.cos(angle)) + sideLength), (float) (y + (sideLength *
     * Math.sin(angle)) +
     * sideLength),0).setColor(color.getRed(),color.getGreen(),color.getBlue(),color
     * .getAlpha());
     * }
     * 
     * 
     * GL11.glDisable(GL11.GL_LINE_SMOOTH);
     * GL11.glEnable(GL11.GL_DEPTH_TEST);
     * GL11.glEnable(GL11.GL_CULL_FACE);
     * GL11.glEnable(GL11.GL_TEXTURE_2D);
     * GL11.glDisable(GL11.GL_BLEND);
     * }
     * 
     * public static void drawRectBetter(GuiGraphics context, double x, double y,
     * double width, double height, int color) {
     * drawRect(context, x, y, x + width, y + height, color);
     * }
     * 
     * public static void drawGradientRectBetter(GuiGraphics context, float x, float
     * y, float width, float height, int color, int color2) {
     * drawGradientRect(context, x, y, x + width, y + height, color, color2);
     * }
     * 
     * public static void drawSmoothGradientRect(GuiGraphics context, double left,
     * double top, double right, double bottom, int color, int color2) {
     * GL11.glEnable(GL11.GL_BLEND);
     * GL11.glEnable(GL11.GL_LINE_SMOOTH);
     * drawGradientRect(context, left, top, right, bottom, color, color2);
     * GL11.glScalef(0.5f, 0.5f, 0.5f);
     * drawGradientRect(context, left * 2 - 1, top * 2, left * 2, bottom * 2 - 1,
     * color, color2);
     * drawGradientRect(context, left * 2, top * 2 - 1, right * 2, top * 2, color,
     * color2);
     * drawGradientRect(context, right * 2, top * 2, right * 2 + 1, bottom * 2 - 1,
     * color, color2);
     * drawGradientRect(context, left * 2, bottom * 2 - 1, right * 2, bottom * 2,
     * color, color2);
     * GL11.glDisable(GL11.GL_LINE_SMOOTH);
     * GL11.glDisable(GL11.GL_BLEND);
     * GL11.glScalef(2F, 2F, 2F);
     * }
     * 
     * public static void drawSmoothRectBetter(GuiGraphics context, float x, float
     * y, float width, float height, int color) {
     * drawSmoothRect(context, x, y, x + width, y + height, color);
     * }
     * 
     * public static void drawRect(GuiGraphics context, double left, double top,
     * double right, double bottom, int color) {
     * context.fill((int)left, (int)top, (int)right, (int)bottom, color);
     * }
     * 
     * public static void drawSmoothRect(GuiGraphics context, float left, float top,
     * float right, float bottom, int color) {
     * GL11.glEnable(GL11.GL_BLEND);
     * GL11.glEnable(GL11.GL_LINE_SMOOTH);
     * drawRect(context, left, top, right, bottom, color);
     * GL11.glScalef(0.5f, 0.5f, 0.5f);
     * drawRect(context, left * 2 - 1, top * 2, left * 2, bottom * 2 - 1, color);
     * drawRect(context, left * 2, top * 2 - 1, right * 2, top * 2, color);
     * drawRect(context, right * 2, top * 2, right * 2 + 1, bottom * 2 - 1, color);
     * drawRect(context, left * 2, bottom * 2 - 1, right * 2, bottom * 2, color);
     * GL11.glDisable(GL11.GL_LINE_SMOOTH);
     * GL11.glDisable(GL11.GL_BLEND);
     * GL11.glScalef(2F, 2F, 2F);
     * }
     * 
     * public static void drawGradientRect(GuiGraphics context, double left, double
     * top, double right, double bottom, int color, int color2) {
     * context.fillGradient(
     * (int)left, (int)top, (int)right, (int)bottom,
     * color, color2
     * );
     * }
     * 
     * public static void drawSkeetButton(GuiGraphics context, float x, float y,
     * float right, float bottom) {
     * drawSmoothRect(context, x - 31.0f, y - 43.0f, right + 31.0f, bottom - 30.0f,
     * new Color(0, 0, 0, 255).getRGB());
     * drawSmoothRect(context, x - 30.5f, y - 42.5f, right + 30.5f, bottom - 30.5f,
     * new Color(45, 45, 45, 255).getRGB());
     * drawGradientRect(context, x - 30, y - 42, right + 30, bottom - 31,
     * new Color(48, 48, 48, 255).getRGB(),
     * new Color(19, 19, 19, 255).getRGB());
     * }
     * 
     * public static void drawSkeetRectWithoutBorder(GuiGraphics context, float x,
     * float y, float right, float bottom) {
     * drawSmoothRect(context, x - 41f, y - 61f, right + 41f, bottom + 61f, new
     * Color(48, 48, 48, 255).getRGB());
     * drawSmoothRect(context, x - 40.0f, y - 60.0f, right + 40.0f, bottom + 60.0f,
     * new Color(17, 17, 17, 255).getRGB());
     * }
     * 
     * public static void drawSkeetRect(GuiGraphics context, float x, float y, float
     * right, float bottom) {
     * drawRect(context, x - 46.5f, y - 66.5f, right + 46.5f, bottom + 66.5f, new
     * Color(0, 0, 0, 255).getRGB());
     * drawRect(context, x - 46.0f, y - 66.0f, right + 46.0f, bottom + 66.0f, new
     * Color(48, 48, 48, 255).getRGB());
     * drawRect(context, x - 44.5f, y - 64.5f, right + 44.5f, bottom + 64.5f, new
     * Color(33, 33, 33, 255).getRGB());
     * drawRect(context, x - 43.5f, y - 63.5f, right + 43.5f, bottom + 63.5f, new
     * Color(0, 0, 0, 255).getRGB());
     * drawRect(context, x - 43.0f, y - 63.0f, right + 43.0f, bottom + 63.0f, new
     * Color(9, 9, 9, 255).getRGB());
     * drawRect(context, x - 40.5f, y - 60.5f, right + 40.5f, bottom + 60.5f, new
     * Color(48, 48, 48, 255).getRGB());
     * drawRect(context, x - 40.0f, y - 60.0f, right + 40.0f, bottom + 60.0f, new
     * Color(17, 17, 17, 255).getRGB());
     * }
     * 
     * public static void drawBorderedRect(GuiGraphics context, float left, float
     * top, float right, float bottom, float borderWidth, int insideColor, int
     * borderColor, boolean borderIncludedInBounds) {
     * drawRect(context, left - (!borderIncludedInBounds ? borderWidth : 0), top -
     * (!borderIncludedInBounds ? borderWidth : 0),
     * right + (!borderIncludedInBounds ? borderWidth : 0), bottom +
     * (!borderIncludedInBounds ? borderWidth : 0), borderColor);
     * drawRect(context, left + (borderIncludedInBounds ? borderWidth : 0), top +
     * (borderIncludedInBounds ? borderWidth : 0),
     * right - ((borderIncludedInBounds ? borderWidth : 0)), bottom -
     * ((borderIncludedInBounds ? borderWidth : 0)), insideColor);
     * }
     * 
     * public static void drawOutlineRect(GuiGraphics context, float x, float y,
     * float width, float height, Color color, Color colorTwo) {
     * drawRect(context, x, y, x + width, y + height, color.getRGB());
     * int colorRgb = colorTwo.getRGB();
     * drawRect(context, x - 1, y, x, y + height, colorRgb);
     * drawRect(context, x + width, y, x + width + 1, y + height, colorRgb);
     * drawRect(context, x - 1, y - 1, x + width + 1, y, colorRgb);
     * drawRect(context, x - 1, y + height, x + width + 1, y + height + 1,
     * colorRgb);
     * }
     * 
     * private static void setColor(Color color) {
     * GL11.glColor4f(color.getRed() / 255F, color.getGreen() / 255F,
     * color.getBlue() / 255F, color.getAlpha() / 255F);
     * }
     */
    public static void drawRect(GuiGraphics context, double left, double top, double right, double bottom, int color) {
        context.fill((int) left, (int) top, (int) right, (int) bottom, color);
    }

    public static void drawRoundedRect(GuiGraphics context, float x, float y, float width, float height, float radius,
            int color) {
        // Removed rounding: draw a simple axis-aligned rectangle instead of rounded rect
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
    }

    private static void drawCorner(GuiGraphics context, float cx, float cy, float radius, int startAngle, int endAngle,
            int color) {
        // Legacy method - kept for compatibility
        for (int i = startAngle; i < endAngle; i += 10) {
            double rad1 = Math.toRadians(i);
            double rad2 = Math.toRadians(i + 10);

            float x1 = (float) (cx + Math.cos(rad1) * radius);
            float y1 = (float) (cy + Math.sin(rad1) * radius);
            float x2 = (float) (cx + Math.cos(rad2) * radius);
            float y2 = (float) (cy + Math.sin(rad2) * radius);

            int minX = (int) Math.min(Math.min(cx, x1), x2);
            int minY = (int) Math.min(Math.min(cy, y1), y2);
            int maxX = (int) Math.max(Math.max(cx, x1), x2) + 1;
            int maxY = (int) Math.max(Math.max(cy, y1), y2) + 1;

            context.fill(minX, minY, maxX, maxY, color);
        }
    }

    public static void drawRoundedOutline(GuiGraphics context, float x, float y, float width, float height,
            float radius, float lineWidth, int color) {
        // Removed rounded outlines: draw a simple rectangle outline using thin borders
        int w = Math.max(1, (int) lineWidth);
        // top
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + w), color);
        // bottom
        context.fill((int) x, (int) (y + height - w), (int) (x + width), (int) (y + height), color);
        // left
        context.fill((int) x, (int) y, (int) (x + w), (int) (y + height), color);
        // right
        context.fill((int) (x + width - w), (int) y, (int) (x + width), (int) (y + height), color);
    }

    public static void drawBox(PoseStack matrices,
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ,
            float red, float green, float blue, float alpha, float lineWidth) {

        Matrix4f matrix = matrices.last().pose();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(
                RenderPresets.TWLines);

        // Переводим мировые координаты в координаты относительно камеры
        float x1 = (float) (minX - cameraPos.x);
        float y1 = (float) (minY - cameraPos.y);
        float z1 = (float) (minZ - cameraPos.z);
        float x2 = (float) (maxX - cameraPos.x);
        float y2 = (float) (maxY - cameraPos.y);
        float z2 = (float) (maxZ - cameraPos.z);

        // Нижняя грань
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);

        // Верхняя грань
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);

        // Вертикальные линии
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);

    }

}
