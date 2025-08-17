package dev.eposs.elementsutils.feature.potion;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class PotionDisplay {
	private static final int ICON_SIZE = 16;
	private static final int GAP_BELOW_ICON = 2;
	private static final int BOTTOM_MARGIN = 2;
	private static final int MIN_GAP = 5;
	private static final String[] TEXTURES = {
			"small_heal.png", "big_heal.png", "small_mana.png", "big_mana.png"
	};

	private static int smallHeal = 0, bigHeal = 0, smallMana = 0, bigMana = 0;

	public static void updatePotions(MinecraftClient client) {
		if (client.player == null || client.world == null) return;
		smallHeal = bigHeal = smallMana = bigMana = 0;

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
							case "heal_potion_big" -> bigHeal += count;
							case "mana_potion_small" -> smallMana += count;
							case "mana_potion_big" -> bigMana += count;
						}
					}
				});
	}

	public static void render(DrawContext context, MinecraftClient client) {
		if (!ModConfig.getConfig().potionDisplay.show) return;

		int textHeight = client.textRenderer.fontHeight;
		int totalHeight = ICON_SIZE + GAP_BELOW_ICON + textHeight;
		int y = context.getScaledWindowHeight() - totalHeight - BOTTOM_MARGIN;

		int[] counts = {smallHeal, bigHeal, smallMana, bigMana};
		String[] countStrings = new String[counts.length];
		int[] textWidths = new int[counts.length];
		for (int i = 0; i < counts.length; i++) {
			countStrings[i] = String.valueOf(counts[i]);
			textWidths[i] = client.textRenderer.getWidth(countStrings[i]);
		}

		int start = getStartPosition(context, counts.length);

		int[] iconXs = new int[counts.length];
		iconXs[0] = start;
		for (int i = 1; i < counts.length; i++) {
			int maxTextWidth = Math.max(textWidths[i - 1], textWidths[i]);
			int dynamicGap = Math.max(1, maxTextWidth - ICON_SIZE);
			iconXs[i] = iconXs[i - 1] + ICON_SIZE + MIN_GAP + dynamicGap;
		}

		for (int i = 0; i < counts.length; i++) {
			draw(context, client.textRenderer, TEXTURES[i], countStrings[i], iconXs[i], y);
		}
	}

	private static int getStartPosition(DrawContext context, int count) {
		int xOffset = Math.max(0, ModConfig.getConfig().potionDisplay.xOffset);
		return switch (ModConfig.getConfig().potionDisplay.position) {
			case LEFT -> context.getScaledWindowWidth() / 2 - 200 - xOffset;
			case RIGHT -> context.getScaledWindowWidth() / 2 + 200 - ((ICON_SIZE + MIN_GAP) * count) + xOffset;
		};
	}

	private static void draw(DrawContext context, TextRenderer textRenderer, String texture, String countString, int x, int y) {
		context.drawTexture(
				RenderLayer::getGuiTextured,
				Identifier.of(ElementsUtils.MOD_ID, "gui/containers/" + texture),
				x, y, 0.0f, 0.0f, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE
		);
		int textWidth = textRenderer.getWidth(countString);
		int iconCenterX = x + ICON_SIZE / 2;
		int textX = iconCenterX - textWidth / 2 + 1;
		int textY = y + ICON_SIZE + GAP_BELOW_ICON;
		context.drawText(textRenderer, countString, textX, textY, Colors.WHITE, true);
	}
}