package com.vitalis;

import com.vitalis.config.VitalisConfig;
import com.vitalis.enchant.EnchantHelperRenderer;
import com.vitalis.hud.HealthDisplayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class VitalisClient implements ClientModInitializer {
    public static final String MOD_ID = "vitalis";

    @Override
    public void onInitializeClient() {
        VitalisConfig.get();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "health_display"),
                (graphics, tickCounter) -> {
                    VitalisConfig config = VitalisConfig.get();
                    if (config.healthDisplayEnabled) {
                        HealthDisplayRenderer.render(graphics);
                    }
                }
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            VitalisConfig config = VitalisConfig.get();
            if (config.enchantHelperEnabled && client.gui != null && client.gui.screen() != null) {
                EnchantHelperRenderer.onScreenTick(client.gui.screen());
            }
        });

        System.out.println("[Vitalis] Mod initialized successfully!");
    }
}
