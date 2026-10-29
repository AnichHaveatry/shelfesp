package dev.shelfesp;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/** Render types backed by {@link ShelfEspPipelines}. */
public final class ShelfEspRenderTypes {
    public static final RenderType ESP_LINES = RenderType.create(
            "shelfesp:esp_lines",
            RenderSetup.builder(ShelfEspPipelines.ESP_LINES)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup());

    public static final RenderType ESP_QUADS = RenderType.create(
            "shelfesp:esp_quads",
            RenderSetup.builder(ShelfEspPipelines.ESP_QUADS)
                    .sortOnUpload()
                    .createRenderSetup());

    private ShelfEspRenderTypes() {
    }
}
