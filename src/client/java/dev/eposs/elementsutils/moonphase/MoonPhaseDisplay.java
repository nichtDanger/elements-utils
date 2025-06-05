package dev.eposs.elementsutils.moonphase;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.rendering.RenderData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class MoonPhaseDisplay {

    public static @Nullable RenderData getRenderData(MinecraftClient client) {
        if (!ModConfig.getConfig().showMoonPhaseDisplay) return null;

        assert client.world != null;
        MoonPhase moonPhase = MoonPhase.fromId(client.world.getMoonPhase());
        if (moonPhase == null) return null;

        var texture = Identifier.of(ElementsUtils.MOD_ID, moonPhase.getTexturePath());

        return new RenderData(texture, 16);
    }
}
