package dev.eposs.elementsutils.moonphase;

import dev.eposs.elementsutils.ElementsUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
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
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;
            if (world == null) return;
            MoonPhase moonPhase = MoonPhase.fromId(world.getMoonPhase());
            if (moonPhase == null) return;

            // Draw image
            var texture = Identifier.of(ElementsUtils.MOD_ID, moonPhase.getTexturePath());
            context.drawTexture(
                    identifier -> RenderLayer.getGuiTextured(texture),
                    texture,
                    client.getWindow().getScaledWidth()-16, client.getWindow().getScaledHeight()-16,
                    0.0f, 0.0f,
                    16, 16,
                    16, 16
            );
        });
    }
}
