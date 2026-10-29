package dev.shelfesp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;

/** Loads and saves {@link ShelfEspConfig}; a missing or broken file falls back to defaults. */
public final class ShelfEspConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final Logger logger;
    private ShelfEspConfig config = new ShelfEspConfig();

    public ShelfEspConfigManager(Path path, Logger logger) {
        this.path = path;
        this.logger = logger;
    }

    public ShelfEspConfig get() {
        return config;
    }

    public void load() {
        if (!Files.isRegularFile(path)) {
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ShelfEspConfig loaded = GSON.fromJson(reader, ShelfEspConfig.class);
            if (loaded == null) {
                throw new JsonParseException("Config file is empty");
            }
            loaded.validate();
            config = loaded;
        } catch (IOException | JsonParseException e) {
            logger.warn("Could not read {}, using defaults", path, e);
            config = new ShelfEspConfig();
            save();
        }
    }

    public void save() {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            logger.warn("Could not save {}", path, e);
        }
    }
}
