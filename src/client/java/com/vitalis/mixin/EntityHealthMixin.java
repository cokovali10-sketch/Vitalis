package com.vitalis.mixin;

import com.vitalis.config.VitalisConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.renderer.entity.LivingEntityRenderer.class)
public class EntityHealthMixin {

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void vitalisForceShowHealth(LivingEntity entity, double distanceSquared, CallbackInfoReturnable<Boolean> cir) {
        if (VitalisConfig.get().health.enabled && !(entity instanceof Player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractNameTags(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void vitalisAppendHealth(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (VitalisConfig.get().health.enabled && !(entity instanceof Player)) {
            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            int healthInt = Mth.ceil(health);
            int maxHealthInt = Mth.ceil(maxHealth);
            float ratio = maxHealth > 0 ? health / maxHealth : 0;

            ChatFormatting color;
            if (ratio > 0.6f) color = ChatFormatting.GREEN;
            else if (ratio > 0.3f) color = ChatFormatting.YELLOW;
            else color = ChatFormatting.RED;

            Component healthText = Component.literal(healthInt + "/" + maxHealthInt).withStyle(color);

            if (state.nameTag != null) {
                state.nameTag = state.nameTag.copy().append(Component.literal("\n")).append(healthText);
            } else {
                state.nameTag = healthText;
            }
        }
    }
}
