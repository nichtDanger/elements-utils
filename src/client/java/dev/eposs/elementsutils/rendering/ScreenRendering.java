package dev.eposs.elementsutils.rendering;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.moonphase.MoonPhaseDisplay;
import dev.eposs.elementsutils.time.TimeDisplay;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

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

        RenderData moonPhaseRenderData = MoonPhaseDisplay.getRenderData(client);
        if (moonPhaseRenderData != null) {
            Position position = ScreenPositioning.getMoonPhasePosition(client.getWindow());

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
            Position position = ScreenPositioning.getTimePosition(client.getWindow());

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
