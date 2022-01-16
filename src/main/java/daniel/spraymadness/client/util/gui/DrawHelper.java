package daniel.spraymadness.client.util.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;

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
}
