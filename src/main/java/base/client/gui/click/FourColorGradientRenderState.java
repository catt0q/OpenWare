package base.client.gui.click;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record FourColorGradientRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x0,
        int y0, int x1, int y1, int col1, int col2, int col3, int col4, @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
    public FourColorGradientRenderState(RenderPipeline renderPipeline, TextureSetup textureSetup, Matrix3x2f matrix3x2f,
            int i, int j, int k, int l, int m, int n, int col3, int col4, @Nullable ScreenRectangle screenRectangle) {
        this(renderPipeline, textureSetup, matrix3x2f, i, j, k, l, m, n, col3, col4, screenRectangle,
                getBounds(i, j, k, l, matrix3x2f, screenRectangle));
    }

    public FourColorGradientRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x0,
            int y0, int x1, int y1, int col1, int col2, int col3, int col4, @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds) {
        this.pipeline = pipeline;
        this.textureSetup = textureSetup;
        this.pose = pose;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.col1 = col1;
        this.col2 = col2;
        this.col3 = col3;
        this.col4 = col4;
        this.scissorArea = scissorArea;
        this.bounds = bounds;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(this.pose(), (float) this.x0(), (float) this.y0()).setColor(this.col1());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float) this.x0(), (float) this.y1()).setColor(this.col2());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float) this.x1(), (float) this.y1()).setColor(this.col3());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float) this.x1(), (float) this.y0()).setColor(this.col4());
    }

    @Nullable
    private static ScreenRectangle getBounds(int i, int j, int k, int l, Matrix3x2f matrix3x2f,
            @Nullable ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = (new ScreenRectangle(i, j, k - i, l - j)).transformMaxBounds(matrix3x2f);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }

    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    public TextureSetup textureSetup() {
        return this.textureSetup;
    }

    public Matrix3x2f pose() {
        return this.pose;
    }

    public int x0() {
        return this.x0;
    }

    public int y0() {
        return this.y0;
    }

    public int x1() {
        return this.x1;
    }

    public int y1() {
        return this.y1;
    }

    public int col1() {
        return this.col1;
    }

    public int col2() {
        return this.col2;
    }

    @Nullable
    public ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Nullable
    public ScreenRectangle bounds() {
        return this.bounds;
    }
}
