package dev.eposs.elementsutils.mixin.client;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.feature.leveltracker.LevelState;
import dev.eposs.elementsutils.feature.leveltracker.PlayerLevelTracker;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

	@Shadow
	public abstract void addMessage(Text message);

	@Unique
	private static final NumberFormat GERMAN_NUMBER_FORMAT = NumberFormat.getInstance(java.util.Locale.GERMAN);

	@Unique
	private static final Pattern LUCKY_DROP_PATTERN = Pattern.compile("Server: Du hast einen Lucky Drop in Höhe von (\\d+) Leveln bekommen\\.");

	@Unique
	private static final List<Map.Entry<Long, Integer>> luckyDropHistory = new ArrayList<>();

	@Unique
	private static final long MILLIS_PER_MINUTE = 60_000L;

	@Unique
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d{4,}\\b");

	@Unique
	private MutableText formatNumbersInText(Text text) {
		if (text.getSiblings().isEmpty()) {
			String content = text.getString();
			Matcher matcher = NUMBER_PATTERN.matcher(content);
			MutableText result = Text.empty();
			int lastEnd = 0;
			while (matcher.find()) {
				String number = matcher.group();
				result.append(Text.literal(content.substring(lastEnd, matcher.start())).setStyle(text.getStyle()));
				result.append(Text.literal(GERMAN_NUMBER_FORMAT.format(Long.parseLong(number))).setStyle(text.getStyle()));
				lastEnd = matcher.end();
			}
			if (lastEnd < content.length()) {
				result.append(Text.literal(content.substring(lastEnd)).setStyle(text.getStyle()));
			}
			return result;
		} else {
			MutableText result = Text.empty();
			MutableText contentFirst = text.copy();
			contentFirst.getSiblings().clear();
			result.append(formatNumbersInText(contentFirst));
			for (Text sibling : text.getSiblings()) {
				result.append(formatNumbersInText(sibling));
			}
			return result;
		}
	}

	@Unique
	private MutableText replaceLevelInText(Text text, String original, String replacement) {
		MutableText result = Text.empty();

		if (text.getSiblings().isEmpty()) {
			String content = text.getString() != null ? text.getString() : "";
			int idx = content.indexOf(original);

			if (idx != -1) {
				if (idx > 0) {
					result.append(Text.literal(content.substring(0, idx)).setStyle(text.getStyle()));
				}
				result.append(Text.literal(replacement).setStyle(text.getStyle()));
				int endIdx = idx + original.length();
				if (endIdx < content.length()) {
					result.append(Text.literal(content.substring(endIdx)).setStyle(text.getStyle()));
				}
			} else if (!content.isEmpty()) {
				result.append(Text.literal(content).setStyle(text.getStyle()));
			}
		} else {
			MutableText contentFirst = text.copy();
			contentFirst.getSiblings().clear();
			result.append(Text.literal(contentFirst.getString()).setStyle(text.getStyle()));
		}

		for (Text sibling : text.getSiblings()) {
			result.append(replaceLevelInText(sibling, original, replacement));
		}

		return result;
	}

	@Inject(method = "addMessage*", at = @At("HEAD"), cancellable = true)
	private void onAddMessage(Text message, CallbackInfo ci) {
		String msg = message.getString();

		String summarySuffix = "/" + ModConfig.getConfig().chatEnhancements.luckyDropSummaryMinutes + "Min)";
		if (msg.contains(summarySuffix)) {
			return;
		}

		Matcher matcher = LUCKY_DROP_PATTERN.matcher(msg);
		if (matcher.find()) {
			int level = Integer.parseInt(matcher.group(1));
			PlayerLevelTracker.onLuckyDrop(LevelState.lastKnownLevel, LevelState.lastKnownLevel + level);
			LevelState.lastKnownLevel += level;
			PlayerLevelTracker.update(LevelState.lastKnownLevel);

			if (ModConfig.getConfig().chatEnhancements.showLuckyDropSummary) {
				long now = System.currentTimeMillis();
				luckyDropHistory.add(new AbstractMap.SimpleEntry<>(now, level));
				luckyDropHistory.removeIf(entry -> now - entry.getKey() > ModConfig.getConfig().chatEnhancements.luckyDropSummaryMinutes * MILLIS_PER_MINUTE);
				int sum = luckyDropHistory.stream().mapToInt(Map.Entry::getValue).sum();

				String formattedLevel = GERMAN_NUMBER_FORMAT.format(level);
				String formattedSum = GERMAN_NUMBER_FORMAT.format(sum);

				String original = matcher.group(1);
				MutableText formattedText = replaceLevelInText(message, original, formattedLevel);

				MutableText prefix = Text.literal(" (").styled(style -> style.withColor(Formatting.GRAY));
				MutableText sumText = Text.literal(formattedSum + " Level").styled(style -> style.withColor(Formatting.AQUA));
				MutableText suffix = Text.literal(summarySuffix).styled(style -> style.withColor(Formatting.GRAY));

				MutableText newMsg = formattedText.append(prefix).append(sumText).append(suffix);

				addMessage(newMsg);
				ci.cancel();
				return;
			}
		}

		if (ModConfig.getConfig().playerLevelConfig.enabled) {
			MutableText formattedText = formatNumbersInText(message);
			if (!formattedText.getString().equals(message.getString())) {
				addMessage(formattedText);
				ci.cancel();
			}
		}
	}
}