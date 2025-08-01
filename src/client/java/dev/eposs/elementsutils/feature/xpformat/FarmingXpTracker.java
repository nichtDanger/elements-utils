package dev.eposs.elementsutils.feature.xpformat;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks farming XP changes over time to calculate XP per second.
 */
public class FarmingXpTracker {
	private static final List<Long> timestamps = new ArrayList<>();
	private static final List<Integer> xpValues = new ArrayList<>();
	private static long lastMessageTime = 0;
	private static int lastXp = -1;

	/**
	 * Updates the tracker with the current XP value.
	 * Resets the history if more than 5 seconds have passed since the last update.
	 * Only stores values if the XP has changed.
	 * Removes values older than 20 seconds.
	 *
	 * @param currentXp the current XP value
	 */
	public static void update(int currentXp) {
		long now = System.currentTimeMillis();

		if (now - lastMessageTime > 5000) {
			timestamps.clear();
			xpValues.clear();
			lastXp = currentXp;
		}

		if (currentXp != lastXp) {
			timestamps.add(now);
			xpValues.add(currentXp);
			lastXp = currentXp;
		}
		lastMessageTime = now;

		while (!timestamps.isEmpty() && now - timestamps.getFirst() > 20000) {
			timestamps.removeFirst();
			xpValues.removeFirst();
		}
	}

	/**
	 * Calculates the XP gained per second over the tracked period.
	 *
	 * @return the XP per second as a float
	 */
	public static float getXpPerSecond() {
		if (xpValues.size() < 2) return 0f;
		int deltaXp = xpValues.getLast() - xpValues.getFirst();
		long deltaTime = timestamps.getLast() - timestamps.getFirst();
		if (deltaTime == 0) return 0f;
		return deltaXp / (deltaTime / 1000f);
	}

	/**
	 * Resets the tracker and clears all stored values.
	 */
	public static void reset() {
		timestamps.clear();
		xpValues.clear();
		lastXp = -1;
		lastMessageTime = 0;
	}
}