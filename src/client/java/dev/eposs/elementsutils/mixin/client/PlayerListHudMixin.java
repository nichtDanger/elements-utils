package dev.eposs.elementsutils.mixin.client;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.util.Util;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

	@ModifyArgs(
			method = "renderScoreboardObjective",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
			)
	)
	private void adjustScoreDrawArgs(Args args) {
		if (!ModConfig.getConfig().formatPlayerLevel) return;

		Text originalScoreText = args.get(1);
		int x = args.get(2);

		String original = originalScoreText.getString().replace(".", "").replace(",", "");
		int score;
		try {
			score = Integer.parseInt(original);
		} catch (NumberFormatException e) {
			return;
		}

		String formatted = Util.formatLevel(score);
		int diff = formatted.length() - original.length();
		int pixelPerChar = 2;

		args.set(1, Text.literal(formatted).setStyle(originalScoreText.getStyle()));
		args.set(2, x - (diff * pixelPerChar));
	}
}
