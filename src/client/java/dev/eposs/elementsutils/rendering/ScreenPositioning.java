package dev.eposs.elementsutils.rendering;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.util.Window;

public class ScreenPositioning {
    public static final int IMAGE_SIZE = 16;
    public static final int GAP = 4;

    public static Position getMoonPhasePosition(Window window) {
        ModConfig.Position displayPosition = ModConfig.getConfig().displayPosition;

        switch (displayPosition) {
            case TOP_LEFT -> {
                return new Position(0, 0);
            }
            case TOP_RIGHT -> {
                return new Position(window.getScaledWidth() - IMAGE_SIZE, 0);
            }
            case BOTTOM_LEFT -> {
                return new Position(0, window.getScaledHeight() - IMAGE_SIZE);
            }
            case BOTTOM_RIGHT -> {
                return new Position(window.getScaledWidth() - IMAGE_SIZE, window.getScaledHeight() - IMAGE_SIZE);
            }
        }

        return new Position(0, 0);
    }

    public static Position getTimePosition(Window window) {
        ModConfig.Position displayPosition = ModConfig.getConfig().displayPosition;

        switch (displayPosition) {
            case TOP_LEFT -> {
                return new Position(IMAGE_SIZE + GAP, 0);
            }
            case TOP_RIGHT -> {
                return new Position(window.getScaledWidth() - (IMAGE_SIZE * 2) - GAP, 0);
            }
            case BOTTOM_LEFT -> {
                return new Position(IMAGE_SIZE + GAP, window.getScaledHeight() - IMAGE_SIZE);
            }
            case BOTTOM_RIGHT -> {
                return new Position(window.getScaledWidth() - (IMAGE_SIZE * 2) - GAP, window.getScaledHeight() - IMAGE_SIZE);
            }
        }
        
        return new Position(0, 0);
    }
}
