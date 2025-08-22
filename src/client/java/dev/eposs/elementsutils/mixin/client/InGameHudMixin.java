package dev.eposs.elementsutils.mixin.client;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.feature.leveltracker.LevelState;
import dev.eposs.elementsutils.feature.leveltracker.PlayerLevelTracker;
import dev.eposs.elementsutils.feature.pet.PetDisplay;
import dev.eposs.elementsutils.feature.xpformat.FarmingXpTracker;
import dev.eposs.elementsutils.feature.xpformat.XpFormat;
import dev.eposs.elementsutils.util.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(value = InGameHud.class)
public abstract class InGameHudMixin {

	@Shadow private Text overlayMessage;
	@Shadow private int overlayRemaining;
	@Shadow public abstract void setTitleTicks(int fadeIn, int stay, int fadeOut);

	@Unique
	private static final Pattern XP_PATTERN = Pattern.compile("^[^:]+: ([\\d,.]+)/[\\d,.]+ XP");

	@Inject(at = @At("HEAD"), method = "renderOverlayMessage")
	private void renderOverlayMessage(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if (overlayMessage != null && overlayRemaining > 0) {
			PetDisplay.updatePetXP(overlayMessage, false);
		}
	}

	@Inject(
			method = "setOverlayMessage",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
		if (message == null) return;
		var config = ModConfig.getConfig();
		String original = message.getString();

		if (config.elementsXPConfig.hideBaseMessage) {
			if (original.trim().equals("Base ✔")) {
				ci.cancel();
				return;
			} else if (original.contains("Base ✔")) {
				original = original.replace("Base ✔", "");
			}
		}

		if (config.elementsXPConfig.hideMaxPetXP) {
			if (original.matches(".*🐰 Pet: [\\d,.]+/-1 XP.*")) PetDisplay.setPetMaxLevel();
			original = original.replaceAll("\\s*🐰 Pet: [\\d,.]+/-1 XP", "");
		}

		if (config.elementsXPConfig.hideLevelForecast) {
			original = original.replaceAll("\\s*🖩 Lvl/h: [\\d,.]+", "");
		}

		original = original.replaceAll("\\s+", " ").trim();

		String formatted = config.elementsXPConfig.enabled
				? XpFormat.formatNumbersWithDots(original)
				: original;

		Style overlayStyle = message.getStyle();
		var overlayColor = config.elementsXPConfig.overlayMessageColor;
		if (overlayColor != null && formatted.contains("XP")) {
			overlayStyle = overlayStyle.withColor(overlayColor.color);
		}

		Text xpPerSecText = null;
		Style xpPerSecStyle = Style.EMPTY;
		var matcher = XP_PATTERN.matcher(original);
		if (matcher.find()) {
			int farmingXp = Integer.parseInt(matcher.group(1).replace(".", "").replace(",", ""));
			FarmingXpTracker.update(farmingXp);

			if (config.elementsXPConfig.showXpPerSecond) {
				float xpPerSec = FarmingXpTracker.getXpPerSecond();
				var color = config.elementsXPConfig.xpPerSecondColor;
				if (color != null) xpPerSecStyle = xpPerSecStyle.withColor(color.color);
				xpPerSecText = Text.literal(String.format("%.2fXP/s", xpPerSec)).setStyle(xpPerSecStyle);
			}
		}

		if (config.elementsXPConfig.showLevelCounter && formatted.contains("XP")) {
			double lvlPerMin = PlayerLevelTracker.getLevelsInHistoryWindow();
			int interval = config.elementsXPConfig.levelCounterMinutes;
			String intervalLabel = (interval == 60) ? "h" : interval + "min";
			String formattedLvlPerMin = XpFormat.formatNumbersWithDots(String.valueOf((int) lvlPerMin));
			String lvlPerMinMsg = String.format("  🕒 Lvl/%s: %s", intervalLabel, formattedLvlPerMin);
			formatted += lvlPerMinMsg;
		}

		if (xpPerSecText != null) {
			int xpIndex = formatted.indexOf("XP");
			if (xpIndex != -1) {
				String beforeXp = formatted.substring(0, xpIndex + 2);
				String afterXp = formatted.substring(xpIndex + 2);
				overlayMessage = Text.literal(beforeXp).setStyle(overlayStyle)
						.append(Text.literal(" (").setStyle(xpPerSecStyle))
						.append(xpPerSecText)
						.append(Text.literal(")").setStyle(xpPerSecStyle))
						.append(Text.literal(afterXp).setStyle(overlayStyle));
				overlayRemaining = 60;
				ci.cancel();
				return;
			}
		}

		overlayMessage = Text.literal(formatted).setStyle(overlayStyle);
		overlayRemaining = 60;
		ci.cancel();
	}

	@ModifyArg(
			method = "renderOverlayMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)I"
			),
			index = 3
	)
	private int modifyOverlayMessageY(int originalY) {
		return originalY + ModConfig.getConfig().elementsXPConfig.overlayMessageYOffset;
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
		var config = ModConfig.getConfig();
		if (!config.playerLevelConfig.enabled) return original;
		try {
			int level = Integer.parseInt(original);
			PlayerLevelTracker.update(level);
			LevelState.lastKnownLevel = level;
			return Util.formatLevel(level);
		} catch (Exception e) {
			return original;
		}
	}

	@ModifyArg(
			method = "renderExperienceLevel",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
			),
			index = 4
	)
	private int modifyLevelColor(int originalColor) {
		var config = ModConfig.getConfig();
		var color = config.playerLevelConfig.formattedPlayerLevelColor;
		if (color == null || originalColor == 0) return originalColor;
		return color.color;
	}

	@Inject(
			method = "setTitle",
			at = @At("HEAD")
	)
	private void onSetTitle(Text title, CallbackInfo ci) {
		var config = ModConfig.getConfig().overlaySettings;
		if (config.overrideAfkTitleTime && title != null && title.getString().trim().equalsIgnoreCase("afk")) {
			int stay = (config.afkTitleTimeType == ModConfig.OverlaySettingsConfig.AfkTitleTimeType.INFINITY)
					? Integer.MAX_VALUE
					: Math.max(1, config.afkTitleTimeSeconds) * 20;
			setTitleTicks(0, stay, 0);
		} else {
			setTitleTicks(10, 70, 20);
		}
	}
}