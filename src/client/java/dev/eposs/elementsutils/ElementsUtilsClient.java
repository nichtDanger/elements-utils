package dev.eposs.elementsutils;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.displays.basedisplay.BaseDisplay;
import dev.eposs.elementsutils.displays.bosstimer.BossTimerData;
import dev.eposs.elementsutils.displays.bosstimer.BossTimerDisplay;
import dev.eposs.elementsutils.rendering.ScreenRendering;
import dev.eposs.elementsutils.util.DevUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class ElementsUtilsClient implements ClientModInitializer {
    private static KeyBinding baseDisplayToggle;
    private static KeyBinding bossTimerToggle;
    private static KeyBinding devUtils;

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        // Register Config
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);

        registerKeyBinding();
        registerEvents();

        BossTimerData.updateData();
    }

    private void registerEvents() {
        HudLayerRegistrationCallback.EVENT.register(ScreenRendering::register);

        WorldRenderEvents.LAST.register(BaseDisplay::register);

        ClientTickEvents.END_CLIENT_TICK.register(this::registerKeyEvents);
    }

    private void registerKeyBinding() {
        String category = "category." + ElementsUtils.MOD_ID + ".keys";

        baseDisplayToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                getKeyBindingTranslation("baseDisplayToggle"),
                GLFW.GLFW_KEY_Z,
                category
        ));

        bossTimerToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                getKeyBindingTranslation("bossTimerToggle"),
                GLFW.GLFW_KEY_V,
                category
        ));

        devUtils = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                getKeyBindingTranslation("devUtils"),
                GLFW.GLFW_KEY_UNKNOWN,
                category
        ));
    }

    private void registerKeyEvents(MinecraftClient client) {
        while (baseDisplayToggle.wasPressed()) {
            BaseDisplay.toggleDisplay(client);
        }
        while (bossTimerToggle.wasPressed()) {
            BossTimerDisplay.toggleDisplay(client);
        }
        while (devUtils.wasPressed()) {
            DevUtil.entityData(client);
        }
    }

    private String getKeyBindingTranslation(String keyBinding) {
        return "key." + ElementsUtils.MOD_ID + "." + keyBinding;
    }
}
