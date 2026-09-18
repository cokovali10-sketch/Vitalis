package com.vitalis.mixin;

import com.vitalis.enchant.EnchantHelperRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreen.class)
public class EnchantmentScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void vitalisRenderEnchantInfo(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EnchantmentScreen self = (EnchantmentScreen) (Object) this;
        EnchantHelperRenderer.renderEnchantInfo(graphics, self, mouseX, mouseY);
    }
}
