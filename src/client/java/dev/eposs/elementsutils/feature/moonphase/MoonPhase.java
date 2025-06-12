package dev.eposs.elementsutils.feature.moonphase;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MoonPhase {
    FULL_MOON("full-moon.png"),
    WANING_GIBBOUS("waning-gibbous-moon.png"),
    THIRD_QUARTER("last-quarter-moon.png"),
    WANING_CRESCENT("waning-crescent-moon.png"),
    NEW_MOON("new-moon.png"),
    WAXING_CRESCENT("waxing-crescent-moon.png"),
    FIRST_QUARTER("first-quarter-moon.png"),
    WAXING_GIBBOUS("waxing-gibbous-moon.png");

    private final String texture;

    MoonPhase(String texture) {
        this.texture = texture;
    }

    @Nullable
    public static MoonPhase fromId(int id) {
        return MoonPhase.values()[id % MoonPhase.values().length];
    }

    public @NotNull String getTexturePath() {
        return "gui/containers/" + this.texture;
    }
}
