package daniel.spraymadness.client.util.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.texture.SprayTexture;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public class DrawHelper {

    public static void drawOptionsGradient(int top, int bottom, int left, int right, int scrollAmount) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(left, bottom, 0.0).texture((float)left / 32.0f, (float)(bottom + scrollAmount) / 32.0f).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(right, bottom, 0.0).texture((float)right / 32.0f, (float)(bottom + scrollAmount) / 32.0f).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(right, top, 0.0).texture((float)right / 32.0f, (float)(top + scrollAmount) / 32.0f).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(left, top, 0.0).texture((float)left / 32.0f, (float)(top + scrollAmount) / 32.0f).color(32, 32, 32, 255).next();

        tessellator.draw();
    }

    public static void drawSprayTexture(MatrixStack matrices, SprayTexture texture, int x, int y, int width, int height) {
        Matrix4f posMat = matrices.peek().getPositionMatrix();

        int x1 = x + width;
        int y1 = y + height;

        RenderSystem.setShaderTexture(0, texture.getIdentifier());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(posMat, x, y1, 0).texture(0, 1).next();
        bufferBuilder.vertex(posMat, x1, y1, 0).texture(1, 1).next();
        bufferBuilder.vertex(posMat, x1, y, 0).texture(1, 0).next();
        bufferBuilder.vertex(posMat, x, y, 0).texture(0, 0).next();

        bufferBuilder.end();

        BufferRenderer.draw(bufferBuilder);
    }
}
