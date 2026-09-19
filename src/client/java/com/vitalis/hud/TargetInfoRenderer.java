package com.vitalis.hud;

import com.vitalis.config.VitalisConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TargetInfoRenderer {

    private static Entity lastTarget = null;
    private static int lastTargetHealth = 0;

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (client.gui != null && client.gui.hud != null && client.gui.hud.isHidden()) return;

        VitalisConfig config = VitalisConfig.get();
        VitalisConfig.TargetConfig tc = config.target;
        if (!tc.enabled) return;

        HitResult hit = client.player.pick(tc.maxDistance, 0, false);
        if (hit.getType() != HitResult.Type.ENTITY) {
            lastTarget = null;
            return;
        }

        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity target = entityHit.getEntity();
        if (!(target instanceof LivingEntity living)) {
            lastTarget = null;
            return;
        }

        lastTarget = target;

        Font font = client.font;
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int centerX = scaledWidth / 2;

        int panelWidth = 160;
        int panelHeight = 0;
        int lines = 0;

        if (tc.showName) lines++;
        if (tc.showType) lines++;
        if (tc.showHealth && target instanceof LivingEntity le) lines++;
        if (tc.showDistance) lines++;
        if (tc.showEffects && target instanceof LivingEntity le && !le.getActiveEffects().isEmpty()) lines++;

        if (lines == 0) return;
        panelHeight = lines * (font.lineHeight + 1) + 8;

        int panelX = centerX - panelWidth / 2;
        int panelY = 2;

        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xC0101010);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0181818);

        int topColor = getEntityColor(target);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 2, topColor);

        int textX = panelX + 4;
        int textY = panelY + 4;

        if (tc.showName) {
            String name = target.getName().getString();
            int nameColor = target instanceof Player ? 0x55FFFF : (target instanceof Monster ? 0xFF5555 : 0x55FF55);
            graphics.text(font, name, textX, textY, nameColor, true);
            textY += font.lineHeight + 1;
        }

        if (tc.showType) {
            String type = getEntityCategory(target);
            graphics.text(font, type, textX, textY, 0x888888, true);
            textY += font.lineHeight + 1;
        }

        if (tc.showHealth && target instanceof LivingEntity le) {
            float hp = le.getHealth();
            float maxHp = le.getMaxHealth();
            int hpInt = Mth.ceil(hp);
            int maxHpInt = Mth.ceil(maxHp);
            float ratio = maxHp > 0 ? hp / maxHp : 0;
            int hpColor = ratio > 0.6f ? 0x55FF55 : (ratio > 0.3f ? 0xFFFF55 : 0xFF5555);

            String hpText = hpInt + " / " + maxHpInt + " HP";
            graphics.text(font, hpText, textX, textY, hpColor, true);

            int barWidth = panelWidth - 8;
            int barY = textY + font.lineHeight + 1;
            graphics.fill(textX, barY, textX + barWidth, barY + 3, 0x40000000);
            graphics.fill(textX, barY, textX + (int)(ratio * barWidth), barY + 3, hpColor);

            textY = barY + 5;
        }

        if (tc.showDistance) {
            double dist = target.distanceTo(client.player);
            String distText = String.format("Distance: %.1fm", dist);
            graphics.text(font, distText, textX, textY, 0x888888, true);
        }
    }

    private static int getEntityColor(Entity entity) {
        if (entity instanceof Player) return 0x55FFFF;
        if (entity instanceof Monster) return 0xFF5555;
        if (entity instanceof Animal) return 0x55FF55;
        return 0xAAAAAA;
    }

    private static String getEntityCategory(Entity entity) {
        if (entity instanceof Player) return "Player";
        if (entity instanceof Monster) return "Hostile";
        if (entity instanceof Animal) return "Animal";
        return entity.getType().toShortString();
    }
}
