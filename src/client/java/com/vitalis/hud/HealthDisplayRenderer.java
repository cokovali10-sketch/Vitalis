package com.vitalis.hud;

import com.vitalis.config.VitalisConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HealthDisplayRenderer {

    public static final int MODE_BAR = 0;
    public static final int MODE_NUMBERS = 1;
    public static final int MODE_BOTH = 2;
    public static final int MODE_COUNT = 3;

    private static long lastSwitchTime = 0;
    private static String pendingMessage = null;

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || client.getCameraEntity() == null) return;
        if (client.gui != null && client.gui.hud != null && client.gui.hud.isHidden()) return;

        VitalisConfig config = VitalisConfig.get();
        VitalisConfig.HealthConfig hc = config.health;
        if (!hc.enabled) return;
        if (hc.showOnSneak && !client.player.isShiftKeyDown()) return;

        Entity cameraEntity = client.getCameraEntity();
        double camX = cameraEntity.getX();
        double camY = cameraEntity.getY();
        double camZ = cameraEntity.getZ();

        double d = hc.maxDistance;
        List<LivingEntity> entities = client.level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(camX - d, camY - d, camZ - d, camX + d, camY + d, camZ + d),
                e -> true);

        for (LivingEntity entity : entities) {
            if (entity == cameraEntity) continue;
            if (entity.isRemoved()) continue;
            if (entity.distanceTo(cameraEntity) > hc.maxDistance) continue;

            boolean show = false;
            if (entity instanceof Player && hc.showPlayers) show = true;
            else if (entity instanceof Monster && hc.showHostile) show = true;
            else if (entity instanceof Animal && hc.showAnimals) show = true;
            else if (hc.showOthers) show = true;

            if (!show) continue;

            renderHealthAbove(graphics, entity, client, hc);
        }

        renderSwitchNotification(graphics, client);
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

    private static void renderSwitchNotification(GuiGraphicsExtractor graphics, Minecraft client) {
        if (pendingMessage == null) return;
        long elapsed = System.currentTimeMillis() - lastSwitchTime;
        if (elapsed > 1500) {
            pendingMessage = null;
            return;
        }

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

    private static void renderHealthAbove(GuiGraphicsExtractor graphics, LivingEntity entity, Minecraft client, VitalisConfig.HealthConfig hc) {
        Font font = client.font;

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float absorption = entity.getAbsorptionAmount();
        int healthInt = Mth.ceil(health);
        int maxHealthInt = Mth.ceil(maxHealth);

        double x = entity.getX();
        double y = entity.getY() + entity.getEyeHeight() + hc.yOffset;
        double z = entity.getZ();

        int[] screenPos = worldToScreen(client, x, y, z);
        if (screenPos == null) return;

        int screenX = screenPos[0];
        int screenY = screenPos[1];

        float ratio = maxHealth > 0 ? health / maxHealth : 0;
        int healthColor = getHealthColor(ratio);

        boolean showBar = hc.displayMode == MODE_BAR || hc.displayMode == MODE_BOTH;
        boolean showNum = hc.displayMode == MODE_NUMBERS || hc.displayMode == MODE_BOTH;

        if (showBar) {
            renderHealthBar(graphics, font, screenX, screenY, health, maxHealth, absorption, healthColor);
            if (showNum) {
                String numText = healthInt + "/" + maxHealthInt;
                if (hc.showAbsorption && absorption > 0) {
                    numText += "+" + Mth.ceil(absorption);
                }
                int textWidth = font.width(numText);
                graphics.text(font, numText, screenX - textWidth / 2, screenY - 11, 0xFFFFFF, true);
            }
        } else if (showNum) {
            String numText = healthInt + "/" + maxHealthInt;
            if (hc.showAbsorption && absorption > 0) {
                numText += "+" + Mth.ceil(absorption);
            }
            int textWidth = font.width(numText);

            graphics.fill(screenX - textWidth / 2 - 2, screenY - 2,
                    screenX + textWidth / 2 + 2, screenY + font.lineHeight + 2, 0x80000000);
            graphics.text(font, numText, screenX - textWidth / 2, screenY, healthColor, true);
        }
    }

    private static void renderHealthBar(GuiGraphicsExtractor graphics, Font font, int centerX, int y, float health, float maxHealth, float absorption, int color) {
        int barWidth = 40;
        int barHeight = 4;
        int barX = centerX - barWidth / 2;
        int barY = y;

        graphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xA0000000);

        float ratio = maxHealth > 0 ? Math.min(health / maxHealth, 1.0f) : 0;
        int filled = (int) (ratio * barWidth);
        graphics.fill(barX, barY, barX + filled, barY + barHeight, color);

        if (absorption > 0 && maxHealth > 0) {
            float absRatio = Math.min(absorption / maxHealth, 1.0f - ratio);
            int absFilled = (int) (absRatio * barWidth);
            graphics.fill(barX + filled, barY, barX + filled + absFilled, barY + barHeight, 0xFFFF55);
        }
    }

    private static int getHealthColor(float ratio) {
        if (ratio > 0.6f) return 0x55FF55;
        if (ratio > 0.3f) return 0xFFFF55;
        return 0xFF5555;
    }

    private static int[] worldToScreen(Minecraft client, double wx, double wy, double wz) {
        net.minecraft.client.Camera camera = client.gameRenderer.mainCamera();
        if (!camera.isInitialized()) return null;

        Vec3 pos = camera.position();
        double dx = wx - pos.x;
        double dy = wy - pos.y;
        double dz = wz - pos.z;

        float yawRad = (float) Math.toRadians(-camera.yRot());
        float pitchRad = (float) Math.toRadians(-camera.xRot());

        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);
        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);

        float viewX = (float) (dx * cosYaw - dz * sinYaw);
        float viewY = (float) (dy * cosPitch + (dx * sinYaw + dz * cosYaw) * sinPitch);
        float viewZ = (float) (-dy * sinPitch + (dx * sinYaw + dz * cosYaw) * cosPitch);

        if (viewZ <= 0.1) return null;

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();

        double fov = Math.toRadians(client.options.fov().get());
        double aspect = (double) scaledWidth / scaledHeight;

        float ndcX = (float) (viewX / (-viewZ * Math.tan(fov / 2) * aspect));
        float ndcY = (float) (viewY / (-viewZ * Math.tan(fov / 2)));

        float screenX = (ndcX * 0.5f + 0.5f) * scaledWidth;
        float screenY = (-ndcY * 0.5f + 0.5f) * scaledHeight;

        return new int[]{(int) screenX, (int) screenY};
    }
}
