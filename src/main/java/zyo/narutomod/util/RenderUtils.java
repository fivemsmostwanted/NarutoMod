package zyo.narutomod.util;

import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class RenderUtils {

    public static void drawCircle(GuiGraphics graphics, int centerX, int centerY, float radius, int color) {
        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = graphics.pose().last().pose();

        int segments = 36;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) Math.toRadians(i * 10);
            float angle2 = (float) Math.toRadians((i + 1) * 10);

            float x1 = centerX + (float) Math.cos(angle1) * radius;
            float y1 = centerY + (float) Math.sin(angle1) * radius;
            float x2 = centerX + (float) Math.cos(angle2) * radius;
            float y2 = centerY + (float) Math.sin(angle2) * radius;

            bufferbuilder.vertex(matrix, centerX, centerY, 0.0F).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(matrix, x1, y1, 0.0F).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a).endVertex();
        }
        BufferUploader.drawWithShader(bufferbuilder.end());

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}