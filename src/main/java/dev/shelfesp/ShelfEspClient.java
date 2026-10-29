package dev.shelfesp;

import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side entry point. Every client tick the loaded shelves are rescanned
 * (at a configurable interval) and the resulting boxes are handed to the
 * renderer, which draws them through walls.
 */
public final class ShelfEspClient implements ClientModInitializer {
    public static final String MOD_ID = "shelfesp";
    public static final Logger LOGGER = LoggerFactory.getLogger("ShelfESP");

    private static ShelfEspClient instance;

    private final Minecraft client = Minecraft.getInstance();
    private ShelfEspConfigManager configManager;
    private ShelfEspConfig config;
    private KeyMapping toggleKey;
    private volatile List<AABB> boxes = List.of();
    private int tickCounter;

    @Override
    public void onInitializeClient() {
        instance = this;

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
        configManager = new ShelfEspConfigManager(configPath, LOGGER);
        configManager.load();
        config = configManager.get();
        tickCounter = config.updateIntervalTicks;

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.shelfesp.toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_H,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        LOGGER.info("ShelfESP initialised; config at {}", configPath);
    }

    private void onEndClientTick(Minecraft minecraft) {
        while (toggleKey.consumeClick()) {
            toggle(minecraft);
        }

        if (!config.enabled) {
            boxes = List.of();
            return;
        }

        if (tickCounter++ >= config.updateIntervalTicks) {
            tickCounter = 0;
            refresh(minecraft);
        }
    }

    private void toggle(Minecraft minecraft) {
        config.enabled = !config.enabled;
        configManager.save();
        tickCounter = 0;

        if (config.enabled) {
            refresh(minecraft);
        } else {
            boxes = List.of();
        }

        if (minecraft.player != null) {
            minecraft.player.sendOverlayMessage(
                    Component.translatable(config.enabled ? "shelfesp.toggle.on" : "shelfesp.toggle.off"));
        }
    }

    private void refresh(Minecraft minecraft) {
        boxes = ShelfScanner.scan(minecraft, config);
    }

    public static ShelfEspClient getInstance() {
        return instance;
    }

    public ShelfEspConfig getConfig() {
        return config;
    }

    public List<AABB> getBoxes() {
        return boxes;
    }

    public boolean isEnabled() {
        return config.enabled;
    }
}
