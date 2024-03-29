package daniel.spraymadness.client.util.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.texture.SprayTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Matrix3f;
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

    public static void drawSolidColor(Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();


        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(r, g, b, a).next();

        BufferRenderer.drawWithShader(bufferBuilder.end());

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawSprayTexture(MatrixStack matrices, SprayTexture texture, int x, int y, int width, int height) {
        drawSprayTexture(matrices, texture, x, y, width, height, 1.0f);
    }


    public static void drawSprayTexture(MatrixStack matrices, SprayTexture texture, int x, int y, int width, int height, float opacity) {
        Matrix4f posMat = matrices.peek().getPositionMatrix();

        final int x1 = x + width;
        final int y1 = y + height;

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, texture.getIdentifier());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        bufferBuilder.vertex(posMat, x, y1, 0).texture(0, 1).color(1.0f, 1.0f, 1.0f, opacity).next();
        bufferBuilder.vertex(posMat, x1, y1, 0).texture(1, 1).color(1.0f, 1.0f, 1.0f, opacity).next();
        bufferBuilder.vertex(posMat, x1, y, 0).texture(1, 0).color(1.0f, 1.0f, 1.0f, opacity).next();
        bufferBuilder.vertex(posMat, x, y, 0).texture(0, 0).color(1.0f, 1.0f, 1.0f, opacity).next();


        BufferRenderer.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }


    public static void drawSprayTextureQuad(BufferBuilder builder, Matrix4f position, float x1, float y1, float z1, float x2, float y2, float z2, float u1, float v1, float u2, float v2, int light) {
        drawSprayTextureQuad(builder, position, x1, y1, z1, x2, y2, z2, u1, v1, u2, v2, light, false);
    }

    public static void drawSprayTextureQuad(BufferBuilder builder, Matrix4f position, float x1, float y1, float z1, float x2, float y2, float z2, float u1, float v1, float u2, float v2, int light, boolean rotated) {
        boolean vertical = y2 - y1 > 0;

        builder.vertex(position, x1, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(u1, rotated ? v2 : u2).light(light).next();
        builder.vertex(position, x1, y2, vertical ? z1 : z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(rotated ? v1 : u1, v2).light(light).next();
        builder.vertex(position, x2, y2, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(v1, rotated ? u2 : v2).light(light).next();
        builder.vertex(position, x2, y1, vertical ? z2 : z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(rotated ? u1 : v1, u2).light(light).next();
    }

    public static void drawDebugSprayRange(float x1, float y1, float z1, float x2, float y2, float z2) {
        x1 += 0.5f;
        x2 += 0.5f;
        y1 += 0.5f;
        y2 += 0.5f;
        z1 += 0.5f;
        z2 += 0.5f;

        final float width = x2 - x1;
        final float depth = z2 - z1;

        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), x1, y1, z1, 0, 0, 0);
        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), x2, y2, z2, 0, 0, 0);

        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), x2 - width, y2, z2 - depth, 0, 0, 0);
        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), x2, y2, z2 - depth, 0, 0, 0);
        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), x2 - width, y2, z2, 0, 0, 0);

        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), x2, y1, z2, 0, 0, 0);
        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), x2, y1, z2 - depth, 0, 0, 0);
        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), x2 - width, y1, z2, 0, 0, 0);
    }
}
