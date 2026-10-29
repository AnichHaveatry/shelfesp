package dev.shelfesp;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Finds loaded shelf blocks that currently hold at least one item. */
public final class ShelfScanner {
    private ShelfScanner() {
    }

    public static List<AABB> scan(Minecraft client, ShelfEspConfig config) {
        if (client.level == null || client.player == null) {
            return List.of();
        }

        int renderDistance = Math.max(2, client.options.getEffectiveRenderDistance());
        int distanceChunks = (int) Math.ceil(config.maxDistance / 16.0D) + 1;
        int radius = Math.min(renderDistance + 2, distanceChunks);
        ChunkPos center = client.player.chunkPosition();
        double maxDistanceSq = config.maxDistance * config.maxDistance;
        Vec3 eye = client.player.position();
        List<AABB> boxes = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int chunkX = center.x() + dx;
                int chunkZ = center.z() + dz;
                if (!client.level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                LevelChunk chunk = client.level.getChunk(chunkX, chunkZ);
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof ShelfBlockEntity shelf) || !hasItems(shelf)) {
                        continue;
                    }

                    BlockPos pos = shelf.getBlockPos();
                    if (pos.getCenter().distanceToSqr(eye) > maxDistanceSq) {
                        continue;
                    }

                    BlockState state = shelf.getBlockState();
                    VoxelShape shape = state.getShape(client.level, pos);
                    if (shape.isEmpty()) {
                        continue;
                    }

                    boxes.add(shape.bounds().move(pos).inflate(0.003D));
                }
            }
        }

        return List.copyOf(boxes);
    }

    public static boolean hasItems(ShelfBlockEntity shelf) {
        for (int slot = 0; slot < shelf.getItems().size(); slot++) {
            if (!shelf.getItems().get(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
