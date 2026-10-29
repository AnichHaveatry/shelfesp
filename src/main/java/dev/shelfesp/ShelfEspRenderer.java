package dev.shelfesp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Draws the ESP boxes and tracer lines for the currently highlighted shelves. */
public final class ShelfEspRenderer {
    private static final Minecraft CLIENT = Minecraft.getInstance();
    private static final float LINE_WIDTH = 2.0F;

    private ShelfEspRenderer() {
    }

    public static void render(PoseStack poseStack, float partialTick) {
        ShelfEspClient mod = ShelfEspClient.getInstance();
        if (mod == null || !mod.isEnabled() || CLIENT.level == null) {
            return;
        }

        List<AABB> boxes = mod.getBoxes();
        if (boxes.isEmpty()) {
            return;
        }

        Camera camera = CLIENT.gameRenderer.getMainCamera();
        if (camera == null) {
            return;
        }

        ShelfEspConfig config = mod.getConfig();
        Vec3 offset = camera.position().reverse();
        BufferSource buffers = CLIENT.renderBuffers().bufferSource();

        if (config.style.hasBoxes()) {
            if (config.drawFill && config.fillAlpha > 0) {
                drawFilledBoxes(poseStack, buffers, boxes, offset, config.argb(config.fillAlpha));
            }
            if (config.drawOutline && config.outlineAlpha > 0) {
                drawOutlinedBoxes(poseStack, buffers, boxes, offset, config.argb(config.outlineAlpha));
            }
        }

        if (config.style.hasTracers() && config.tracerAlpha > 0) {
            drawTracers(poseStack, buffers, boxes, offset, partialTick, config.argb(config.tracerAlpha));
        }
    }

    private static void drawFilledBoxes(PoseStack poseStack, BufferSource buffers, List<AABB> boxes, Vec3 offset, int color) {
        VertexConsumer buffer = buffers.getBuffer(ShelfEspRenderTypes.ESP_QUADS);
        for (AABB box : boxes) {
            drawFilledBox(poseStack, buffer, box.move(offset), color);
        }
        buffers.endBatch(ShelfEspRenderTypes.ESP_QUADS);
    }

    private static void drawOutlinedBoxes(PoseStack poseStack, BufferSource buffers, List<AABB> boxes, Vec3 offset, int color) {
        VertexConsumer buffer = buffers.getBuffer(ShelfEspRenderTypes.ESP_LINES);
        for (AABB box : boxes) {
            drawOutlinedBox(poseStack, buffer, box.move(offset), color);
        }
        buffers.endBatch(ShelfEspRenderTypes.ESP_LINES);
    }

    private static void drawTracers(PoseStack poseStack, BufferSource buffers, List<AABB> boxes, Vec3 offset, float partialTick, int color) {
        VertexConsumer buffer = buffers.getBuffer(ShelfEspRenderTypes.ESP_LINES);
        Vec3 start = lookVector(partialTick).scale(10.0D);
        for (AABB box : boxes) {
            drawLine(poseStack, buffer, start, box.getCenter().add(offset), color);
        }
        buffers.endBatch(ShelfEspRenderTypes.ESP_LINES);
    }

    private static Vec3 lookVector(float partialTick) {
        if (CLIENT.player == null) {
            return Vec3.ZERO;
        }

        float yaw = CLIENT.player.getViewYRot(partialTick);
        float pitch = CLIENT.player.getViewXRot(partialTick);
        float radiansPerDegree = (float) (Math.PI / 180.0D);
        float adjustedYaw = -yaw * radiansPerDegree - (float) Math.PI;
        float cosYaw = (float) Math.cos(adjustedYaw);
        float sinYaw = (float) Math.sin(adjustedYaw);
        float adjustedPitch = -pitch * radiansPerDegree;
        float cosPitch = -(float) Math.cos(adjustedPitch);
        float sinPitch = (float) Math.sin(adjustedPitch);

        Vec3 look = new Vec3(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
        return CLIENT.options.getCameraType() == CameraType.THIRD_PERSON_FRONT ? look.reverse() : look;
    }

    private static void drawFilledBox(PoseStack poseStack, VertexConsumer buffer, AABB box, int color) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        addQuad(poseStack, buffer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color);
        addQuad(poseStack, buffer, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, color);
        addQuad(poseStack, buffer, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, color);
        addQuad(poseStack, buffer, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, color);
        addQuad(poseStack, buffer, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, color);
        addQuad(poseStack, buffer, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, color);
    }

    private static void addQuad(PoseStack poseStack, VertexConsumer buffer, float ax, float ay, float az,
            float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, int color) {
        Pose pose = poseStack.last();
        buffer.addVertex(pose, ax, ay, az).setColor(color);
        buffer.addVertex(pose, bx, by, bz).setColor(color);
        buffer.addVertex(pose, cx, cy, cz).setColor(color);
        buffer.addVertex(pose, dx, dy, dz).setColor(color);
    }

    private static void drawOutlinedBox(PoseStack poseStack, VertexConsumer buffer, AABB box, int color) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        drawLine(poseStack, buffer, x1, y1, z1, x2, y1, z1, color);
        drawLine(poseStack, buffer, x2, y1, z1, x2, y1, z2, color);
        drawLine(poseStack, buffer, x2, y1, z2, x1, y1, z2, color);
        drawLine(poseStack, buffer, x1, y1, z2, x1, y1, z1, color);

        drawLine(poseStack, buffer, x1, y2, z1, x2, y2, z1, color);
        drawLine(poseStack, buffer, x2, y2, z1, x2, y2, z2, color);
        drawLine(poseStack, buffer, x2, y2, z2, x1, y2, z2, color);
        drawLine(poseStack, buffer, x1, y2, z2, x1, y2, z1, color);

        drawLine(poseStack, buffer, x1, y1, z1, x1, y2, z1, color);
        drawLine(poseStack, buffer, x2, y1, z1, x2, y2, z1, color);
        drawLine(poseStack, buffer, x2, y1, z2, x2, y2, z2, color);
        drawLine(poseStack, buffer, x1, y1, z2, x1, y2, z2, color);
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer buffer, float x1, float y1, float z1,
            float x2, float y2, float z2, int color) {
        drawLine(poseStack, buffer, new Vec3(x1, y1, z1), new Vec3(x2, y2, z2), color);
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer buffer, Vec3 start, Vec3 end, int color) {
        Vec3 direction = end.subtract(start);
        if (direction.lengthSqr() < 1.0E-8D) {
            return;
        }

        Vec3 normal = direction.normalize();
        Pose pose = poseStack.last();
        buffer.addVertex(pose, (float) start.x, (float) start.y, (float) start.z)
                .setColor(color)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z)
                .setLineWidth(LINE_WIDTH);
        buffer.addVertex(pose, (float) end.x, (float) end.y, (float) end.z)
                .setColor(color)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z)
                .setLineWidth(LINE_WIDTH);
    }
}
