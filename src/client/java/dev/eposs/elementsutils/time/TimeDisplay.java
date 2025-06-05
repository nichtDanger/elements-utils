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

            String texturePath;
            if (timeOfDay > 1000L && timeOfDay < 13000L) {
                // Day
                texturePath = "gui/containers/day.png";
            } else {
                // Night
                texturePath = "gui/containers/night.png";
            }

            int size = 16;

            Position position = Position.fromConfig(ModConfig.getConfig().timeDisplay.position, client.getWindow(),
                    size, size, 0, 0);

            // Draw image
            var texture = Identifier.of(ElementsUtils.MOD_ID, texturePath);
            context.drawTexture(
                    identifier -> RenderLayer.getGuiTextured(texture),
                    texture,
                    position.x(), position.y(),
                    0.0f, 0.0f,
                    size, size, size, size
            );
        });
    }
}
