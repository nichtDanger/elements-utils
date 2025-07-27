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
	private static boolean measuringXp = false;
	private static int startXp = 0;
	private static int currentProgress = 0;
	private static long startTime = 0;
	private static int noXpTicks = 0;

	private static double displayedElapsedSeconds = 0.0;
	private static double displayedXpPerSecond = 0.0;
	private static long lastDisplayUpdate = 0;

	private static int getTargetXp() {
		return ModConfig.getConfig().measuringXpTarget;
	}

	public static boolean isMeasuringXp() {
		return measuringXp;
	}

	public static float getXpProgress() {
		return Math.min(currentProgress / (float) getTargetXp(), 1.0f);
	}

	public static int getCurrentProgress() {
		return currentProgress;
	}

	private static final Pattern MINING_XP_PATTERN = Pattern.compile("Mining: (\\d+)");

	private static final String trackedItemName = "Komprimiertes Basalt";
	private static int lastItemCount = 0;
	private static int itemsGainedTotal = 0;

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

	public static void startMeasuring(MinecraftClient client) {
		if (client.player == null || client.world == null) return;

		Text overlayMessage = ((InGameHudAccessor) client.inGameHud).getOverlayMessage();
		String overlayText = (overlayMessage != null) ? overlayMessage.getString() : "";

		Matcher matcher = MINING_XP_PATTERN.matcher(overlayText);

		if (!matcher.find()) {
			Util.sendChatMessage(Text.literal("Messung konnte nicht gestartet werden (keine Mining XP erkannt)."));
			return;
		}

		startXp = Integer.parseInt(matcher.group(1));
		startTime = System.currentTimeMillis();
		currentProgress = 0;
		measuringXp = true;
		noXpTicks = 0;
		displayedElapsedSeconds = 0.0;
		displayedXpPerSecond = 0.0;
		lastDisplayUpdate = System.currentTimeMillis();

		lastItemCount = countItemInInventory(client);
		itemsGainedTotal = 0;
	}

	public static void updateXpMeter(MinecraftClient client) {
		if (!measuringXp) return;

		Text overlayMessage = ((InGameHudAccessor) client.inGameHud).getOverlayMessage();
		String overlayText = (overlayMessage != null) ? overlayMessage.getString() : "";

		Matcher matcher = MINING_XP_PATTERN.matcher(overlayText);

		if (!matcher.find()) {
			noXpTicks++;
			if (noXpTicks >= 100) {
				Util.sendChatMessage(Text.literal("Messung abgebrochen: 5 Sekunden keine XP angezeigt."));
				measuringXp = false;
			}
			return;
		}

		int currentXp = Integer.parseInt(matcher.group(1));
		currentProgress = currentXp - startXp;
		noXpTicks = 0;

		int currentCount = countItemInInventory(client);
		if (currentCount > lastItemCount) {
			itemsGainedTotal += (currentCount - lastItemCount);
		}
		lastItemCount = currentCount;

		if (currentProgress >= getTargetXp()) {
			double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
			double averageXpPerSecond = elapsed > 0 ? currentProgress / elapsed : 0.0;
			Util.sendChatMessage(Text.literal(
					"§3" + getTargetXp() + "XP§r in §e" + String.format("%.2f", elapsed) +
							"s§r erreicht |§b Ø " + String.format("%.2f", averageXpPerSecond) + " XP/s §r| " +
							"compressed: §a+" + itemsGainedTotal +
							"§r (§a+" + String.format("%.2f", itemsGainedTotal / 99.0) + " 2er§r)"
			));
			measuringXp = false;
		}
	}

	public static void render(DrawContext context, MinecraftClient client) {
		if (!isMeasuringXp()) return;

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

		String textPrefix = "XP-Messung: ";
		String progressText = getCurrentProgress() + "/" + getTargetXp();
		String timePrefix = " (";
		String timeSuffix = "s, ";
		String textSuffix = "XP/s)";
		String itemsText = " +" + itemsGainedTotal + " compressed";

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
		context.drawText(client.textRenderer, xpPerSecondText, x, y, 0xFF00FFFF, true);
		x += xpPerSecondWidth;
		context.drawText(client.textRenderer, textSuffix, x, y, 0xFFFFFF, true);
		x += textSuffixWidth;
		context.drawText(client.textRenderer, itemsText, x, y, 0xFFD3D3D3, true);

		int barWidth = 200;
		int barHeight = 10;
		int barX = width / 2 - barWidth / 2;
		int filled = (int)(barWidth * getXpProgress());
		context.fill(barX, y + 15, barX + filled, y + 15 + barHeight, 0xFF00FF00);
		context.fill(barX + filled, y + 15, barX + barWidth, y + 15 + barHeight, 0xFF555555);
	}
}