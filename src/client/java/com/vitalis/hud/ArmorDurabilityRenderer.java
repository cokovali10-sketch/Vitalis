package com.vitalis.hud;

import com.vitalis.config.VitalisConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.List;

public class ArmorDurabilityRenderer {

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (client.gui != null && client.gui.hud != null && client.gui.hud.isHidden()) return;

        VitalisConfig config = VitalisConfig.get();
        VitalisConfig.ArmorConfig ac = config.armor;
        if (!ac.enabled) return;

        Font font = client.font;
        int scaledWidth = client.getWindow().getGuiScaledWidth();

        List<ArmorEntry> armors = new ArrayList<>();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
                if (equippable != null && equippable.slot() == slot) {
                    int maxDur = stack.getMaxDamage();
                    int curDur = maxDur - stack.getDamageValue();
                    float ratio = maxDur > 0 ? (float) curDur / maxDur : 0;
                    armors.add(new ArmorEntry(slot, stack, curDur, maxDur, ratio));
                }
            }
        }

        if (armors.isEmpty()) return;

        int x = scaledWidth - 4;
        int y = client.getWindow().getGuiScaledHeight() / 2 - (armors.size() * 10) / 2;

        for (ArmorEntry entry : armors) {
            int color = ac.colorByDurability ? getDurabilityColor(entry.ratio) : 0xFFFFFF;

            String icon = getArmorIcon(entry.slot);
            String durText = String.format("%s %d/%d", icon, entry.currentDurability, entry.maxDurability);
            if (ac.showPercentage) {
                durText += String.format(" (%d%%)", (int) (entry.ratio * 100));
            }

            int textWidth = font.width(durText);

            graphics.fill(x - textWidth - 3, y - 1, x + 1, y + font.lineHeight + 1, 0x80000000);
            graphics.text(font, durText, x - textWidth, y, color, true);

            y += 12;
        }
    }

    private static int getDurabilityColor(float ratio) {
        if (ratio > 0.6f) return 0x55FF55;
        if (ratio > 0.3f) return 0xFFFF55;
        if (ratio > 0.1f) return 0xFF8800;
        return 0xFF5555;
    }

    private static String getArmorIcon(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "\u2666";
            case CHEST -> "\u2663";
            case LEGS -> "\u2660";
            case FEET -> "\u2665";
            default -> "?";
        };
    }

    private record ArmorEntry(EquipmentSlot slot, ItemStack stack, int currentDurability, int maxDurability, float ratio) {}
}
