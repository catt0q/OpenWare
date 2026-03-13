package base.client.helpers.impl.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderHelper {
    public static int darker(int color, float factor) {
        int r = (int) ((float) (color >> 16 & 0xFF) * factor);
        int g = (int) ((float) (color >> 8 & 0xFF) * factor);
        int b = (int) ((float) (color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }

    public static void setColor(int color) {
        GL11.glColor4ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));
    }

    // replacement for removed ShapeRenderer.renderLineBox()
    public static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, AABB box, float r, float g, float b,
            float a) {
        Matrix4f matrix = poseStack.last().pose();
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // bottom face edges
        line(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        line(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        line(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        line(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // top face edges
        line(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        line(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        line(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // vertical edges
        line(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        line(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        line(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void line(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2,
            float z2, float r, float g, float b, float a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float nx = len > 0 ? dx / len : 0;
        float ny = len > 0 ? dy / len : 0;
        float nz = len > 0 ? dz / len : 1;

        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(2.0f);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(2.0f);
    }

}
