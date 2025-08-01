package dev.eposs.elementsutils.mixin.client;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.feature.pet.PetDisplay;
import dev.eposs.elementsutils.feature.xpformat.XpFormat;
import dev.eposs.elementsutils.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

	/**
     * Updates the pet XP display when an overlay message is shown.
     *
     * @param context The drawing context.
     * @param tickCounter The render tick counter.
     * @param ci The callback info.
     */
    @Inject(at = @At("HEAD"), method = "renderOverlayMessage")
    private void renderOverlayMessage(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.overlayMessage != null && this.overlayRemaining > 0) {
            PetDisplay.updatePetXP(this.overlayMessage, false);
        }
    }

    /**
     * Handles the overlay message before it is set.
     * Applies formatting, hides max pet XP, and sets the color if configured.
     *
     * @param message The original overlay message.
     * @param tinted Whether the message is tinted.
     * @param ci The callback info.
     */
    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message != null) {
            String original = message.getString();

            if (ModConfig.getConfig().playerXPConfig.hideMaxPetXP) {
                boolean isMaxLevel = original.matches(".*Pet: [\\d,.]+/-1 XP$");
                if (isMaxLevel) PetDisplay.setPetMaxLevel();
                original = original.replaceFirst("XP.*(\\p{So}?\\s*Pet: [\\d,.]+/-1 XP)$", "XP");
            }

            String formatted = ModConfig.getConfig().playerXPConfig.enabled
                    ? XpFormat.formatNumbersWithDots(original)
                    : original;

            var style = message.getStyle();
            if (ModConfig.getConfig().playerXPConfig.overlayMessageColor != null
                    && formatted.contains("XP")) {
                style = style.withColor(ModConfig.getConfig().playerXPConfig.overlayMessageColor.color);
            }

            this.overlayMessage = Text.literal(formatted).setStyle(style);
            this.overlayRemaining = 60;
            ci.cancel();
        }
    }

    /**
     * Modifies the displayed experience level text.
     * Formats the level with dots if enabled in the config.
     *
     * @param original The original level string.
     * @return The formatted level string.
     */
    @ModifyArg(
            method = "renderExperienceLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
            ),
            index = 1
    )
    private String modifyLevelText(String original) {
        if (!ModConfig.getConfig().playerLevelConfig.enabled) return original;
        try {
            int level = Integer.parseInt(original);
            return Util.formatLevel(level);
        } catch (Exception e) {
            return original;
        }
    }

    /**
     * Modifies the color of the experience level text.
     * Uses the configured color if available.
     *
     * @param originalColor The original color value.
     * @return The new color value.
     */
    @ModifyArg(
            method = "renderExperienceLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
            ),
            index = 4
    )
    private int modifyLevelColor(int originalColor) {
        if (ModConfig.getConfig().playerLevelConfig.formattedPlayerLevelColor == null) return originalColor;
        if (originalColor == 0) return originalColor;
        return ModConfig.getConfig().playerLevelConfig.formattedPlayerLevelColor.color;
    }


}
