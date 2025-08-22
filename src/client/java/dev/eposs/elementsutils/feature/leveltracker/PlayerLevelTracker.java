package dev.eposs.elementsutils.feature.leveltracker;

import dev.eposs.elementsutils.config.ModConfig;
import java.util.LinkedList;

public class PlayerLevelTracker {
	private static final LinkedList<Entry> history = new LinkedList<>();
	private static final LinkedList<LuckyDropEvent> luckyDropEvents = new LinkedList<>();
	private static int lastLevel = -1;
	private static int lastHistoryMinutes = -1;

	public static void update(int currentLevel) {
		checkAndResetIfHistoryMinutesChanged();
		long now = System.currentTimeMillis();
		boolean exclude = ModConfig.getConfig().elementsXPConfig.excludeLuckyDropsFromLevelCounter;
		boolean isLuckyDrop = luckyDropEvents.stream().anyMatch(e -> e.oldLevel == lastLevel && e.newLevel == currentLevel && Math.abs(e.timestamp - now) < 2000);
		if (lastLevel != -1 && currentLevel != lastLevel) {
			if (exclude && isLuckyDrop) {
				lastLevel = currentLevel;
				return;
			}
			history.add(new Entry(now, currentLevel));
			removeOldHistoryEntries();
		}
		lastLevel = currentLevel;
	}

	public static void onLuckyDrop(int oldLevel, int newLevel) {
		long now = System.currentTimeMillis();
		luckyDropEvents.add(new LuckyDropEvent(now, oldLevel, newLevel));
		removeOldLuckyDropEvents();
	}

	public static int getLevelsInHistoryWindow() {
		if (history.size() < 2) return 0;
		long cutoff = System.currentTimeMillis() - getHistoryMillis();
		int startLevel = -1;
		int endLevel = -1;
		for (Entry entry : history) {
			if (entry.timestamp >= cutoff) {
				if (startLevel == -1) startLevel = entry.level;
				endLevel = entry.level;
			}
		}
		if (startLevel == -1) return 0;
		int diff = endLevel - startLevel;
		boolean exclude = ModConfig.getConfig().elementsXPConfig.excludeLuckyDropsFromLevelCounter;
		if (exclude) {
			int luckyDropSum = 0;
			for (LuckyDropEvent e : luckyDropEvents) {
				if (e.timestamp >= cutoff) {
					luckyDropSum += (e.newLevel - e.oldLevel);
				}
			}
			diff -= luckyDropSum;
		}
		return diff;
	}

	public static void resetHistory() {
		history.clear();
		luckyDropEvents.clear();
		lastLevel = -1;
	}

	private static void checkAndResetIfHistoryMinutesChanged() {
		int current = ModConfig.getConfig().elementsXPConfig.levelCounterMinutes;
		if (lastHistoryMinutes != -1 && current != lastHistoryMinutes) {
			resetHistory();
		}
		lastHistoryMinutes = current;
	}

	private static long getHistoryMillis() {
		return ModConfig.getConfig().elementsXPConfig.levelCounterMinutes * 60000L;
	}

	private static void removeOldHistoryEntries() {
		long cutoff = System.currentTimeMillis() - getHistoryMillis();
		while (!history.isEmpty() && history.getFirst().timestamp < cutoff) {
			history.removeFirst();
		}
	}

	private static void removeOldLuckyDropEvents() {
		long cutoff = System.currentTimeMillis() - getHistoryMillis();
		while (!luckyDropEvents.isEmpty() && luckyDropEvents.getFirst().timestamp < cutoff) {
			luckyDropEvents.removeFirst();
		}
	}

	private static class Entry {
		long timestamp;
		int level;
		Entry(long timestamp, int level) {
			this.timestamp = timestamp;
			this.level = level;
		}
	}

	private static class LuckyDropEvent {
		long timestamp;
		int oldLevel;
		int newLevel;
		LuckyDropEvent(long timestamp, int oldLevel, int newLevel) {
			this.timestamp = timestamp;
			this.oldLevel = oldLevel;
			this.newLevel = newLevel;
		}
	}
}