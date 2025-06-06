package dev.eposs.elementsutils.displays.basedisplay;

import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.util.Util;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class BaseDisplay {
    public static void toggleDisplay(@NotNull MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        ModConfig.getConfig().showBaseDisplay = !ModConfig.getConfig().showBaseDisplay;
        AutoConfig.getConfigHolder(ModConfig.class).save();
        
        Util.sendChatMessage(Text.literal("Base display is now ")
                .append(Text.literal(ModConfig.getConfig().showBaseDisplay ? "enabled" : "disabled")
                        .formatted(ModConfig.getConfig().showBaseDisplay ? Formatting.GREEN : Formatting.RED))
        );
    }
    
    public static void register(WorldRenderContext context) {
        ModConfig config = ModConfig.getConfig();
        if (!config.showBaseDisplay) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ClientWorld world = client.world;

        List<VillagerEntity> villagers = new ArrayList<>();
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof VillagerEntity villager)) continue;
            
            if (!config.devUtils.enable) {
                ItemStack offHandStack = villager.getOffHandStack();
                if (offHandStack == null) continue;
                if (offHandStack.getItem() != Items.WOODEN_SWORD) continue;

                Text customName = offHandStack.getCustomName();
                if (customName == null) continue;
            }
            
            // String baseOwnerName = customName.getString();
            //
            // List<String> playerNames = new ArrayList<>(config.baseDisplay.playerNames);
            // playerNames.add(client.player.getName().getString());
            //
            // if (!playerNames.contains(baseOwnerName)) continue;

            villagers.add(villager);
        }

        if (villagers.isEmpty()) return;

        MatrixStack matrices = context.matrixStack();
        assert matrices != null;
        
        Vec3d camPos = context.camera().getPos();

        int radius = 45;
        int stacks = 32; // Number of horizontal slices (like latitude lines)
        int sectors = 64; // Number of vertical slices (like longitude lines)

        for (VillagerEntity villager : villagers) {
            Vec3d pos = villager.getPos();

            matrices.push();
            
            // Translate to camera-relative space
            Vec3d target = new Vec3d(pos.x, pos.y, pos.z).subtract(camPos);

            VertexConsumerProvider.Immediate vcp = (VertexConsumerProvider.Immediate) context.consumers();
            assert vcp != null;
            renderSolidSphere(matrices, vcp, target, radius, stacks, sectors); // Adjust radius and segments

            matrices.pop();
            vcp.draw(); // Flush
        }
    }

    private static void renderSolidSphere(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, Vec3d center, float radius, int stacks, int sectors) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(Identifier.of("textures/misc/white.png")));

        for (int i = 0; i < stacks; i++) {
            float phi1 = (float) (Math.PI * i / stacks);
            float phi2 = (float) (Math.PI * (i + 1) / stacks);

            for (int j = 0; j < sectors; j++) {
                float theta1 = (float) (2 * Math.PI * j / sectors);
                float theta2 = (float) (2 * Math.PI * (j + 1) / sectors);

                // Vertex positions relative to center
                float x1 = (float) (radius * Math.sin(phi1) * Math.cos(theta1));
                float y1 = (float) (radius * Math.cos(phi1));
                float z1 = (float) (radius * Math.sin(phi1) * Math.sin(theta1));

                float x2 = (float) (radius * Math.sin(phi2) * Math.cos(theta1));
                float y2 = (float) (radius * Math.cos(phi2));
                float z2 = (float) (radius * Math.sin(phi2) * Math.sin(theta1));

                float x3 = (float) (radius * Math.sin(phi2) * Math.cos(theta2));
                float y3 = (float) (radius * Math.cos(phi2));
                float z3 = (float) (radius * Math.sin(phi2) * Math.sin(theta2));

                float x4 = (float) (radius * Math.sin(phi1) * Math.cos(theta2));
                float y4 = (float) (radius * Math.cos(phi1));
                float z4 = (float) (radius * Math.sin(phi1) * Math.sin(theta2));

                // Offset to center position
                x1 += (float) center.x; y1 += (float) center.y; z1 += (float) center.z;
                x2 += (float) center.x; y2 += (float) center.y; z2 += (float) center.z;
                x3 += (float) center.x; y3 += (float) center.y; z3 += (float) center.z;
                x4 += (float) center.x; y4 += (float) center.y; z4 += (float) center.z;

                // Two triangles per quad
                vertex(vertexConsumer, positionMatrix, x1, y1, z1);
                vertex(vertexConsumer, positionMatrix, x2, y2, z2);
                vertex(vertexConsumer, positionMatrix, x3, y3, z3);
                vertex(vertexConsumer, positionMatrix, x4, y4, z4);
            }
        }
    }


    private static void vertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z) {
        vc.vertex(matrix, x, y, z)
                .color(0, 255, 0, 128) // RGBA (green, 50% transparent)
                .texture(0.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(15728880) // Max brightness
                .normal(0, 1, 0);
    }

}
