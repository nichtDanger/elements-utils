package dev.eposs.elementsutils.util;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class TimerUtil {

    public static @Nullable ZonedDateTime parseTime(String time) {
        if (time == null || time.isEmpty()) return null;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(time).withZoneSameLocal(TimeZone.getDefault().toZoneId());
        return zonedDateTime.isBefore(ZonedDateTime.ofInstant(Instant.EPOCH, zonedDateTime.getZone())) ? null : zonedDateTime;
    }
    
}
