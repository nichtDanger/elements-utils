package dev.eposs.elementsutils;

import dev.eposs.elementsutils.api.commonmods.CommonModIdsApi;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.feature.bosstimer.BossTimerData;
import dev.eposs.elementsutils.feature.bosstimer.BossTimerDisplay;
import dev.eposs.elementsutils.feature.loot.LootSound;
import dev.eposs.elementsutils.feature.pet.PetDisplay;
import dev.eposs.elementsutils.feature.playerbase.BaseBorderDisplay;
import dev.eposs.elementsutils.feature.potion.PotionDisplay;
import dev.eposs.elementsutils.feature.xpmeter.XpMeter;
import dev.eposs.elementsutils.rendering.ScreenRendering;
import dev.eposs.elementsutils.util.DevUtil;
import dev.eposs.elementsutils.common.GameMessageHandler;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.apache.commons.codec.digest.DigestUtils;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.Collectors;

public class ElementsUtilsClient implements ClientModInitializer {
    private static KeyBinding baseDisplayToggle;
    private static KeyBinding bossTimerToggle;
    private static KeyBinding xpMeasureTrigger;
    private static KeyBinding timeMeasureTrigger;
    private static KeyBinding devUtils;

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        // Register Config
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);

        registerKeyBinding();
        registerEvents();

        BossTimerData.startUpdateTimers();
        CommonModIdsApi.fetchCommonModIds();
    }

    private void registerEvents() {
        HudLayerRegistrationCallback.EVENT.register(ScreenRendering::register);

        WorldRenderEvents.LAST.register(BaseBorderDisplay::render);

        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onLeave);

        ClientTickEvents.END_CLIENT_TICK.register(this::clientTick);

        ClientReceiveMessageEvents.ALLOW_GAME.register(this::onGameMessage);
    }

    private void clientTick(MinecraftClient client) {
        onKeyEvent(client);
        PetDisplay.updatePet(client);
        PotionDisplay.updatePotions(client);
        XpMeter.updateXpMeter(client);
        GameMessageHandler.processPendingCommands(client);
    }

    private void onJoin(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        runServerCheck(client);
        PetDisplay.loadPet(handler.getRegistryManager());

        ModConfig.InternalConfig.Servers server = ModConfig.getConfig().internal.server;
        if (server != ModConfig.InternalConfig.Servers.UNKNOWN) {
            GameMessageHandler.queueModlistCommands(client, getUserMods(), "/modlist ", 256);
        }
    }

    private void onLeave(ClientPlayNetworkHandler handler, MinecraftClient client) {
        PetDisplay.savePet();
    }

    private void runServerCheck(MinecraftClient client) {
        ServerInfo serverEntry = client.getCurrentServerEntry();
        if (serverEntry == null) return;

        String server1 = "d2228bebe6cb6b55feb3258bc4aff39ffa41b6222a145951ba88916af1706553";
        String server2 = "1f4492b5647f7b11ebd28bc0bcea28bddebe39d83961803c9038ece28defda70";
        String hash = DigestUtils.sha3_256Hex(serverEntry.address);

        if (hash.equals(server1)) {
            ModConfig.getConfig().internal.server = ModConfig.InternalConfig.Servers.COMMUNITY_SERVER_1;
            ModConfig.save();
            ElementsUtils.LOGGER.info("Detected elements community server 1");
        } else if (hash.equals(server2)) {
            ModConfig.getConfig().internal.server = ModConfig.InternalConfig.Servers.COMMUNITY_SERVER_2;
            ModConfig.save();
            ElementsUtils.LOGGER.info("Detected elements community server 2");
        } else {
            ElementsUtils.LOGGER.warn("Unable to detect elements community server");
        }
    }

    private boolean onGameMessage(Text text, boolean b) {
        LootSound.onGameMessage(text);
		return GameMessageHandler.onGameMessage(text);
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

        xpMeasureTrigger = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                getKeyBindingTranslation("xpMeasureTrigger"),
                GLFW.GLFW_KEY_UNKNOWN,
                category
        ));

        timeMeasureTrigger = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                getKeyBindingTranslation("timeMeasureTrigger"),
                GLFW.GLFW_KEY_UNKNOWN,
                category
        ));

        devUtils = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                getKeyBindingTranslation("devUtils"),
                GLFW.GLFW_KEY_UNKNOWN,
                category
        ));
    }

    private void onKeyEvent(MinecraftClient client) {
        while (baseDisplayToggle.wasPressed()) {
            BaseBorderDisplay.toggleDisplay(client);
        }
        while (bossTimerToggle.wasPressed()) {
            BossTimerDisplay.toggleDisplay(client);
        }
        while (xpMeasureTrigger.wasPressed()) {
            XpMeter.startXPMeasurement(client);
        }
        while (timeMeasureTrigger.wasPressed()) {
            XpMeter.startTimeMeasurement(client);
        }
        while (devUtils.wasPressed()) {
            DevUtil.doSomething(client);
        }
    }

    private String getKeyBindingTranslation(String keyBinding) {
        return "key." + ElementsUtils.MOD_ID + "." + keyBinding;
    }

    private List<String> getUserMods() {
        List<String> commonModIds = CommonModIdsApi.getCachedCommonModIds();
        return FabricLoader.getInstance().getAllMods().stream()
                .filter(mod -> {
                    var meta = mod.getMetadata();
                    boolean isCommon = commonModIds.contains(meta.getId());
                    return !isCommon;
                })
                .map(mod -> mod.getMetadata().getId())
                .collect(Collectors.toList());
    }
}
