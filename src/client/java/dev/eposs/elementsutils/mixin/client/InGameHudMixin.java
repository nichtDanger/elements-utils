package dev.eposs.elementsutils.mixin.client;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.feature.pet.PetDisplay;
import dev.eposs.elementsutils.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    private Text overlayMessage;
    @Shadow
    private int overlayRemaining;
    @Final
    @Shadow
    private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "renderOverlayMessage")
    private void renderOverlayMessage(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.overlayMessage != null && this.overlayRemaining > 0) {
            PetDisplay.updatePetXP(this.overlayMessage, false);
        }
    }

    @ModifyArg(
            method = "renderExperienceLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
            ),
            index = 1
    )
    private String modifyLevelText(String original) {
        if (!ModConfig.getConfig().formatPlayerLevel) return original;
        try {
            int level = Integer.parseInt(original);
            return Util.formatLevel(level);
        } catch (NumberFormatException e) {
            return original;
        }
    }
}
