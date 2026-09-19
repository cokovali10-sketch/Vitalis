package com.vitalis.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.BiConsumer;

public class VitalisConfigScreen extends Screen {

    private final Screen parent;
    private final VitalisConfig config;
    private int scrollOffset = 0;

    private static final List<ConfigEntry> ENTRIES = new ArrayList<>();

    static {
        ENTRIES.add(header("health", "=== Health Display ==="));
        ENTRIES.add(toggle("health.enabled", "Enabled", c -> c.health.enabled, (c, v) -> c.health.enabled = v));
        ENTRIES.add(mode("health.displayMode", "Display Mode", c -> c.health.displayMode, (c, v) -> c.health.displayMode = v));
        ENTRIES.add(toggle("health.showPlayers", "Show Players", c -> c.health.showPlayers, (c, v) -> c.health.showPlayers = v));
        ENTRIES.add(toggle("health.showHostile", "Show Hostile", c -> c.health.showHostile, (c, v) -> c.health.showHostile = v));
        ENTRIES.add(toggle("health.showAnimals", "Show Animals", c -> c.health.showAnimals, (c, v) -> c.health.showAnimals = v));
        ENTRIES.add(toggle("health.showOthers", "Show Others", c -> c.health.showOthers, (c, v) -> c.health.showOthers = v));
        ENTRIES.add(toggle("health.showOnSneak", "Show Only on Sneak", c -> c.health.showOnSneak, (c, v) -> c.health.showOnSneak = v));
        ENTRIES.add(toggle("health.showAbsorption", "Show Absorption", c -> c.health.showAbsorption, (c, v) -> c.health.showAbsorption = v));

        ENTRIES.add(header("enchant", "=== Enchantment Helper ==="));
        ENTRIES.add(toggle("enchant.enabled", "Enabled", c -> c.enchant.enabled, (c, v) -> c.enchant.enabled = v));
        ENTRIES.add(toggle("enchant.showChances", "Show Chances", c -> c.enchant.showChances, (c, v) -> c.enchant.showChances = v));
        ENTRIES.add(toggle("enchant.showLevels", "Show Levels", c -> c.enchant.showLevels, (c, v) -> c.enchant.showLevels = v));
        ENTRIES.add(toggle("enchant.highlightBest", "Highlight Best", c -> c.enchant.highlightBest, (c, v) -> c.enchant.highlightBest = v));

        ENTRIES.add(header("overlay", "=== Compact HUD ==="));
        ENTRIES.add(toggle("overlay.enabled", "Enabled", c -> c.overlay.enabled, (c, v) -> c.overlay.enabled = v));
        ENTRIES.add(toggle("overlay.showCoords", "Show Coords", c -> c.overlay.showCoords, (c, v) -> c.overlay.showCoords = v));
        ENTRIES.add(toggle("overlay.showDirection", "Show Direction", c -> c.overlay.showDirection, (c, v) -> c.overlay.showDirection = v));
        ENTRIES.add(toggle("overlay.showFps", "Show FPS", c -> c.overlay.showFps, (c, v) -> c.overlay.showFps = v));
        ENTRIES.add(toggle("overlay.showLightLevel", "Show Light Level", c -> c.overlay.showLightLevel, (c, v) -> c.overlay.showLightLevel = v));

        ENTRIES.add(header("target", "=== Target Info ==="));
        ENTRIES.add(toggle("target.enabled", "Enabled", c -> c.target.enabled, (c, v) -> c.target.enabled = v));
        ENTRIES.add(toggle("target.showName", "Show Name", c -> c.target.showName, (c, v) -> c.target.showName = v));
        ENTRIES.add(toggle("target.showHealth", "Show Health", c -> c.target.showHealth, (c, v) -> c.target.showHealth = v));
        ENTRIES.add(toggle("target.showType", "Show Type", c -> c.target.showType, (c, v) -> c.target.showType = v));
        ENTRIES.add(toggle("target.showDistance", "Show Distance", c -> c.target.showDistance, (c, v) -> c.target.showDistance = v));

        ENTRIES.add(header("armor", "=== Armor Durability ==="));
        ENTRIES.add(toggle("armor.enabled", "Enabled", c -> c.armor.enabled, (c, v) -> c.armor.enabled = v));
        ENTRIES.add(toggle("armor.showDurability", "Show Durability", c -> c.armor.showDurability, (c, v) -> c.armor.showDurability = v));
        ENTRIES.add(toggle("armor.showPercentage", "Show Percentage", c -> c.armor.showPercentage, (c, v) -> c.armor.showPercentage = v));
        ENTRIES.add(toggle("armor.colorByDurability", "Color by Durability", c -> c.armor.colorByDurability, (c, v) -> c.armor.colorByDurability = v));

        ENTRIES.add(header("potion", "=== Potion Timer ==="));
        ENTRIES.add(toggle("potion.enabled", "Enabled", c -> c.potion.enabled, (c, v) -> c.potion.enabled = v));
        ENTRIES.add(toggle("potion.showDuration", "Show Duration", c -> c.potion.showDuration, (c, v) -> c.potion.showDuration = v));
        ENTRIES.add(toggle("potion.showAmplifier", "Show Amplifier", c -> c.potion.showAmplifier, (c, v) -> c.potion.showAmplifier = v));
    }

    public VitalisConfigScreen(Screen parent) {
        super(net.minecraft.network.chat.Component.literal("Vitalis Settings"));
        this.parent = parent;
        this.config = VitalisConfig.get();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        Font font = this.font;
        int centerX = this.width / 2;

        graphics.fill(0, 0, this.width, this.height, 0xC0101010);

        graphics.text(font, "Vitalis Settings", centerX - font.width("Vitalis Settings") / 2, 8, 0xFFDDAA33, true);
        graphics.text(font, "\u00A77Click to toggle/change", centerX - font.width("\u00A77Click to toggle/change") / 2, 20, 0x888888, true);

        int startY = 36;
        int lineH = 14;
        int maxVisible = (this.height - startY - 20) / lineH;

        int maxScroll = Math.max(0, ENTRIES.size() - maxVisible);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        for (int i = 0; i < maxVisible && (i + scrollOffset) < ENTRIES.size(); i++) {
            int idx = i + scrollOffset;
            ConfigEntry entry = ENTRIES.get(idx);
            int y = startY + i * lineH;

            if (entry.type == EntryType.HEADER) {
                graphics.text(font, entry.label, centerX - font.width(entry.label) / 2, y + 2, 0xFFDDAA33, true);
                continue;
            }

            int valueX = centerX + 60;
            String valueStr = getValueString(entry);
            int valueColor = getValueColor(entry);

            graphics.text(font, entry.label, centerX - 80, y + 2, 0xFFFFFF, true);
            graphics.fill(valueX - 2, y, valueX + font.width(valueStr) + 4, y + font.lineHeight + 2, 0x40FFFFFF);
            graphics.text(font, valueStr, valueX, y + 1, valueColor, true);

            if (mouseX >= valueX - 2 && mouseX <= valueX + font.width(valueStr) + 4
                    && mouseY >= y && mouseY <= y + font.lineHeight + 2) {
                graphics.fill(valueX - 3, y - 1, valueX + font.width(valueStr) + 5, y + font.lineHeight + 3, 0x60FFFFFF);
            }
        }

        if (scrollOffset > 0) {
            graphics.text(font, "\u25B2", this.width - 16, startY - 2, 0xAAAAAA, true);
        }
        if (scrollOffset < maxScroll) {
            graphics.text(font, "\u25BC", this.width - 16, startY + maxVisible * lineH, 0xAAAAAA, true);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean held) {
        if (event.button() != 0) return super.mouseClicked(event, held);

        Font font = this.font;
        int centerX = this.width / 2;
        int startY = 36;
        int lineH = 14;
        int maxVisible = (this.height - startY - 20) / lineH;
        double mouseX = event.x();
        double mouseY = event.y();

        for (int i = 0; i < maxVisible && (i + scrollOffset) < ENTRIES.size(); i++) {
            int idx = i + scrollOffset;
            ConfigEntry entry = ENTRIES.get(idx);
            if (entry.type == EntryType.HEADER) continue;

            int y = startY + i * lineH;
            int valueX = centerX + 60;
            String valueStr = getValueString(entry);

            if (mouseX >= valueX - 2 && mouseX <= valueX + font.width(valueStr) + 4
                    && mouseY >= y && mouseY <= y + font.lineHeight + 2) {
                toggleValue(entry);
                config.save();
                return true;
            }
        }

        return super.mouseClicked(event, held);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int) verticalAmount;
        return true;
    }

    @Override
    public void onClose() {
        config.save();
        Minecraft.getInstance().gui.setScreen(parent);
    }

    private String getValueString(ConfigEntry entry) {
        if (entry.type == EntryType.TOGGLE) {
            return entry.getBoolean(config) ? "\u00A7aON" : "\u00A7cOFF";
        } else if (entry.type == EntryType.MODE) {
            return switch (entry.getInt(config)) {
                case 0 -> "\u00A76Bar";
                case 1 -> "\u00A7bNumbers";
                case 2 -> "\u00A7eBoth";
                default -> "???";
            };
        }
        return "";
    }

    private int getValueColor(ConfigEntry entry) {
        if (entry.type == EntryType.TOGGLE) {
            return entry.getBoolean(config) ? 0x55FF55 : 0xFF5555;
        } else if (entry.type == EntryType.MODE) {
            return switch (entry.getInt(config)) {
                case 0 -> 0xDDAA33;
                case 1 -> 0x55FFFF;
                case 2 -> 0xFFFF55;
                default -> 0xFFFFFF;
            };
        }
        return 0xFFFFFF;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void toggleValue(ConfigEntry entry) {
        if (entry.type == EntryType.TOGGLE) {
            boolean current = entry.getBoolean(config);
            ((BiConsumer) entry.setter).accept(config, !current);
        } else if (entry.type == EntryType.MODE) {
            int current = entry.getInt(config);
            ((BiConsumer) entry.setter).accept(config, (current + 1) % 3);
        }
    }

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        Screen current = mc.gui != null ? mc.gui.screen() : null;
        mc.gui.setScreen(new VitalisConfigScreen(current));
    }

    private static ConfigEntry header(String id, String label) {
        return new ConfigEntry(id, label, EntryType.HEADER, null, null);
    }

    @SuppressWarnings("unchecked")
    private static ConfigEntry toggle(String id, String label,
                                      Function<VitalisConfig, Boolean> getter,
                                      BiConsumer<VitalisConfig, Boolean> setter) {
        return new ConfigEntry(id, label, EntryType.TOGGLE,
                (Function<VitalisConfig, Object>) (Function<?, ?>) getter,
                (BiConsumer<VitalisConfig, Object>) (BiConsumer<?, ?>) setter);
    }

    @SuppressWarnings("unchecked")
    private static ConfigEntry mode(String id, String label,
                                    Function<VitalisConfig, Integer> getter,
                                    BiConsumer<VitalisConfig, Integer> setter) {
        return new ConfigEntry(id, label, EntryType.MODE,
                (Function<VitalisConfig, Object>) (Function<?, ?>) getter,
                (BiConsumer<VitalisConfig, Object>) (BiConsumer<?, ?>) setter);
    }

    private record ConfigEntry(String id, String label, EntryType type,
                               Function<VitalisConfig, Object> getter,
                               BiConsumer<VitalisConfig, Object> setter) {

        boolean getBoolean(VitalisConfig config) {
            return getter != null && (Boolean) getter.apply(config);
        }

        int getInt(VitalisConfig config) {
            return getter != null ? (int) getter.apply(config) : 0;
        }
    }

    private enum EntryType {
        HEADER, TOGGLE, MODE
    }
}
