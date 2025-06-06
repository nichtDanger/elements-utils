package dev.eposs.elementsutils.displays.bosstimer;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.util.Util;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class BossTimerDisplay {
    public static void toggleDisplay(@NotNull MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        ModConfig.getConfig().showBossTimers = !ModConfig.getConfig().showBossTimers;
        AutoConfig.getConfigHolder(ModConfig.class).save();
        
        if (ModConfig.getConfig().showBossTimers) {
            BossTimerData.updateData();
        }
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (!ModConfig.getConfig().showBossTimers) return;

        BossTimerData timerData = BossTimerData.getInstance();

        drawText(client, context, 0, Text.literal("Dungeon Boss Death Time:").formatted(Formatting.UNDERLINE));
        drawText(client, context, 1, formattedText("Axolotl", Formatting.LIGHT_PURPLE, timerData.getAxolotl()));
        drawText(client, context, 2, formattedText("Zombie", Formatting.GREEN, timerData.getZombie()));
        drawText(client, context, 3, formattedText("Spider", Formatting.DARK_GRAY, timerData.getSpider()));
        drawText(client, context, 4, formattedText("Bogged", Formatting.DARK_GREEN, timerData.getBogged()));
        drawText(client, context, 5, formattedText("Piglin", Formatting.RED, timerData.getPiglin()));
    }

    private static Text formattedText(String name, Formatting nameColor, ZonedDateTime time) {
        return Text.literal("")
                .append(Text.literal(name + ": ").formatted(nameColor))
                .append(time == null ? "unknown" : time.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
    }

    private static void drawText(MinecraftClient client, DrawContext context, int line, Text text) {
        int lineHeight = client.textRenderer.fontHeight + 3;
        context.drawText(
                client.textRenderer,
                text,
                4, (client.getWindow().getScaledHeight() / 2) - (lineHeight * 3) + (lineHeight * line),
                Colors.WHITE, false
        );
    }
}
