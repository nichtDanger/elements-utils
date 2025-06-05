package dev.eposs.elementsutils.time;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.rendering.RenderData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TimeDisplay {

    public static @Nullable RenderData getRenderData(MinecraftClient client) {
        if (!ModConfig.getConfig().showTimeDisplay) return null;

        assert client.world != null;
        long timeOfDay = client.world.getTimeOfDay() % 24000L;

        String texturePath = getClockTexture(timeOfDay);

        var texture = Identifier.of(texturePath);

        return new RenderData(texture, 16);
    }

    private static String getClockTexture(long time) {
        // 00 : noon ( 6000 )
        // 16 : night start ( 13000 )
        // 32 : midnight ( 18000 )
        // 48 : day start ( 1000 )

        // Shift the timeline so that noon (6000) becomes position 0
        long shifted = (time - 6000 + 24000) % 24000;

        // Map the shifted value to 0-63 range
        int id = (int) (shifted * 64 / 24000);

        // Add zero padding
        return String.format("textures/item/clock_%02d.png", id);
    }
}
