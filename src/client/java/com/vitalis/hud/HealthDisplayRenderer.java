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

    private static final double MAX_RENDER_DISTANCE = 24.0;

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || client.getCameraEntity() == null) return;
        if (client.gui != null && client.gui.hud != null && client.gui.hud.isHidden()) return;

        VitalisConfig config = VitalisConfig.get();
        if (config.healthShowOnSneak && !client.player.isShiftKeyDown()) return;

        Entity cameraEntity = client.getCameraEntity();
        double camX = cameraEntity.getX();
        double camY = cameraEntity.getY();
        double camZ = cameraEntity.getZ();

        double d = MAX_RENDER_DISTANCE;
        List<LivingEntity> entities = client.level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(camX - d, camY - d, camZ - d, camX + d, camY + d, camZ + d),
                e -> true);

        for (LivingEntity entity : entities) {
            if (entity == cameraEntity) continue;
            if (entity.isRemoved()) continue;
            if (entity.distanceTo(client.getCameraEntity()) > MAX_RENDER_DISTANCE) continue;

            boolean show = false;
            if (entity instanceof Player && config.healthShowPlayers) show = true;
            else if (entity instanceof Monster && config.healthShowHostile) show = true;
            else if (entity instanceof Animal && config.healthShowAnimals) show = true;
            else if (config.healthShowMobs) show = true;

            if (!show) continue;

            renderHealthAbove(graphics, entity, client);
        }
    }

    private static void renderHealthAbove(GuiGraphicsExtractor graphics, LivingEntity entity, Minecraft client) {
        Font textRenderer = client.font;

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        int healthInt = Mth.ceil(health);
        int maxHealthInt = Mth.ceil(maxHealth);

        String healthText = healthInt + "/" + maxHealthInt;

        int color;
        float ratio = health / maxHealth;
        if (ratio > 0.6f) color = 0x55FF55;
        else if (ratio > 0.3f) color = 0xFFFF55;
        else color = 0xFF5555;

        double x = entity.getX();
        double y = entity.getY() + entity.getEyeHeight() + 0.35;
        double z = entity.getZ();

        int[] screenPos = worldToScreen(client, x, y, z);
        if (screenPos == null) return;

        int screenX = screenPos[0];
        int screenY = screenPos[1];

        int textWidth = textRenderer.width(healthText);
        int pad = 2;

        graphics.fill(screenX - textWidth / 2 - pad, screenY - pad,
                screenX + textWidth / 2 + pad, screenY + textRenderer.lineHeight + pad,
                0x80000000);
        graphics.text(textRenderer, healthText, screenX - textWidth / 2, screenY, color, true);
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
