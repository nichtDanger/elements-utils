package dev.eposs.elementsutils.feature.excaliburtimer;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Duration;

public class ExcaliburTimerDisplay {
	public static void render(DrawContext context, MinecraftClient client, int baseLine) {
		ModConfig.TimeDisplaysConfig timeDisplaysConfig = ModConfig.getConfig().timeDisplays;
		if (!timeDisplaysConfig.show) return;

		ExcaliburTimerData data = ExcaliburTimerData.getInstance();

		drawText(client, context, baseLine, Text.translatable("elements-utils.display.excaliburtimer.title")
				.formatted(Formatting.UNDERLINE));
		drawText(client, context, baseLine + 1, Text.literal("")
				.append(Text.translatable("elements-utils.display.excaliburtimer.next_player")
						.formatted(Formatting.RED))
				.append(Text.literal(data.getNext_user() == null ? "?" : data.getNext_user())
						.formatted(timeDisplaysConfig.colorExcaliburNames ? Formatting.GOLD : Formatting.WHITE))
		);
		drawText(client, context, baseLine + 2, Text.literal("")
				.append(Text.translatable("elements-utils.display.excaliburtimer.time_left")
						.formatted(Formatting.AQUA))
				.append(
						(timeDisplaysConfig.excaliburTimeFormat == ModConfig.TimeDisplaysConfig.TimeFormat.RELATIVE
								? toRelativeTime(data.getTimeUntilNextExcalibur())
								: Text.literal(formatAbsoluteTime(data.getTimeUntilNextExcalibur())
						).formatted(timeDisplaysConfig.colorExcaliburTime ? Formatting.GREEN : Formatting.WHITE))
				)
		);
	}

	private static Text toRelativeTime(Duration duration) {
		ModConfig.TimeDisplaysConfig config = ModConfig.getConfig().timeDisplays;
		boolean isPast = duration.isNegative();
		Duration absDuration = duration.abs();

		long days = absDuration.toDays();
		absDuration = absDuration.minusDays(days);
		long hours = absDuration.toHours();
		absDuration = absDuration.minusHours(hours);
		long minutes = absDuration.toMinutes();

		StringBuilder sb = new StringBuilder();
		if (days > 0) sb.append(days).append("d ");
		if (hours > 0) sb.append(hours).append("h ");
		if (minutes > 0) sb.append(minutes).append("m ");
		String timeString = sb.toString().trim();
		if (timeString.isEmpty()) timeString = "0m";

		String key = isPast
				? "elements-utils.display.excaliburtimer.relative_after"
				: "elements-utils.display.excaliburtimer.relative";
		Formatting color = config.colorExcaliburTime ? Formatting.GREEN : Formatting.WHITE;

		return Text.translatable(key, timeString).formatted(color);
	}

	private static String formatAbsoluteTime(Duration duration) {
		if (duration.isNegative() || duration.isZero()) {
			return "?";
		}
		java.time.Instant target = java.time.Instant.now().plus(duration);
		java.time.ZonedDateTime dateTime = java.time.ZonedDateTime.ofInstant(target, java.time.ZoneId.systemDefault());
		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		return dateTime.format(formatter);
	}

	private static void drawText(MinecraftClient client, DrawContext context, int line, Text text) {
		int lineHeight = client.textRenderer.fontHeight + 3;
		boolean outline = ModConfig.getConfig().timeDisplays.textOutline;
		context.drawText(
				client.textRenderer,
				text,
				4, (client.getWindow().getScaledHeight() / 2) - (lineHeight * 3) + (lineHeight * line),
				net.minecraft.util.Colors.WHITE, outline
		);
	}
}