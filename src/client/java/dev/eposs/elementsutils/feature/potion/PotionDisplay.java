package dev.eposs.elementsutils.feature.potion;

import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;
import net.minecraft.util.collection.DefaultedList;

public class PotionDisplay {
    private static int smallHeal = 0;
    private static int largeHeal = 0;
    private static int smallMana = 0;
    private static int largeMana = 0;

    public static void updatePotions(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        
        smallHeal = 0;
        largeHeal = 0;
        smallMana = 0;
        largeMana = 0;

        DefaultedList<ItemStack> stacks = client.player.getInventory().main;
        stacks.stream()
                .filter(stack -> !stack.isEmpty() && stack.getItem() == Items.POTION)
                .forEach(stack -> {
                    NbtComponent customData = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
                    if (customData != null) {
                        String tag = customData.copyNbt().getString("tag");
                        int count = stack.getCount();

                        switch (tag) {
                            case "heal_potion_small" -> smallHeal += count;
                            case "heal_potion_large" -> largeHeal += count;
                            case "mana_potion_small" -> smallMana += count;
                            case "mana_potion_large" -> largeMana += count;
                        }
                    }
                });
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (!ModConfig.getConfig().potionDisplay.show) return;


        context.drawText(client.textRenderer,
                "sh: " + smallHeal +
                        " lh: " + largeHeal +
                        " sm: " + smallMana +
                        " lm: " + largeMana,
                100, 100, Colors.WHITE, false
        );
    }
}
