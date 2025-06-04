package dev.eposs.elementsutils;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.moonphase.MoonPhaseDisplay;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;

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
    }
}
