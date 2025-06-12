package dev.eposs.elementsutils.rendering;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.feature.bosstimer.BossTimerDisplay;
import dev.eposs.elementsutils.feature.moonphase.MoonPhaseDisplay;
import dev.eposs.elementsutils.feature.time.TimeDisplay;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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

        MoonPhaseDisplay.render(context, client);
        TimeDisplay.render(context, client);
        BossTimerDisplay.render(context, client);
    }
}
