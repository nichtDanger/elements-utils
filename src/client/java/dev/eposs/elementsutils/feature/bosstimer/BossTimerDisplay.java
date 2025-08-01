package dev.eposs.elementsutils.feature.bosstimer;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class BossTimerDisplay {
    public static void toggleDisplay(@NotNull MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        ModConfig.getConfig().bossTimer.show = !ModConfig.getConfig().bossTimer.show;
        ModConfig.save();

        if (ModConfig.getConfig().bossTimer.show) {
            BossTimerData.updateData();
        }
    }

    public static void render(DrawContext context, MinecraftClient client) {
        ModConfig.BossTimerConfig config = ModConfig.getConfig().bossTimer;
        if (!config.show) return;

        BossTimerData timerData = BossTimerData.getInstance();

        drawText(client, context, 0, Text.translatable("elements-utils.display.bossTimer.title").formatted(Formatting.UNDERLINE));
        drawText(client, context, 1, formattedText("Axolotl", Formatting.LIGHT_PURPLE, timerData.getAxolotl(), config));
        drawText(client, context, 2, formattedText("Zombie", Formatting.GREEN, timerData.getZombie(), config));
        drawText(client, context, 3, formattedText("Spider", Formatting.DARK_GRAY, timerData.getSpider(), config));
        drawText(client, context, 4, formattedText("Bogged", Formatting.DARK_GREEN, timerData.getBogged(), config));
        drawText(client, context, 5, formattedText("Piglin", Formatting.RED, timerData.getPiglin(), config));
    }

    private static final DateTimeFormatter ABSOLUTE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static Text formattedText(String name, Formatting nameColor, ZonedDateTime time, ModConfig.BossTimerConfig config) {
        return Text.literal("")
                .append(Text.literal(name + ": ").formatted(config.colorBossNames ? nameColor : Formatting.WHITE))
                .append(time == null ? Text.translatable("elements-utils.unknown") :
                        config.bossTimeFormat == ModConfig.TimeFormat.RELATIVE
                                ? toRelativeTime(time)
                                : Text.literal(time.format(ABSOLUTE_FORMATTER)))
                .formatted(config.colorBossTime ? getTimeColor(time) : Formatting.WHITE);
    }

    private static Formatting getTimeColor(ZonedDateTime dateTime) {
        if (dateTime == null) return Formatting.WHITE;

        Duration duration = getDuration(dateTime);

        if (duration.isNegative()) return Formatting.WHITE;

        long hours = duration.toHours();

        if (hours >= 36 && hours < 48) {
            return Formatting.YELLOW;
        }

        if (hours >= 48) {
            return Formatting.RED;
        }

        return Formatting.GREEN;
    }

    private static Text toRelativeTime(@NotNull ZonedDateTime dateTime) {
        Duration duration = getDuration(dateTime);

        if (duration.isNegative()) {
            return Text.translatable("elements-utils.unknown");
        }

        long days = duration.toDays();
        duration = duration.minus(days, ChronoUnit.DAYS);
        long hours = duration.toHours();
        duration = duration.minus(hours, ChronoUnit.HOURS);
        long minutes = duration.toMinutes();

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }

        return Text.translatable("elements-utils.display.bossTimer.relative", sb.toString().trim());
    }

    private static void drawText(MinecraftClient client, DrawContext context, int line, Text text) {
        int lineHeight = client.textRenderer.fontHeight + 3;
        boolean outline = ModConfig.getConfig().bossTimer.textOutline;
        context.drawText(
                client.textRenderer,
                text,
                4, (client.getWindow().getScaledHeight() / 2) - (lineHeight * 3) + (lineHeight * line),
                net.minecraft.util.Colors.WHITE, outline
        );
    }

    private static Duration getDuration(@NotNull ZonedDateTime dateTime) {
        ZonedDateTime now = ZonedDateTime.now();
        return Duration.between(dateTime, now);
    }
}
