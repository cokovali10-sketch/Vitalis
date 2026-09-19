package com.vitalis.hud;

import com.vitalis.config.VitalisConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class CompactHUDRenderer {

    private static int fpsCounter = 0;
    private static int lastFps = 0;
    private static long lastFpsTime = System.currentTimeMillis();

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (client.gui != null && client.gui.hud != null && client.gui.hud.isHidden()) return;

        VitalisConfig config = VitalisConfig.get();
        VitalisConfig.OverlayConfig oc = config.overlay;
        if (!oc.enabled) return;

        Font font = client.font;
        Player player = client.player;

        updateFps();

        int x = oc.positionX;
        int y = oc.positionY;
        int lineH = font.lineHeight + 1;

        if (oc.showFps) {
            String fpsText = lastFps + " FPS";
            drawCompactLabel(graphics, font, fpsText, x, y, getFpsColor(lastFps));
            y += lineH;
        }

        if (oc.showCoords) {
            int px = (int) player.getX();
            int py = (int) player.getY();
            int pz = (int) player.getZ();
            String coordText = String.format("XYZ: %d / %d / %d", px, py, pz);
            drawCompactLabel(graphics, font, coordText, x, y, 0xAAAAAA);
            y += lineH;
        }

        if (oc.showDirection) {
            String dir = getDirection(player.getYRot());
            String dirText = "Facing: " + dir;
            drawCompactLabel(graphics, font, dirText, x, y, 0xAAAAAA);
            y += lineH;
        }

        if (oc.showLightLevel) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                BlockPos pos = player.blockPosition();
                int light = mc.level.getMaxLocalRawBrightness(pos);
                int skyLight = mc.level.getRawBrightness(pos, 0);
                String lightText = "Light: " + light + " / " + skyLight;
                int lightColor = light < 7 ? 0xFF5555 : (light < 12 ? 0xFFFF55 : 0x55FF55);
                drawCompactLabel(graphics, font, lightText, x, y, lightColor);
            }
        }
    }

    private static void drawCompactLabel(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
        int w = font.width(text);
        graphics.fill(x - 1, y - 1, x + w + 2, y + font.lineHeight + 1, 0x60000000);
        graphics.text(font, text, x, y, color, true);
    }

    private static void updateFps() {
        fpsCounter++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            lastFps = fpsCounter;
            fpsCounter = 0;
            lastFpsTime = now;
        }
    }

    private static int getFpsColor(int fps) {
        if (fps >= 60) return 0x55FF55;
        if (fps >= 30) return 0xFFFF55;
        return 0xFF5555;
    }

    private static String getDirection(float yaw) {
        int dir = Mth.floor((yaw % 360 + 360) % 360 / 45.0F + 0.5F) & 7;
        return switch (dir) {
            case 0 -> "S";
            case 1 -> "SW";
            case 2 -> "W";
            case 3 -> "NW";
            case 4 -> "N";
            case 5 -> "NE";
            case 6 -> "E";
            case 7 -> "SE";
            default -> "?";
        };
    }
}
