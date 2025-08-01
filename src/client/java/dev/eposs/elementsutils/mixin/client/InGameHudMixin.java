package dev.eposs.elementsutils.mixin.client;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.feature.pet.PetDisplay;
import dev.eposs.elementsutils.feature.xpformat.FarmingXpTracker;
import dev.eposs.elementsutils.feature.xpformat.XpFormat;
import dev.eposs.elementsutils.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Intercepts and formats the overlay message before it is displayed.
     * <p>
     * - Formats XP numbers with dots if enabled.<br>
     * - Optionally appends colored XP/s directly after the first "XP".<br>
     * - Hides max pet XP if configured.<br>
     * - Applies a custom overlay color if set in the config.<br>
     * - Cancels the original method to display the modified message.
     * </p>
     *
     * @param message The original overlay message to be displayed.
     * @param tinted  Whether the message should be tinted.
     * @param ci      The callback info for cancelling or continuing the method.
     */
    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message == null) return;

        String original = message.getString();
        String formatted = ModConfig.getConfig().playerXPConfig.enabled
                ? XpFormat.formatNumbersWithDots(original)
                : original;

        Style overlayStyle = message.getStyle();
        var overlayColor = ModConfig.getConfig().playerXPConfig.overlayMessageColor;
        if (overlayColor != null && formatted.contains("XP")) {
            overlayStyle = overlayStyle.withColor(overlayColor.color);
        }

        Text xpPerSecText = null;
        Style xpPerSecStyle = Style.EMPTY;
        Matcher matcher = Pattern.compile("^[^:]+: ([\\d,.]+)/[\\d,.]+ XP").matcher(original);
        if (matcher.find()) {
            int farmingXp = Integer.parseInt(matcher.group(1).replace(".", "").replace(",", ""));
            FarmingXpTracker.update(farmingXp);

            if (ModConfig.getConfig().playerXPConfig.showXpPerSecond) {
                float xpPerSec = FarmingXpTracker.getXpPerSecond();
                var color = ModConfig.getConfig().playerXPConfig.xpPerSecondColor;
                if (color != null) xpPerSecStyle = xpPerSecStyle.withColor(color.color);
                xpPerSecText = Text.literal(String.format("%.2fXP/s", xpPerSec)).setStyle(xpPerSecStyle);
            }
        }

        if (ModConfig.getConfig().playerXPConfig.hideMaxPetXP) {
            if (original.matches(".*Pet: [\\d,.]+/-1 XP$")) PetDisplay.setPetMaxLevel();
            original = original.replaceFirst("\\s*\\p{So}?\\s*Pet: [\\d,.]+/-1 XP$", "");
            formatted = ModConfig.getConfig().playerXPConfig.enabled
                    ? XpFormat.formatNumbersWithDots(original)
                    : original;
        }

        if (xpPerSecText != null) {
            int xpIndex = formatted.indexOf("XP");
            if (xpIndex != -1) {
                String beforeXp = formatted.substring(0, xpIndex + 2);
                String afterXp = formatted.substring(xpIndex + 2);
                this.overlayMessage = Text.literal(beforeXp).setStyle(overlayStyle)
                        .append(Text.literal(" (").setStyle(xpPerSecStyle))
                        .append(xpPerSecText)
                        .append(Text.literal(")").setStyle(xpPerSecStyle))
                        .append(Text.literal(afterXp).setStyle(overlayStyle));
                this.overlayRemaining = 60;
                ci.cancel();
                return;
            }
        }

        this.overlayMessage = Text.literal(formatted).setStyle(overlayStyle);
        this.overlayRemaining = 60;
        ci.cancel();
    }

    @ModifyArg(
            method = "renderOverlayMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III I)I"
            ),
            index = 3
    )
    private int modifyOverlayMessageY(int originalY) {
        int yOffset = ModConfig.getConfig().playerXPConfig.overlayMessageYOffset;
        return originalY + yOffset;
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
