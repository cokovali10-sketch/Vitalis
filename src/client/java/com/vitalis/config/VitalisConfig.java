package com.vitalis.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class VitalisConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("vitalis.json");

    private static VitalisConfig INSTANCE;

    public HealthConfig health = new HealthConfig();
    public EnchantConfig enchant = new EnchantConfig();
    public OverlayConfig overlay = new OverlayConfig();
    public TargetConfig target = new TargetConfig();
    public ArmorConfig armor = new ArmorConfig();
    public PotionConfig potion = new PotionConfig();

    public static class HealthConfig {
        public boolean enabled = true;
        public boolean showPlayers = true;
        public boolean showHostile = true;
        public boolean showAnimals = true;
        public boolean showOthers = true;
        public boolean showOnSneak = false;
        public boolean showBar = true;
        public boolean showNumbers = true;
        public boolean showAbsorption = true;
        public int maxDistance = 24;
        public float yOffset = 0.35f;
    }

    public static class EnchantConfig {
        public boolean enabled = true;
        public boolean showChances = true;
        public boolean showLevels = true;
        public boolean highlightBest = true;
    }

    public static class OverlayConfig {
        public boolean enabled = true;
        public boolean showCoords = true;
        public boolean showDirection = true;
        public boolean showFps = false;
        public boolean showLightLevel = true;
        public boolean showBiome = false;
        public boolean showClock = false;
        public int positionX = 4;
        public int positionY = 4;
    }

    public static class TargetConfig {
        public boolean enabled = true;
        public boolean showName = true;
        public boolean showHealth = true;
        public boolean showType = true;
        public boolean showDistance = true;
        public boolean showEffects = true;
        public int maxDistance = 8;
    }

    public static class ArmorConfig {
        public boolean enabled = true;
        public boolean showDurability = true;
        public boolean showPercentage = true;
        public boolean colorByDurability = true;
    }

    public static class PotionConfig {
        public boolean enabled = true;
        public boolean showDuration = true;
        public boolean showAmplifier = true;
        public boolean showIcon = true;
    }

    public static VitalisConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static VitalisConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                VitalisConfig loaded = GSON.fromJson(reader, VitalisConfig.class);
                if (loaded != null) return loaded;
            } catch (IOException e) {
                System.err.println("[Vitalis] Failed to load config: " + e.getMessage());
            }
        }
        VitalisConfig config = new VitalisConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[Vitalis] Failed to save config: " + e.getMessage());
        }
    }
}
