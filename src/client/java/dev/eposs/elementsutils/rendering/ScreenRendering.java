package dev.eposs.elementsutils.rendering;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.moonphase.MoonPhaseDisplay;
import dev.eposs.elementsutils.time.TimeDisplay;
import dev.eposs.elementsutils.util.Position;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static dev.eposs.elementsutils.util.Position.fromConfig;

public class ScreenRendering {

    public static void register(@NotNull LayeredDrawerWrapper layeredDrawer) {
        layeredDrawer.attachLayerAfter(
                IdentifiedLayer.MISC_OVERLAYS,
                Identifier.of(ElementsUtils.MOD_ID, "layer_after_misc_overlays"),
                ScreenRendering::render
        );
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // TODO: fix position
        ModConfig config = ModConfig.getConfig();

        RenderData moonPhaseRenderData = MoonPhaseDisplay.getRenderData(client);
        if (moonPhaseRenderData != null) {

            Position position = fromConfig(ModConfig.getConfig().moonPhaseDisplay.position, client.getWindow(), moonPhaseRenderData.size(), moonPhaseRenderData.size(), 0, 0);
            // Position position = Position.getDisplayPosition(Position.DisplayType.MOON_PHASE, client.getWindow(), moonPhaseRenderData.size());

            context.drawTexture(
                    identifier -> RenderLayer.getGuiTextured(moonPhaseRenderData.texture()),
                    moonPhaseRenderData.texture(),
                    position.x(), position.y(),
                    0.0f, 0.0f,
                    moonPhaseRenderData.size(), moonPhaseRenderData.size(),
                    moonPhaseRenderData.size(), moonPhaseRenderData.size()
            );
        }

        RenderData timeRenderData = TimeDisplay.getRenderData(client);
        if (timeRenderData != null) {
            Position position = fromConfig(ModConfig.getConfig().timeDisplay.position, client.getWindow(), timeRenderData.size(), timeRenderData.size(), 0, 0);
            // Position position = Position.getDisplayPosition(Position.DisplayType.TIME, client.getWindow(), timeRenderData.size());

            context.drawTexture(
                    identifier -> RenderLayer.getGuiTextured(timeRenderData.texture()),
                    timeRenderData.texture(),
                    position.x(), position.y(),
                    0.0f, 0.0f,
                    timeRenderData.size(), timeRenderData.size(),
                    timeRenderData.size(), timeRenderData.size()
            );
        }
    }
}
