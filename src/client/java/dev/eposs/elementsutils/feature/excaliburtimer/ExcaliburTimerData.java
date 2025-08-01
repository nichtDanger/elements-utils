package dev.eposs.elementsutils.feature.excaliburtimer;

import dev.eposs.elementsutils.api.excaliburtimer.ExcaliburTimerApi;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class ExcaliburTimerData {
	private String next_user;
	private String time;

	private static final AtomicReference<ExcaliburTimerData> INSTANCE = new AtomicReference<>(new ExcaliburTimerData());
	private static Instant lastUpdate = Instant.MIN;

	public static void startUpdateTimers() {
		new Timer("Excalibur Timer Update").scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateData();
			}
		}, 0, Duration.ofMinutes(30).toMillis());
	}

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

	public static ExcaliburTimerData getInstance() {
		return INSTANCE.get();
	}

	public String getNext_user() {
		return next_user;
	}

	public Instant getTime() {
		if (time == null || time.isEmpty()) return null;
		return Instant.parse(time);
	}

	public Duration getTimeUntilNextExcalibur() {
		Instant last = getTime();
		if (last == null) return Duration.ZERO;
		Instant nextExcaliburTime = last.plusSeconds(604940);
		return Duration.between(Instant.now(), nextExcaliburTime).isNegative()
				? Duration.ZERO
				: Duration.between(Instant.now(), nextExcaliburTime);
	}
}