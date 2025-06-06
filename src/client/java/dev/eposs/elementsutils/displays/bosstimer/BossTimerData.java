package dev.eposs.elementsutils.displays.bosstimer;

import dev.eposs.elementsutils.api.bosstimer.BossTimerApi;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class BossTimerData {
    private Instant axolotl;
    private Instant zombie;
    private Instant spider;
    private Instant bogged;
    private Instant piglin;

    private static final AtomicReference<BossTimerData> INSTANCE = new AtomicReference<>(new BossTimerData());
    
    private static Instant lastUpdate = Instant.MIN;

    public static void updateData() {
        // Only update data every 10 seconds to prevent spamming the API
        if (lastUpdate.isAfter(Instant.now().minusSeconds(10))) return;
        
        Thread.ofVirtual().name("ElementsUtils Boss Timer Data Update Thread").start(() -> {
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
        if (axolotl == null) return null;
        return ZonedDateTime.ofInstant(axolotl, ZoneId.systemDefault());
    }
    
    public ZonedDateTime getZombie() {
        if (zombie == null) return null;
        return ZonedDateTime.ofInstant(zombie, ZoneId.systemDefault());
    }
    
    public ZonedDateTime getSpider() {
        if (spider == null) return null;
        return ZonedDateTime.ofInstant(spider, ZoneId.systemDefault());
    }
    
    public ZonedDateTime getBogged() {
        if (bogged == null) return null;
        return ZonedDateTime.ofInstant(bogged, ZoneId.systemDefault());
    }
    
    public ZonedDateTime getPiglin() {
        if (piglin == null) return null;
        return ZonedDateTime.ofInstant(piglin, ZoneId.systemDefault());
    }
}
