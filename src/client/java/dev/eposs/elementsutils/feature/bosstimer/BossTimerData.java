package dev.eposs.elementsutils.feature.bosstimer;

import dev.eposs.elementsutils.api.timer.BossTimerApi;
import dev.eposs.elementsutils.util.TimerUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class BossTimerData {
    private String axolotl;
    private String zombie;
    private String spider;
    private String bogged;
    private String piglin;

    private static final AtomicReference<BossTimerData> INSTANCE = new AtomicReference<>(new BossTimerData());

    private static Instant lastUpdate = Instant.MIN;

    public static void startUpdateTimers() {
        new Timer("BossTimerData Update Timer").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, Duration.ofHours(1).toMillis());
    }

    public static void updateData() {
        // Only update data every 10 seconds to prevent spamming the API
        if (lastUpdate.isAfter(Instant.now().minusSeconds(10))) return;

        Thread.ofVirtual().name("BossTimerData Update Thread").start(() -> {
            lastUpdate = Instant.now();

            BossTimerData data = new BossTimerApi().getTimerData();
            if (data != null) {
                INSTANCE.set(data); // atomic write
            }
        });
    }

    public static BossTimerData getInstance() {
        return INSTANCE.get(); // atomic read
    }

    public ZonedDateTime getAxolotl() {
        return TimerUtil.parseTime(axolotl);
    }

    public ZonedDateTime getZombie() {
        return TimerUtil.parseTime(zombie);
    }

    public ZonedDateTime getSpider() {
        return TimerUtil.parseTime(spider);
    }

    public ZonedDateTime getBogged() {
        return TimerUtil.parseTime(bogged);
    }

    public ZonedDateTime getPiglin() {
        return TimerUtil.parseTime(piglin);
    }
}
