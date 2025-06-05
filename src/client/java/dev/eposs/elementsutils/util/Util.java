package dev.eposs.elementsutils.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Util {

    public static void sendChatMessage(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        client.player.sendMessage(Text.literal("")
                        .append(Text.literal("[ElementsUtils] ").formatted(Formatting.GOLD))
                        .append(message)
                , false);
    }
}
