package dev.eposs.elementsutils.feature.xpmeter;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.mixin.client.InGameHudAccessor;
import dev.eposs.elementsutils.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XpMeter {
	public enum MeasurementMode {
		XP_TARGET,
		TIME_BASED
	}

	private static MeasurementMode mode = MeasurementMode.XP_TARGET;

	private static boolean measuringInProgress = false;
	private static int startXp = 0;
	private static int currentProgress = 0;
	private static long startTime = 0;
	private static int noXpTicks = 0;

	private static double displayedElapsedSeconds = 0.0;
	private static double displayedXpPerSecond = 0.0;
	private static long lastDisplayUpdate = 0;

	private static final Pattern MINING_XP_PATTERN = Pattern.compile("Mining: (\\d[\\d.,]*)");
	private static final String trackedItemName = "Komprimiertes Basalt";
	private static int lastItemCount = 0;
	private static int itemsGainedTotal = 0;

	private static int getTargetXp() {
		return ModConfig.getConfig().xpMeterConfig.measuringXpTarget;
	}

	private static int getTargetTime() {
		return ModConfig.getConfig().xpMeterConfig.measuringTimeTarget * 1000;
	}

	public static boolean isMeasuringInProgress() {
		return measuringInProgress;
	}

	public static float getXpProgress() {
		if (mode == MeasurementMode.XP_TARGET) {
			return Math.min(currentProgress / (float) getTargetXp(), 1.0f);
		} else {
			return Math.min((System.currentTimeMillis() - startTime) / (float) getTargetTime(), 1.0f);
		}
	}

	public static int getCurrentProgress() {
		return currentProgress;
	}

	/**
	 * Counts the number of tracked items in the player's inventory.
	 *
	 * @param client The Minecraft client instance.
	 * @return The total count of the tracked item.
	 */
	private static int countItemInInventory(MinecraftClient client) {
		int count = 0;
		if (client.player != null) {
			for (int i = 0; i < client.player.getInventory().size(); i++) {
				var stack = client.player.getInventory().getStack(i);
				if (!stack.isEmpty() && stack.getName().getString().equals(XpMeter.trackedItemName)) {
					count += stack.getCount();
				}
			}
		}
		return count;
	}

	/**
	 * Starts an XP-based measurement session.
	 * Cancels an ongoing measurement if already active.
	 *
	 * @param client The Minecraft client instance.
	 */
	public static void startXPMeasurement(MinecraftClient client) {
		if (measuringInProgress) {
			measuringInProgress = false;
			Util.sendChatMessage(Text.translatable("elements-utils.message.xpMeter.cancelled"));
			return;
		}
		if (startMeasurement(client)) return;
		mode = MeasurementMode.XP_TARGET;
	}

	/**
	 * Starts a time-based measurement session.
	 * Cancels an ongoing measurement if already active.
	 *
	 * @param client The Minecraft client instance.
	 */
	public static void startTimeMeasurement(MinecraftClient client) {
		if (measuringInProgress) {
			measuringInProgress = false;
			Util.sendChatMessage(Text.translatable("elements-utils.message.xpMeter.cancelled"));
			return;
		}
		if (startMeasurement(client)) return;
		mode = MeasurementMode.TIME_BASED;
	}

	private static boolean startMeasurement(MinecraftClient client) {
		if (client.player == null || client.world == null) return true;

		Text overlayMessage = ((InGameHudAccessor) client.inGameHud).getOverlayMessage();
		String overlayText = (overlayMessage != null) ? overlayMessage.getString() : "";

		Matcher matcher = MINING_XP_PATTERN.matcher(overlayText);

		if (!matcher.find()) {
			Util.sendChatMessage(Text.translatable("elements-utils.message.xpMeter.startFailed"));
			return true;
		}

		String xpString = matcher.group(1).replaceAll("[.,]", "");
		startXp = Integer.parseInt(xpString);
		startTime = System.currentTimeMillis();
		currentProgress = 0;
		measuringInProgress = true;
		noXpTicks = 0;
		displayedElapsedSeconds = 0.0;
		displayedXpPerSecond = 0.0;
		lastDisplayUpdate = System.currentTimeMillis();

		lastItemCount = countItemInInventory(client);
		itemsGainedTotal = 0;
		return false;
	}

	/**
	 * Updates the XP meter state, progress, and item tracking.
	 * Ends the measurement if the target is reached or failed.
	 *
	 * @param client The Minecraft client instance.
	 */
	public static void updateXpMeter(MinecraftClient client) {
		if (!measuringInProgress) return;

		Text overlayMessage = ((InGameHudAccessor) client.inGameHud).getOverlayMessage();
		String overlayText = (overlayMessage != null) ? overlayMessage.getString() : "";

		Matcher matcher = MINING_XP_PATTERN.matcher(overlayText);

		if (!matcher.find()) {
			noXpTicks++;
			if (noXpTicks >= 100) {
				Util.sendChatMessage(Text.translatable("elements-utils.message.xpMeter.failed"));
				measuringInProgress = false;
			}
			return;
		}

		String xpString = matcher.group(1).replaceAll("[.,]", "");
		int currentXp = Integer.parseInt(xpString);
		currentProgress = currentXp - startXp;
		noXpTicks = 0;

		int currentCount = countItemInInventory(client);
		if (currentCount > lastItemCount) {
			itemsGainedTotal += (currentCount - lastItemCount);
		}
		lastItemCount = currentCount;

		if (mode == MeasurementMode.XP_TARGET) {
			if (currentProgress >= getTargetXp()) {
				double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
				double averageXpPerSecond = elapsed > 0 ? currentProgress / elapsed : 0.0;
				Util.sendChatMessage(Text.literal(
						"§3" + getTargetXp() +
								Text.translatable("elements-utils.message.xpMeter.xpFinished").getString()
										.replace("%s", String.format("%.2f", elapsed))
										.replace("%a", String.format("%.2f", averageXpPerSecond))
										.replace("%i", String.valueOf(itemsGainedTotal))
										.replace("%c", String.format("%.2f", itemsGainedTotal / 99.0))
				));
				measuringInProgress = false;
			}
		} else if (mode == MeasurementMode.TIME_BASED) {
			long elapsed = System.currentTimeMillis() - startTime;
			if (elapsed >= getTargetTime()) {
				double seconds = elapsed / 1000.0;
				double averageXpPerSecond = seconds > 0 ? currentProgress / seconds : 0.0;
				Util.sendChatMessage(Text.literal(
						"§e" + String.format("%.2f", seconds) +
								Text.translatable("elements-utils.message.xpMeter.timeFinished").getString()
										.replace("%p", String.valueOf(currentProgress))
										.replace("%a", String.format("%.2f", averageXpPerSecond))
										.replace("%i", String.valueOf(itemsGainedTotal))
										.replace("%c", String.format("%.2f", itemsGainedTotal / 99.0))
				));
				measuringInProgress = false;
			}
		}
	}

	/**
	 * Renders the XP meter overlay, including progress bar and statistics.
	 *
	 * @param context The drawing context.
	 * @param client The Minecraft client instance.
	 */
	public static void render(DrawContext context, MinecraftClient client) {
		if (!isMeasuringInProgress()) return;

		long now = System.currentTimeMillis();
		double elapsedSeconds = (now - startTime) / 1000.0;
		double xpPerSecond = elapsedSeconds > 0 ? currentProgress / elapsedSeconds : 0.0;
		displayedElapsedSeconds = elapsedSeconds;

		if (now - lastDisplayUpdate > 200) {
			displayedXpPerSecond = xpPerSecond;
			lastDisplayUpdate = now;
		}

		int width = client.getWindow().getScaledWidth();
		int y = 20;
		String timeText = String.format("%.2f", displayedElapsedSeconds);
		String xpPerSecondText = String.format("%.2f", displayedXpPerSecond);
		String itemsText = " +" + itemsGainedTotal + " " + Text.translatable("elements-utils.message.xpMeter.compressed").getString();

		int barWidth = 200;
		int barHeight = 10;
		int barX = width / 2 - barWidth / 2;
		int filled = (int)(barWidth * getXpProgress());

		if (mode == MeasurementMode.XP_TARGET) {
			String textPrefix = Text.translatable("elements-utils.message.xpMeter.xpMode").getString();
			String progressText = getCurrentProgress() + "XP/" + getTargetXp() + "XP";
			String timePrefix = " (";
			String timeSuffix = "§es §r| ";
			String textSuffix = "§bXP/s§r)";

			int prefixWidth = client.textRenderer.getWidth(textPrefix);
			int progressWidth = client.textRenderer.getWidth(progressText);
			int timePrefixWidth = client.textRenderer.getWidth(timePrefix);
			int timeTextWidth = client.textRenderer.getWidth(timeText);
			int timeSuffixWidth = client.textRenderer.getWidth(timeSuffix);
			int xpPerSecondWidth = client.textRenderer.getWidth(xpPerSecondText);
			int textSuffixWidth = client.textRenderer.getWidth(textSuffix);
			int itemsWidth = client.textRenderer.getWidth(itemsText);

			int totalWidth = prefixWidth + progressWidth + timePrefixWidth + timeTextWidth + timeSuffixWidth + xpPerSecondWidth + textSuffixWidth + itemsWidth;
			int x = width / 2 - totalWidth / 2;

			context.drawText(client.textRenderer, textPrefix, x, y, 0xFFFFFF00, true);
			x += prefixWidth;
			context.drawText(client.textRenderer, progressText, x, y, 0xFF99FF99, true);
			x += progressWidth;
			context.drawText(client.textRenderer, timePrefix, x, y, 0xFFFFFF, true);
			x += timePrefixWidth;
			context.drawText(client.textRenderer, timeText, x, y, 0xFFFFFF00, true);
			x += timeTextWidth;
			context.drawText(client.textRenderer, timeSuffix, x, y, 0xFFFFFF, true);
			x += timeSuffixWidth;
			drawContext(context, client, y, xpPerSecondText, itemsText, textSuffix, xpPerSecondWidth, textSuffixWidth, x);

			context.fill(barX, y + 15, barX + filled, y + 15 + barHeight, 0xFF00FF00);
			context.fill(barX + filled, y + 15, barX + barWidth, y + 15 + barHeight, 0xFF555555);
		} else if (mode == MeasurementMode.TIME_BASED) {
			String textPrefix = Text.translatable("elements-utils.message.xpMeter.timeMode").getString();
			String xpText = currentProgress + "XP ";
			String timeRatio = String.format("§r(§e%.2fs§r/§e%.2fs §r| ", displayedElapsedSeconds, getTargetTime() / 1000.0);
			String textSuffix = "§bXP/s§r)";
			int prefixWidth = client.textRenderer.getWidth(textPrefix);
			int xpTextWidth = client.textRenderer.getWidth(xpText);
			int timeRatioWidth = client.textRenderer.getWidth(timeRatio);
			int xpPerSecondWidth = client.textRenderer.getWidth(xpPerSecondText);
			int textSuffixWidth = client.textRenderer.getWidth(textSuffix);
			int itemsWidth = client.textRenderer.getWidth(itemsText);

			int totalWidth = prefixWidth + xpTextWidth + timeRatioWidth + xpPerSecondWidth + textSuffixWidth + itemsWidth;
			int x = width / 2 - totalWidth / 2;

			context.drawText(client.textRenderer, textPrefix, x, y, 0xFF00FFFF, true);
			x += prefixWidth;
			context.drawText(client.textRenderer, xpText, x, y, 0xFF99FF99, true);
			x += xpTextWidth;
			context.drawText(client.textRenderer, timeRatio, x, y, 0xFFD3D3D3, true);
			x += timeRatioWidth;
			drawContext(context, client, y, xpPerSecondText, itemsText, textSuffix, xpPerSecondWidth, textSuffixWidth, x);

			context.fill(barX, y + 15, barX + filled, y + 15 + barHeight, 0xFF00FFFF);
			context.fill(barX + filled, y + 15, barX + barWidth, y + 15 + barHeight, 0xFF555555);
		}
	}

	private static void drawContext(DrawContext context, MinecraftClient client, int y, String xpPerSecondText, String itemsText, String textSuffix, int xpPerSecondWidth, int textSuffixWidth, int x) {
		context.drawText(client.textRenderer, xpPerSecondText, x, y, 0xFF00FFFF, true);
		x += xpPerSecondWidth;
		context.drawText(client.textRenderer, textSuffix, x, y, 0xFFFFFF, true);
		x += textSuffixWidth;
		context.drawText(client.textRenderer, itemsText, x, y, 0xFFD3D3D3, true);
	}
}