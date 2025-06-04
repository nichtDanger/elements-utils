package dev.eposs.elementsutils;

import dev.eposs.elementsutils.moonphase.MoonPhaseDisplay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;

public class ElementsUtilsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		
		registerEvents();
	}


	private void registerEvents() {
		HudLayerRegistrationCallback.EVENT.register(MoonPhaseDisplay::register);
	}
}