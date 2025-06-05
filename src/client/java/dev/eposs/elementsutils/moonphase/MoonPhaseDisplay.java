package dev.eposs.elementsutils.moonphase;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.util.Position;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MoonPhaseDisplay {

    public static void register(@NotNull LayeredDrawerWrapper layeredDrawer) {
        layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, Identifier.of(ElementsUtils.MOD_ID, "moonphase_layer_after_misc_overlays"), (context, tickCounter) -> {
            if (!ModConfig.getConfig().moonPhaseDisplay.show) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;
            ClientWorld world = client.world;

            MoonPhase moonPhase = MoonPhase.fromId(world.getMoonPhase());
            if (moonPhase == null) return;

            int size = 16;

            // Position position = Position.fromConfig(ModConfig.getConfig().moonPhaseDisplay.position, client.getWindow(),
            //         size, size, 0, 0);
            Position position = Position.getDisplayPosition(Position.DisplayType.MOON_PHASE, client.getWindow(), size);

            // Draw image
            var texture = Identifier.of(ElementsUtils.MOD_ID, moonPhase.getTexturePath());
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
