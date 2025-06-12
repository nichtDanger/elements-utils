package dev.eposs.elementsutils.util;

import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;

public class DevUtil {
    public static void doSomething(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (!ModConfig.getConfig().devUtils.enable) return;

        if (client.crosshairTarget instanceof EntityHitResult entityHitResult) {
            client.player.sendMessage(Text.literal(entityHitResult.getEntity().getType().toString()), false);

            if (entityHitResult.getEntity() instanceof VillagerEntity villager) {
                String text = "...";
                // text = villager.getCustomName().getString();

                NbtCompound nbt = new NbtCompound();
                villager.writeNbt(nbt);
                ElementsUtils.LOGGER.info(nbt.toString());

                ItemStack offHandStack = villager.getOffHandStack();
                if (offHandStack != null) {
                    Text customName = offHandStack.getCustomName();
                    if (customName != null) {
                        text = customName.getString();
                    }
                } else {
                    text = "none";
                }

                client.player.sendMessage(Text.literal(text), false);
            }
        }
    }
}
