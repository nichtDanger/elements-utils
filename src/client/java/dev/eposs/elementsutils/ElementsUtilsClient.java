package dev.eposs.elementsutils;

import dev.eposs.elementsutils.basedisplay.BaseDisplay;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.moonphase.MoonPhaseDisplay;
import dev.eposs.elementsutils.util.DevUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class ElementsUtilsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        // Register Config
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);

        registerEvents();
    }

    private void registerEvents() {
        HudLayerRegistrationCallback.EVENT.register(MoonPhaseDisplay::register);
        WorldRenderEvents.LAST.register(BaseDisplay::register);

        ClientTickEvents.END_CLIENT_TICK.register(DevUtil::rcd);
    }
}
