package com.vitalis.hud;

import com.vitalis.config.VitalisConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PotionTimerRenderer {

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (client.gui != null && client.gui.hud != null && client.gui.hud.isHidden()) return;

        VitalisConfig config = VitalisConfig.get();
        VitalisConfig.PotionConfig pc = config.potion;
        if (!pc.enabled) return;

        Collection<MobEffectInstance> effects = client.player.getActiveEffects();
        if (effects.isEmpty()) return;

        Font font = client.font;
        int scaledWidth = client.getWindow().getGuiScaledWidth();

        List<MobEffectInstance> sortedEffects = new ArrayList<>(effects);
        sortedEffects.sort(Comparator.comparingInt(e -> -e.getDuration()));

        int x = scaledWidth - 4;
        int y = client.getWindow().getGuiScaledHeight() / 2 - (sortedEffects.size() * 10) / 2;

        for (MobEffectInstance effect : sortedEffects) {
            Holder<MobEffect> effectHolder = effect.getEffect();
            MobEffect effectType = effectHolder.value();
            String name = effectType.getDisplayName().getString();

            int amplifier = effect.getAmplifier();
            String amplifierText = amplifier > 0 ? " (" + getAmplifierName(amplifier) + ")" : "";

            int durationTicks = effect.getDuration();
            String durationText;
            if (durationTicks == Integer.MAX_VALUE || durationTicks < 0) {
                durationText = "\u221E";
            } else {
                int seconds = durationTicks / 20;
                int minutes = seconds / 60;
                int secs = seconds % 60;
                durationText = minutes > 0 ? String.format("%d:%02d", minutes, secs) : seconds + "s";
            }

            int color = effectType.getColor();

            String lineText = name + amplifierText + " " + durationText;
            int textWidth = font.width(lineText);

            graphics.fill(x - textWidth - 3, y - 1, x + 1, y + font.lineHeight + 1, 0x80000000);
            graphics.text(font, lineText, x - textWidth, y, color, true);

            y += 12;
        }
    }

    private static String getAmplifierName(int amplifier) {
        return switch (amplifier) {
            case 0 -> "II";
            case 1 -> "III";
            case 2 -> "IV";
            case 3 -> "V";
            default -> "Lv." + (amplifier + 1);
        };
    }
}
