package dev.eposs.elementsutils.displays.bosstimer;

import dev.eposs.elementsutils.api.bosstimer.BossTimerApi;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class BossTimerData {
    private String axolotl;
    private String zombie;
    private String spider;
    private String bogged;
    private String piglin;

    private static final AtomicReference<BossTimerData> INSTANCE = new AtomicReference<>(new BossTimerData());

    private static Instant lastUpdate = Instant.MIN;

    public static void updateData() {
        // Only update data every 10 seconds to prevent spamming the API
        if (lastUpdate.isAfter(Instant.now().minusSeconds(10))) return;

        Thread.ofVirtual().name("Boss Timer Data Update Thread").start(() -> {
            lastUpdate = Instant.now();

            BossTimerData data = BossTimerApi.getBossTimerData();
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
        return checkIfValid(ZonedDateTime.parse(axolotl));
    }

    public ZonedDateTime getZombie() {
        if (zombie == null || zombie.isEmpty()) return null;
        return checkIfValid(ZonedDateTime.parse(zombie));
    }

    public ZonedDateTime getSpider() {
        if (spider == null || spider.isEmpty()) return null;
        return checkIfValid(ZonedDateTime.parse(spider));
    }

    public ZonedDateTime getBogged() {
        if (bogged == null || bogged.isEmpty()) return null;
        return checkIfValid(ZonedDateTime.parse(bogged));
    }

    public ZonedDateTime getPiglin() {
        if (piglin == null || piglin.isEmpty()) return null;
        return checkIfValid(ZonedDateTime.parse(piglin));
    }

    private ZonedDateTime checkIfValid(ZonedDateTime time) {
        return time.isBefore(ZonedDateTime.ofInstant(Instant.EPOCH, time.getZone())) ? null : time;
    }
}
