package dev.eposs.elementsutils.basedisplay;

import dev.eposs.elementsutils.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class BaseDisplay {
    public static void register(WorldRenderContext context) {
        ModConfig config = ModConfig.getConfig();
        if (!config.baseDisplay.show) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ClientWorld world = client.world;

        List<VillagerEntity> villagers = new ArrayList<>();
        for (Entity entity1 : world.getEntities()) {
            if (!(entity1 instanceof VillagerEntity villager)) continue;

            ItemStack offHandStack = villager.getOffHandStack();
            if (offHandStack == null) continue;

            Text customName = offHandStack.getCustomName();
            if (customName == null) continue;

            // String baseOwnerName = customName.getString();
            //
            // List<String> playerNames = new ArrayList<>(config.baseDisplay.playerNames);
            // playerNames.add(client.player.getName().getString());
            //
            // if (!playerNames.contains(baseOwnerName)) continue;

            villagers.add(villager);
        }

        if (villagers.isEmpty()) return;
        
        var matrices = context.matrixStack();

        for (VillagerEntity villager : villagers) {
            matrices.push();
            
            var pos = villager.getPos();
            Vec3d camPos = context.camera().getPos();

            matrices.translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);

            var posMatrix = matrices.peek().getPositionMatrix();
            var vertexConsumer = context.consumers().getBuffer(RenderLayer.getLines());

            var radius = 45;
            for (int i = 0; i < 360; i += 1) {
                var degInRad = Math.toRadians(i);
                var nextDegInRad = Math.toRadians(i + 1);

                // Current vertex
                float x = (float) (Math.cos(degInRad) * radius);
                float z = (float) (Math.sin(degInRad) * radius);

                // Next vertex (to connect the line)
                float nextX = (float) (Math.cos(nextDegInRad) * radius);
                float nextZ = (float) (Math.sin(nextDegInRad) * radius);

                // Draw line segment from current to next vertex
                vertexConsumer
                        .vertex(posMatrix, x, 0.1f, z)
                        .color(0, 255, 0, 255)
                        .normal(x / radius, 0f, z / radius);
                vertexConsumer
                        .vertex(posMatrix, nextX, 0.1f, nextZ)
                        .color(0, 255, 0, 255)
                        .normal(nextX / radius, 0f, nextZ / radius);

            }

            matrices.pop();
        }
    }
}
