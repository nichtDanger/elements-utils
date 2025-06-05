package dev.eposs.elementsutils.time;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.util.Position;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TimeDisplay {

    public static void register(@NotNull LayeredDrawerWrapper layeredDrawer) {
        layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, Identifier.of(ElementsUtils.MOD_ID, "time_layer_after_misc_overlays"), (context, tickCounter) -> {
            if (!ModConfig.getConfig().timeDisplay.show) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            long timeOfDay = client.world.getTimeOfDay() % 24000L;

            String texturePath = getClockTexture(timeOfDay);
            
            int size = 16;

            // Position position = Position.fromConfig(ModConfig.getConfig().timeDisplay.position, client.getWindow(),
            //         size, size, 0, 0);
            Position position = Position.getDisplayPosition(Position.DisplayType.TIME, client.getWindow(), size);

            // Draw image
            var texture = Identifier.of(texturePath);
            context.drawTexture(
                    identifier -> RenderLayer.getGuiTextured(texture),
                    texture,
                    position.x(), position.y(),
                    0.0f, 0.0f,
                    size, size, size, size
            );
        });
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
