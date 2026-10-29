package dev.shelfesp;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Rendering pipelines that ignore the depth buffer, so shelves stay visible
 * through walls just like a chest ESP overlay.
 */
public final class ShelfEspPipelines {
    private static final Identifier LINES_SHADER = Identifier.fromNamespaceAndPath("shelfesp", "core/esp_lines");

    private static final Snippet FOGLESS_LINES = RenderPipeline.builder(
                    RenderPipelines.MATRICES_FOG_SNIPPET,
                    RenderPipelines.GLOBALS_SNIPPET)
            .withVertexShader(LINES_SHADER)
            .withFragmentShader(LINES_SHADER)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, Mode.LINES)
            .buildSnippet();

    public static final RenderPipeline ESP_LINES = RenderPipelines.register(
            RenderPipeline.builder(FOGLESS_LINES)
                    .withLocation(Identifier.fromNamespaceAndPath("shelfesp", "pipeline/esp_lines"))
                    .withDepthStencilState(Optional.empty())
                    .build());

    public static final RenderPipeline ESP_QUADS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("shelfesp", "pipeline/esp_quads"))
                    .withDepthStencilState(Optional.empty())
                    .build());

    private ShelfEspPipelines() {
    }
}
