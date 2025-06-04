package dev.eposs.elementsutils.moonphase;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MoonPhaseDisplay {

    public static void register(@NotNull LayeredDrawerWrapper layeredDrawer) {
        layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, Identifier.of(ElementsUtils.MOD_ID, "moonphase_layer_after_misc_overlays"), (context, tickCounter) -> {
            if (!ModConfig.getConfig().moonPhaseDisplay.show) return;

            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;
            if (world == null) return;
            MoonPhase moonPhase = MoonPhase.fromId(world.getMoonPhase());
            if (moonPhase == null) return;

            Position position = getPosition(ModConfig.getConfig().moonPhaseDisplay.position, client.getWindow());

            // Draw image
            var texture = Identifier.of(ElementsUtils.MOD_ID, moonPhase.getTexturePath());
            context.drawTexture(
                    identifier -> RenderLayer.getGuiTextured(texture),
                    texture,
                    position.x, position.y,
                    0.0f, 0.0f,
                    16, 16,
                    16, 16
            );
        });
    }

    private record Position(int x, int y) {
    }

    private static Position getPosition(ModConfig.MoonPhaseDisplayConfig.Position  position, Window window) {
        switch (position) {
            case TOP_LEFT -> {
                return new Position(0, 0);
            }
            case TOP_RIGHT -> {
                return new Position(window.getScaledWidth() - 16, 0);
            }
            case BOTTOM_LEFT -> {
                return new Position(0, window.getScaledHeight() - 16);
            }
            default -> {
                // also BOTTOM_RIGHT
                return new Position(window.getScaledWidth() - 16, window.getScaledHeight() - 16);
            }
        }
    }
}
