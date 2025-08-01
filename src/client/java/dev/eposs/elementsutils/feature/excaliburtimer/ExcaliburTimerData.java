package dev.eposs.elementsutils.feature.excaliburtimer;

import dev.eposs.elementsutils.api.excaliburtimer.ExcaliburTimerApi;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds and updates the Excalibur timer data, including the next user and the time.
 * Provides thread-safe access and periodic updates.
 */
public class ExcaliburTimerData {
	/** The name of the next user. */
	private String next_user;
	/** The time as an ISO-8601 string. */
	private String time;

	/** Singleton instance holder. */
	private static final AtomicReference<ExcaliburTimerData> INSTANCE = new AtomicReference<>(new ExcaliburTimerData());
	/** Timestamp of the last update. */
	private static Instant lastUpdate = Instant.MIN;

	/**
	 * Starts a timer that periodically updates the Excalibur timer data every hour.
	 */
	public static void startUpdateTimers() {
		new Timer("Excalibur Timer Update").scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateData();
			}
		}, 0, Duration.ofHours(1).toMillis());
	}

	/**
	 * Updates the Excalibur timer data from the API if at least 10 seconds have passed since the last update.
	 * Runs the update in a virtual thread.
	 */
	public static void updateData() {
		if (lastUpdate.isAfter(Instant.now().minusSeconds(10))) return;

		Thread.ofVirtual().name("Excalibur Timer Data Update Thread").start(() -> {
			lastUpdate = Instant.now();
			ExcaliburTimerData data = ExcaliburTimerApi.getExcaliburTimerData();
			if (data != null) {
				INSTANCE.set(data);
			}
		});
	}

	/**
	 * Returns the current singleton instance of the timer data.
	 *
	 * @return The current ExcaliburTimerData instance.
	 */
	public static ExcaliburTimerData getInstance() {
		return INSTANCE.get();
	}

	/**
	 * Returns the name of the next user.
	 *
	 * @return The next user as a String.
	 */
	public String getNext_user() {
		return next_user;
	}

	/**
	 * Returns the time as an Instant, or null if not set or empty.
	 *
	 * @return The time as an Instant, or null.
	 */
	public Instant getTime() {
		if (time == null || time.isEmpty()) return null;
		return Instant.parse(time);
	}
}