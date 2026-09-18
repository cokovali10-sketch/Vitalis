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

    public boolean healthDisplayEnabled = true;
    public boolean healthShowPlayers = true;
    public boolean healthShowMobs = true;
    public boolean healthShowAnimals = true;
    public boolean healthShowHostile = true;
    public int healthScale = 2;
    public boolean healthShowOnSneak = false;

    public boolean enchantHelperEnabled = true;
    public boolean enchantShowChances = true;
    public boolean enchantShowApplied = true;
    public boolean enchantHighlightBest = true;

    public static VitalisConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static VitalisConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                return GSON.fromJson(reader, VitalisConfig.class);
            } catch (IOException e) {
                System.err.println("[Vitalis] Failed to load config: " + e.getMessage());
            }
        }
        VitalisConfig config = new VitalisConfig();
        config.save();
        return config;
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("[Vitalis] Failed to save config: " + e.getMessage());
        }
    }
}
