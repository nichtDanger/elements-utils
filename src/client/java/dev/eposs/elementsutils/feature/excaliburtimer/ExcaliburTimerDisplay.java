package dev.eposs.elementsutils.feature.excaliburtimer;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Handles the display and logic for the Excalibur timer overlay.
 */
public class ExcaliburTimerDisplay {
	private static final int DAYS = 7;
	private static final long EXTRA_SECONDS = DAYS * 20;
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	/**
	 * Toggles the visibility of the Excalibur timer overlay and updates data if enabled.
	 *
	 * @param client The Minecraft client instance. Must not be null.
	 */
	public static void toggleDisplay(@NotNull MinecraftClient client) {
		if (client.player == null || client.world == null) return;

		ModConfig.getConfig().excaliburTime.show = !ModConfig.getConfig().excaliburTime.show;
		ModConfig.save();

		if (ModConfig.getConfig().excaliburTime.show) {
			ExcaliburTimerData.updateData();
		}
	}

	/**
	 * Colors the given text if enabled, otherwise returns the text unchanged.
	 *
	 * @param text   The text to colorize.
	 * @param enabled Whether coloring is enabled.
	 * @param color  The formatting color to apply.
	 * @return The (possibly) colored text.
	 */
	private static MutableText colorize(MutableText text, boolean enabled, Formatting color) {
		return enabled ? text.formatted(color) : text;
	}

	/**
	 * Renders the Excalibur timer overlay on the screen.
	 *
	 * @param context  The draw context.
	 * @param client   The Minecraft client instance.
	 * @param baseLine The base line for vertical positioning.
	 */
	public static void render(DrawContext context, MinecraftClient client, int baseLine) {
		var config = ModConfig.getConfig().excaliburTime;
		if (!config.show) return;

		var data = ExcaliburTimerData.getInstance();
		ZonedDateTime targetTime = calculateTargetTime(data.getTime());
		Duration timeUntilTarget = targetTime == null ? Duration.ZERO : Duration.between(Instant.now(), targetTime);

		drawText(client, context, baseLine, Text.translatable("elements-utils.display.excaliburTime.title").formatted(Formatting.UNDERLINE));
		drawText(client, context, baseLine + 1, Text.literal("")
				.append(colorize(Text.translatable("elements-utils.display.excaliburTime.next_player"), config.colorExcaliburNames, Formatting.RED))
				.append(colorize(Text.literal(data.getNext_user().isEmpty() ? "?" : data.getNext_user()), config.colorExcaliburNames, Formatting.GOLD))
		);
		drawText(client, context, baseLine + 2, Text.literal("")
				.append(colorize(Text.translatable("elements-utils.display.excaliburTime.time_left"), config.colorExcaliburTime, Formatting.AQUA))
				.append(
						(config.excaliburTimeFormat == ModConfig.TimeFormat.RELATIVE
								? toRelativeTime(timeUntilTarget)
								: Text.literal(formatTargetTime(targetTime)
						).formatted(config.colorExcaliburTime ? Formatting.GREEN : Formatting.WHITE)))
		);
	}

	/**
	 * Converts a duration to a relative time text (e.g. "2d 3h 5m").
	 *
	 * @param duration The duration until or since the target time.
	 * @return The formatted relative time as a Text object.
	 */
	private static Text toRelativeTime(Duration duration) {
		var config = ModConfig.getConfig().excaliburTime;
		boolean isPast = duration.isNegative();
		Duration abs = duration.abs();

		long days = abs.toDays();
		abs = abs.minusDays(days);
		long hours = abs.toHours();
		abs = abs.minusHours(hours);
		long minutes = abs.toMinutes();

		StringBuilder sb = new StringBuilder();
		if (days > 0) sb.append(days).append("d ");
		if (hours > 0) sb.append(hours).append("h ");
		if (minutes > 0) sb.append(minutes).append("m ");
		String timeString = sb.toString().trim();
		if (timeString.isEmpty()) timeString = "0m";

		String key = isPast
				? "elements-utils.display.excaliburTime.relative_after"
				: "elements-utils.display.excaliburTime.relative";
		Formatting color = config.colorExcaliburTime ? Formatting.GREEN : Formatting.WHITE;

		return Text.translatable(key, timeString).formatted(color);
	}

	/**
	 * Calculates the target instant based on the given start time.
	 *
	 * @param startTime The start time as an Instant.
	 * @return The calculated target Instant, or null if startTime is null.
	 */
	private static ZonedDateTime calculateTargetTime(ZonedDateTime startTime) {
		if (startTime == null) return null;
		return startTime
				.plusDays(DAYS)
				.plusSeconds(EXTRA_SECONDS);
	}

	/**
	 * Formats the target instant as a date-time string.
	 *
	 * @param targetInstant The target instant to format.
	 * @return The formatted date-time string, or "?" if targetInstant is null.
	 */
	private static String formatTargetTime(ZonedDateTime targetInstant) {
		if (targetInstant == null) return "?";
		return FORMATTER.format(targetInstant);
	}

	/**
	 * Draws a line of text on the screen at the specified line index.
	 *
	 * @param client   The Minecraft client instance.
	 * @param context  The draw context.
	 * @param line     The line index for vertical positioning.
	 * @param text     The text to draw.
	 */
	private static void drawText(MinecraftClient client, DrawContext context, int line, Text text) {
		int lineHeight = client.textRenderer.fontHeight + 3;
		boolean outline = ModConfig.getConfig().excaliburTime.textOutline;
		context.drawText(
				client.textRenderer,
				text,
				4, (client.getWindow().getScaledHeight() / 2) - (lineHeight * 3) + (lineHeight * line),
				net.minecraft.util.Colors.WHITE, outline
		);
	}
}