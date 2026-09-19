package com.vitalis.hud;

import com.vitalis.config.VitalisConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HealthDisplayRenderer {

    public static final int MODE_BAR = 0;
    public static final int MODE_NUMBERS = 1;
    public static final int MODE_BOTH = 2;
    public static final int MODE_COUNT = 3;

    private static long lastSwitchTime = 0;
    private static String pendingMessage = null;

    public static void render(GuiGraphicsExtractor graphics) {
        renderSwitchNotification(graphics);
    }

    public static void cycleMode() {
        VitalisConfig config = VitalisConfig.get();
        VitalisConfig.HealthConfig hc = config.health;
        hc.displayMode = (hc.displayMode + 1) % MODE_COUNT;
        config.save();

        lastSwitchTime = System.currentTimeMillis();
        pendingMessage = switch (hc.displayMode) {
            case MODE_BAR -> "Health: Bar";
            case MODE_NUMBERS -> "Health: Numbers";
            case MODE_BOTH -> "Health: Bar + Numbers";
            default -> "Health: ???";
        };
    }

    private static void renderSwitchNotification(GuiGraphicsExtractor graphics) {
        if (pendingMessage == null) return;
        long elapsed = System.currentTimeMillis() - lastSwitchTime;
        if (elapsed > 1500) {
            pendingMessage = null;
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();

        float alpha = elapsed < 1200 ? 1.0f : 1.0f - (elapsed - 1200) / 300.0f;
        int alphaByte = (int) (alpha * 255) << 24;
        int bgColor = alphaByte | 0x000000;
        int textColor = alphaByte | 0xFFFFFF;

        int textWidth = font.width(pendingMessage);
        int x = scaledWidth / 2 - textWidth / 2;
        int y = scaledHeight / 2 + 20;

        graphics.fill(x - 4, y - 2, x + textWidth + 4, y + font.lineHeight + 2, bgColor);
        graphics.text(font, pendingMessage, x, y, textColor, true);
    }
}
