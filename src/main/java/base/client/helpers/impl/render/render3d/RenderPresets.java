package base.client.helpers.impl.render.render3d;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.util.OptionalDouble;

import static net.minecraft.client.renderer.RenderPipelines.*;

public class RenderPresets {
  public static RenderPipeline.Snippet TWLINES_SNIPPET;
  public static RenderPipeline TWLINES;
  public static RenderType TWLines; // ThroughWalls

  public static void init() {
    TWLINES_SNIPPET = RenderPipeline.builder(new RenderPipeline.Snippet[] { MATRICES_FOG_SNIPPET, GLOBALS_SNIPPET })
        .withVertexShader("core/rendertype_lines")
        .withFragmentShader("core/rendertype_lines")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .buildSnippet();
    TWLINES = register(RenderPipeline.builder(new RenderPipeline.Snippet[] { TWLINES_SNIPPET })
        .withLocation("pipeline/lines")
        .build());

    // simplified rendertype - RenderStateShard removed in 1.21.11
    TWLines = RenderTypes.lines();
  }
}
