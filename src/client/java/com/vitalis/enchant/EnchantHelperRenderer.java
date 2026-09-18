package com.vitalis.enchant;

import com.vitalis.config.VitalisConfig;
import com.vitalis.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantHelperRenderer {

    private static EnchantmentScreen lastScreen = null;
    private static List<EnchantInfo> cachedEnchants = null;
    private static ItemStack cachedStack = null;

    public static void onScreenTick(net.minecraft.client.gui.screens.Screen screen) {
        if (screen instanceof EnchantmentScreen enchantScreen) {
            if (enchantScreen != lastScreen) {
                lastScreen = enchantScreen;
                cachedEnchants = null;
                cachedStack = null;
            }
            updateCache(enchantScreen);
        } else {
            lastScreen = null;
            cachedEnchants = null;
            cachedStack = null;
        }
    }

    private static void updateCache(EnchantmentScreen screen) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        EnchantmentMenu menu = screen.getMenu();
        ItemStack stack = menu.getSlot(0).getItem();
        if (stack.isEmpty()) {
            cachedEnchants = null;
            cachedStack = null;
            return;
        }

        if (stack == cachedStack && cachedEnchants != null) return;

        cachedStack = stack;
        cachedEnchants = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            int power = menu.costs[i];
            if (power <= 0) continue;

            int enchantLevel = getEnchantLevel(power);
            if (enchantLevel <= 0) continue;

            List<Map.Entry<Enchantment, Integer>> enchants = getEnchantmentsForSlot(client, stack, i, enchantLevel);

            for (Map.Entry<Enchantment, Integer> entry : enchants) {
                Enchantment ench = entry.getKey();
                int level = entry.getValue();
                float chance = calculateChance(power, enchantLevel);

                cachedEnchants.add(new EnchantInfo(ench, level, chance, i, power));
            }
        }
    }

    private static int getEnchantLevel(int power) {
        if (power < 2) return 1;
        if (power < 5) return 2;
        if (power < 9) return 3;
        if (power < 15) return 4;
        return 5;
    }

    private static List<Map.Entry<Enchantment, Integer>> getEnchantmentsForSlot(Minecraft client, ItemStack stack, int slot, int level) {
        List<Map.Entry<Enchantment, Integer>> result = new ArrayList<>();

        Registry<Enchantment> enchRegistry = client.level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);

        for (Enchantment ench : enchRegistry) {
            if (ench.getWeight() > 0) {
                int maxLevel = ench.getMaxLevel();
                if (level > 0 && level <= maxLevel) {
                    result.add(Map.entry(ench, Math.min(level, maxLevel)));
                }
            }
        }

        if (result.size() > 3) {
            result = result.subList(0, 3);
        }

        return result;
    }

    private static float calculateChance(int power, int level) {
        float base = Math.min(power / 30.0f, 1.0f);
        float levelBonus = level / 5.0f;
        return Math.min(base * 0.7f + levelBonus * 0.3f, 1.0f);
    }

    public static void renderEnchantInfo(GuiGraphicsExtractor graphics, EnchantmentScreen screen, int mouseX, int mouseY) {
        VitalisConfig config = VitalisConfig.get();
        if (!config.enchantHelperEnabled) return;
        if (cachedEnchants == null || cachedEnchants.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        Font textRenderer = client.font;

        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        int x = accessor.getLeftPos() + 195;
        int y = accessor.getTopPos() + 4;

        int panelWidth = 160;
        int lineHeight = textRenderer.lineHeight + 2;
        int totalHeight = 10 + cachedEnchants.size() * (lineHeight + 4) + 6;

        graphics.fill(x - 2, y - 2, x + panelWidth + 2, y + totalHeight, 0xE0101010);
        graphics.fill(x - 1, y - 1, x + panelWidth + 1, y + totalHeight - 1, 0xE0202020);

        MutableComponent title = Component.literal("Enchantment Info").withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD);
        graphics.text(textRenderer, title, x + 2, y + 2, 0xFFFFFF, true);
        y += 10;

        for (EnchantInfo info : cachedEnchants) {
            String enchName = info.enchantment.description().getString();
            MutableComponent name = Component.literal(enchName + " ").withStyle(net.minecraft.ChatFormatting.AQUA);

            String chanceText = String.format("%.0f%%", info.chance * 100);
            net.minecraft.ChatFormatting chanceColor;
            if (info.chance >= 0.8f) chanceColor = net.minecraft.ChatFormatting.GREEN;
            else if (info.chance >= 0.5f) chanceColor = net.minecraft.ChatFormatting.YELLOW;
            else chanceColor = net.minecraft.ChatFormatting.RED;

            name.append(Component.literal(chanceText).withStyle(chanceColor));

            int indent = info.slot * 4;
            graphics.text(textRenderer, name, x + 4 + indent, y, 0xFFFFFF, true);

            if (config.enchantShowApplied) {
                MutableComponent levelText = Component.literal("  Lv." + info.level).withStyle(net.minecraft.ChatFormatting.GRAY);
                graphics.text(textRenderer, levelText, x + 4 + indent + textRenderer.width(name), y, 0xAAAAAA, true);
            }

            y += lineHeight + 2;
        }
    }

    private static class EnchantInfo {
        Enchantment enchantment;
        int level;
        float chance;
        int slot;
        int power;

        EnchantInfo(Enchantment enchantment, int level, float chance, int slot, int power) {
            this.enchantment = enchantment;
            this.level = level;
            this.chance = chance;
            this.slot = slot;
            this.power = power;
        }
    }
}
