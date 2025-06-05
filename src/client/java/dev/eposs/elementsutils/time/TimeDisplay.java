package dev.eposs.elementsutils.time;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.util.Position;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TimeDisplay {

    public static void register(@NotNull LayeredDrawerWrapper layeredDrawer) {
        layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, Identifier.of(ElementsUtils.MOD_ID, "time_layer_after_misc_overlays"), (context, tickCounter) -> {
            if (!ModConfig.getConfig().timeDisplay.show) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            long timeOfDay = client.world.getTimeOfDay() % 24000L;

            String timeText;
            int color;
            if (timeOfDay > 1000L && timeOfDay < 13000L) {
                // Day
                timeText = "☀";
                color = Colors.YELLOW;
            } else {
                // Night
                timeText = "🌙";
                color = Colors.BLUE;
            }

            Position position = Position.fromConfig(ModConfig.getConfig().timeDisplay.position, client.getWindow(),
                    32, 16, 10, 10);

            context.drawText(
                    client.textRenderer,
                    timeText,
                    position.x(), position.y(),
                    color, false
            );
        });
    }
}
