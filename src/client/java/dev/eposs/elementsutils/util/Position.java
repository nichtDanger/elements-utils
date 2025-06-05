package dev.eposs.elementsutils.util;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.util.Window;

import java.util.Arrays;

public record Position(int x, int y) {
    public enum DisplayType {
        MOON_PHASE, TIME
    }

    public static Position getDisplayPosition(DisplayType displayType, Window window, int size) {
        ModConfig config = ModConfig.getConfig();
        // Get all display configurations
        DisplayInfo[] displays = {
                new DisplayInfo(DisplayType.MOON_PHASE, config.moonPhaseDisplay.position, size),
                new DisplayInfo(DisplayType.TIME, config.timeDisplay.position, size),
        };

        DisplayInfo targetDisplay = Arrays.stream(displays).filter(display -> display.type == displayType)
                .findFirst().orElse(null);

        if (targetDisplay == null) {
            return new Position(0, 0); // Fallback
        }

        // Calculate offset based on other displays in the same position
        int offsetX = calculateOffset(targetDisplay, displays);
        int offsetY = 0;

        return fromConfig(targetDisplay.position, window, size, size, offsetX, offsetY);
    }

    private static int calculateOffset(DisplayInfo targetDisplay, DisplayInfo[] displays) {
        int offset = 0;
        final int GAP = 4;

        for (DisplayInfo display : displays) {
            // Skip the target display itself
            if (display.type == targetDisplay.type) {
                continue;
            }

            // Only consider displays in the same position
            if (display.position != targetDisplay.position) {
                continue;
            }
        }

        // Add offset based on position type
        switch (targetDisplay.position) {
            case TOP_LEFT, BOTTOM_LEFT -> {
                // For left positions, stack displays to the right
                return targetDisplay.size + offset;
            }
            case TOP_RIGHT, BOTTOM_RIGHT -> {
                // For right positions, stack displays to the left (negative offset)
                return -targetDisplay.size - offset;
            }
        }

        return offset;
    }

    private record DisplayInfo(DisplayType type, ModConfig.Position position, int size) {
    }

    public static Position fromConfig(ModConfig.Position position, Window window, int width, int height, int offsetX, int offsetY) {
        switch (position) {
            case TOP_LEFT -> {
                return new Position(offsetX, offsetY);
            }
            case TOP_RIGHT -> {
                return new Position(window.getScaledWidth() - width + offsetX, offsetY);
            }
            case BOTTOM_LEFT -> {
                return new Position(offsetX, window.getScaledHeight() - height + offsetY);
            }
            default -> {
                // also BOTTOM_RIGHT
                return new Position(window.getScaledWidth() - width + offsetX, window.getScaledHeight() - height + offsetY);
            }
        }
    }
}
