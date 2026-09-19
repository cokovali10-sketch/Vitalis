package com.vitalis;

import com.vitalis.config.VitalisConfig;
import com.vitalis.enchant.EnchantHelperRenderer;
import com.vitalis.hud.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class VitalisClient implements ClientModInitializer {
    public static final String MOD_ID = "vitalis";

    @Override
    public void onInitializeClient() {
        VitalisConfig.get();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "health_display"),
                (graphics, tickCounter) -> {
                    if (VitalisConfig.get().health.enabled) {
                        HealthDisplayRenderer.render(graphics);
                    }
                }
        );

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "armor_durability"),
                (graphics, tickCounter) -> {
                    if (VitalisConfig.get().armor.enabled) {
                        ArmorDurabilityRenderer.render(graphics);
                    }
                }
        );

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "compact_hud"),
                (graphics, tickCounter) -> {
                    if (VitalisConfig.get().overlay.enabled) {
                        CompactHUDRenderer.render(graphics);
                    }
                }
        );

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "target_info"),
                (graphics, tickCounter) -> {
                    if (VitalisConfig.get().target.enabled) {
                        TargetInfoRenderer.render(graphics);
                    }
                }
        );

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "potion_timer"),
                (graphics, tickCounter) -> {
                    if (VitalisConfig.get().potion.enabled) {
                        PotionTimerRenderer.render(graphics);
                    }
                }
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            VitalisConfig config = VitalisConfig.get();
            if (config.enchant.enabled && client.gui != null && client.gui.screen() != null) {
                EnchantHelperRenderer.onScreenTick(client.gui.screen());
            }
        });

        System.out.println("[Vitalis] Mod initialized! v1.3.0 - health above entities");
    }
}
