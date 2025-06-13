package dev.eposs.elementsutils.feature.pet;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.rendering.Position;
import dev.eposs.elementsutils.rendering.ScreenPositioning;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class PetDisplay {
    private static int currentXP = 0;
    private static int nextLvlXP = 0;

    public static void updateCurrentPetXP(Text text) {
        String msg = text.getString();
        if (!msg.contains("Pet: ")) return;

        int start = msg.indexOf("Pet: ");
        int slash = msg.indexOf("/", start);
        int end = msg.indexOf(" XP", slash);

        currentXP = Integer.parseInt(msg.substring(start + 5, slash));
        nextLvlXP = Integer.parseInt(msg.substring(slash + 1, end));
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (!ModConfig.getConfig().showPetDisplay) return;

        Position position = ScreenPositioning.getPetPosition(client.getWindow());

        renderCircle(context, new Position(position.x() + 16, position.y() + 16));
    }

    // TODO
    private static void renderIcon(@NotNull DrawContext context, @NotNull Position centerPosition) {

    }

    private static void renderCircle(@NotNull DrawContext context, @NotNull Position centerPosition) {
        int segments = 64;
        float innerRadius = 10;
        float outerRadius = 12;
        float centerX = centerPosition.x();
        float centerY = centerPosition.y();
        int white = 0xFFFFFFFF;
        int green = 0xFF00FF00;
        
        Matrix4f transformationMatrix = context.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            // Start at the top
            float angle = (float) (Math.PI * (2.0F * i / segments + 1.5F));
            float angleNext = (float) (Math.PI * (2.0F * (i + 1) / segments + 1.5F));

            float outerX1 = centerX + MathHelper.cos(angle) * outerRadius;
            float outerY1 = centerY + MathHelper.sin(angle) * outerRadius;

            float outerX2 = centerX + MathHelper.cos(angleNext) * outerRadius;
            float outerY2 = centerY + MathHelper.sin(angleNext) * outerRadius;

            float innerX1 = centerX + MathHelper.cos(angle) * innerRadius;
            float innerY1 = centerY + MathHelper.sin(angle) * innerRadius;

            float innerX2 = centerX + MathHelper.cos(angleNext) * innerRadius;
            float innerY2 = centerY + MathHelper.sin(angleNext) * innerRadius;

            int color = green;
            if (nextLvlXP > 0 && (float) i / segments > (float) currentXP / nextLvlXP) color = white;

            buffer.vertex(transformationMatrix, innerX1, innerY1, 0).color(color);
            buffer.vertex(transformationMatrix, innerX2, innerY2, 0).color(color);
            buffer.vertex(transformationMatrix, outerX2, outerY2, 0).color(color);
            buffer.vertex(transformationMatrix, outerX1, outerY1, 0).color(color);
        }
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
