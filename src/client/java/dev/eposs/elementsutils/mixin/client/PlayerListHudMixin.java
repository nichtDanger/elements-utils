package dev.eposs.elementsutils.mixin.client;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.util.Util;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

	/**
	 * Modifies the scoreboard score text in the player list.
	 * Formats the score with dots as thousands separators if enabled in the config,
	 * and applies a custom color if configured.
	 *
	 * @param args The method arguments for drawing the scoreboard text.
	 */
	@ModifyArgs(
			method = "renderScoreboardObjective",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
			)
	)
	private void adjustScoreDrawArgs(Args args) {
		Text originalScoreText = args.get(1);
		int x = args.get(2);

		String original = originalScoreText.getString().replace(".", "").replace(",", "");
		int score;
		try {
			score = Integer.parseInt(original);
		} catch (NumberFormatException e) {
			return;
		}

		String formatted = ModConfig.getConfig().playerLevelConfig.enabled ? Util.formatLevel(score) : original;
		int diff = formatted.length() - original.length();
		int pixelPerChar = 2;

		Style style = originalScoreText.getStyle();
		if (ModConfig.getConfig().playerLevelConfig.formattedPlayerListLevelColor != null) {
			int color = ModConfig.getConfig().playerLevelConfig.formattedPlayerListLevelColor.color;
			style = style.withColor(color);
		}

		args.set(1, Text.literal(formatted).setStyle(style));
		args.set(2, x - (diff * pixelPerChar));
	}
}
