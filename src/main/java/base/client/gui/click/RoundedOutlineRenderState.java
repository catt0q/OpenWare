package base.client.gui.click;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record RoundedOutlineRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose,
        float x, float y, float width, float height, float radius, float lineWidth, int color, int segments,
        @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {

    public RoundedOutlineRenderState(RenderPipeline renderPipeline, TextureSetup textureSetup, Matrix3x2f matrix3x2f,
            float x, float y, float width, float height, float radius, float lineWidth, int color, int segments,
            @Nullable ScreenRectangle screenRectangle) {
        this(renderPipeline, textureSetup, matrix3x2f, x, y, width, height, radius, lineWidth, color, segments, screenRectangle,
                getBounds(x, y, width, height, matrix3x2f, screenRectangle));
    }

    @Override
    public void buildVertices(VertexConsumer buffer) {
        float actualRadius = Math.min(radius, Math.min(width, height) / 2);
        float lw = lineWidth;

        // Top edge
        addQuad(buffer, x + actualRadius, y, x + width - actualRadius, y + lw);
        // Bottom edge
        addQuad(buffer, x + actualRadius, y + height - lw, x + width - actualRadius, y + height);
        // Left edge
        addQuad(buffer, x, y + actualRadius, x + lw, y + height - actualRadius);
        // Right edge
        addQuad(buffer, x + width - lw, y + actualRadius, x + width, y + height - actualRadius);

        // Corner arcs
        float innerRadius = actualRadius - lw;
        if (innerRadius < 0) innerRadius = 0;

        float cx1 = x + actualRadius;
        float cy1 = y + actualRadius;
        float cx2 = x + width - actualRadius;
        float cy2 = y + height - actualRadius;

        buildCornerOutline(buffer, cx1, cy1, actualRadius, innerRadius, 180, 270); // top-left
        buildCornerOutline(buffer, cx2, cy1, actualRadius, innerRadius, 270, 360); // top-right
        buildCornerOutline(buffer, cx2, cy2, actualRadius, innerRadius, 0, 90);    // bottom-right
        buildCornerOutline(buffer, cx1, cy2, actualRadius, innerRadius, 90, 180);  // bottom-left
    }

    private void addQuad(VertexConsumer buffer, float x1, float y1, float x2, float y2) {
        buffer.addVertexWith2DPose(pose, x1, y1).setColor(color);
        buffer.addVertexWith2DPose(pose, x1, y2).setColor(color);
        buffer.addVertexWith2DPose(pose, x2, y2).setColor(color);
        buffer.addVertexWith2DPose(pose, x2, y1).setColor(color);
    }

    private void buildCornerOutline(VertexConsumer buffer, float cx, float cy, float outerR, float innerR, int startAngle, int endAngle) {
        int segs = Math.max(4, segments / 4);
        float angleStep = (float) (endAngle - startAngle) / segs;

        for (int i = 0; i < segs; i++) {
            float angle1 = (float) Math.toRadians(startAngle + i * angleStep);
            float angle2 = (float) Math.toRadians(startAngle + (i + 1) * angleStep);

            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);
            float cos2 = (float) Math.cos(angle2);
            float sin2 = (float) Math.sin(angle2);

            float ox1 = cx + cos1 * outerR;
            float oy1 = cy + sin1 * outerR;
            float ox2 = cx + cos2 * outerR;
            float oy2 = cy + sin2 * outerR;

            float ix1 = cx + cos1 * innerR;
            float iy1 = cy + sin1 * innerR;
            float ix2 = cx + cos2 * innerR;
            float iy2 = cy + sin2 * innerR;

            // Quad from inner to outer arc
            buffer.addVertexWith2DPose(pose, ix1, iy1).setColor(color);
            buffer.addVertexWith2DPose(pose, ix2, iy2).setColor(color);
            buffer.addVertexWith2DPose(pose, ox2, oy2).setColor(color);
            buffer.addVertexWith2DPose(pose, ox1, oy1).setColor(color);
        }
    }

    @Nullable
    private static ScreenRectangle getBounds(float x, float y, float width, float height, Matrix3x2f matrix3x2f,
            @Nullable ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = (new ScreenRectangle((int) x, (int) y, (int) width, (int) height))
                .transformMaxBounds(matrix3x2f);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }
}
