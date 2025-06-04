package dev.eposs.elementsutils.util;

import dev.eposs.elementsutils.ElementsUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

public class DevUtil {
    private static boolean wasPressed = false;

    public static void rcd(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // Detect right mouse button (button 1 is right click)
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean pressed = GLFW.glfwGetKey(windowHandle, GLFW_KEY_KP_0) == GLFW_PRESS;

        if (pressed && !wasPressed) {
            // Only trigger on press, not hold
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

        wasPressed = pressed;
    }
}
