package dev.eposs.elementsutils.feature.bosstimer;

import dev.eposs.elementsutils.api.timer.BossTimerApi;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.TimeZone;
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
        new Timer("Boss Death Update Timer").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, Duration.ofHours(1).toMillis());
    }

    public static void updateData() {
        // Only update data every 10 seconds to prevent spamming the API
        if (lastUpdate.isAfter(Instant.now().minusSeconds(10))) return;

        Thread.ofVirtual().name("Boss Timer Data Update Thread").start(() -> {
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
        if (axolotl == null || axolotl.isEmpty()) return null;
        return parseTime(axolotl);
    }

    public ZonedDateTime getZombie() {
        if (zombie == null || zombie.isEmpty()) return null;
        return parseTime(zombie);
    }

    public ZonedDateTime getSpider() {
        if (spider == null || spider.isEmpty()) return null;
        return parseTime(spider);
    }

    public ZonedDateTime getBogged() {
        if (bogged == null || bogged.isEmpty()) return null;
        return parseTime(bogged);
    }

    public ZonedDateTime getPiglin() {
        if (piglin == null || piglin.isEmpty()) return null;
        return parseTime(piglin);
    }

    private ZonedDateTime parseTime(String time) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(time).withZoneSameLocal(TimeZone.getDefault().toZoneId());
        return zonedDateTime.isBefore(ZonedDateTime.ofInstant(Instant.EPOCH, zonedDateTime.getZone())) ? null : zonedDateTime;
    }
}
