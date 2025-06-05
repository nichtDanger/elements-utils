package dev.eposs.elementsutils.util;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.util.Window;

import java.util.Arrays;

public record Position(int x, int y) {
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
