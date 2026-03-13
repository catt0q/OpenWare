package base.client.gui.click;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record RoundedRectRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose,
        float x, float y, float width, float height, float radius, int color, int segments,
        @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {

    public RoundedRectRenderState(RenderPipeline renderPipeline, TextureSetup textureSetup, Matrix3x2f matrix3x2f,
            float x, float y, float width, float height, float radius, int color, int segments,
            @Nullable ScreenRectangle screenRectangle) {
        this(renderPipeline, textureSetup, matrix3x2f, x, y, width, height, radius, color, segments, screenRectangle,
                getBounds(x, y, width, height, matrix3x2f, screenRectangle));
    }

    @Override
    public void buildVertices(VertexConsumer buffer) {
        float actualRadius = Math.min(radius, Math.min(width, height) / 2);

        if (actualRadius <= 1) {
            // Fallback to regular rect (quad)
            buffer.addVertexWith2DPose(pose, x, y).setColor(color);
            buffer.addVertexWith2DPose(pose, x, y + height).setColor(color);
            buffer.addVertexWith2DPose(pose, x + width, y + height).setColor(color);
            buffer.addVertexWith2DPose(pose, x + width, y).setColor(color);
            return;
        }

        float cx1 = x + actualRadius;
        float cy1 = y + actualRadius;
        float cx2 = x + width - actualRadius;
        float cy2 = y + height - actualRadius;

        // Main center quad (full height, between corners horizontally)
        buffer.addVertexWith2DPose(pose, cx1, y).setColor(color);
        buffer.addVertexWith2DPose(pose, cx1, y + height).setColor(color);
        buffer.addVertexWith2DPose(pose, cx2, y + height).setColor(color);
        buffer.addVertexWith2DPose(pose, cx2, y).setColor(color);

        // Left quad (between top-left and bottom-left corners)
        buffer.addVertexWith2DPose(pose, x, cy1).setColor(color);
        buffer.addVertexWith2DPose(pose, x, cy2).setColor(color);
        buffer.addVertexWith2DPose(pose, cx1, cy2).setColor(color);
        buffer.addVertexWith2DPose(pose, cx1, cy1).setColor(color);

        // Right quad (between top-right and bottom-right corners)
        buffer.addVertexWith2DPose(pose, cx2, cy1).setColor(color);
        buffer.addVertexWith2DPose(pose, cx2, cy2).setColor(color);
        buffer.addVertexWith2DPose(pose, x + width, cy2).setColor(color);
        buffer.addVertexWith2DPose(pose, x + width, cy1).setColor(color);

        // Corners - use quad strips from center outward for proper filling
        int segs = Math.max(8, segments);
        buildFilledCorner(buffer, cx1, cy1, actualRadius, 180, 270, segs); // top-left
        buildFilledCorner(buffer, cx2, cy1, actualRadius, 270, 360, segs); // top-right
        buildFilledCorner(buffer, cx2, cy2, actualRadius, 0, 90, segs);    // bottom-right
        buildFilledCorner(buffer, cx1, cy2, actualRadius, 90, 180, segs);  // bottom-left
    }

    private void buildFilledCorner(VertexConsumer buffer, float cx, float cy, float r, int startAngle, int endAngle, int totalSegs) {
        // Fill corner using concentric quad strips from center outward
        int radialSteps = 4; // Number of concentric rings
        int arcSegs = Math.max(4, totalSegs / 4);
        float angleStep = (float) (endAngle - startAngle) / arcSegs;

        for (int ring = 0; ring < radialSteps; ring++) {
            float innerR = r * ring / radialSteps;
            float outerR = r * (ring + 1) / radialSteps;

            for (int i = 0; i < arcSegs; i++) {
                float angle1 = (float) Math.toRadians(startAngle + i * angleStep);
                float angle2 = (float) Math.toRadians(startAngle + (i + 1) * angleStep);

                float cos1 = (float) Math.cos(angle1);
                float sin1 = (float) Math.sin(angle1);
                float cos2 = (float) Math.cos(angle2);
                float sin2 = (float) Math.sin(angle2);

                float ix1 = cx + cos1 * innerR;
                float iy1 = cy + sin1 * innerR;
                float ix2 = cx + cos2 * innerR;
                float iy2 = cy + sin2 * innerR;

                float ox1 = cx + cos1 * outerR;
                float oy1 = cy + sin1 * outerR;
                float ox2 = cx + cos2 * outerR;
                float oy2 = cy + sin2 * outerR;

                // Proper quad from inner arc to outer arc
                buffer.addVertexWith2DPose(pose, ix1, iy1).setColor(color);
                buffer.addVertexWith2DPose(pose, ix2, iy2).setColor(color);
                buffer.addVertexWith2DPose(pose, ox2, oy2).setColor(color);
                buffer.addVertexWith2DPose(pose, ox1, oy1).setColor(color);
            }
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
