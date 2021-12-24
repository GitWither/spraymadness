package daniel.spraymadness.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.util.Spray;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Matrix4f;

public class WorldRenderingCallbacks
{
    public static boolean renderSprays(WorldRenderContext ctx, HitResult ctx2) {
        //VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        //VertexConsumer consumer = immediate.getBuffer(RenderLayer.getText(new Identifier("test")));

        //MinecraftClient.getInstance().gameRenderer.getMapRenderer().draw(ctx.matrixStack(), ctx.consumers(), 0, state, false, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        //MinecraftClient.getInstance().getItemRenderer().renderItem(Items.BONE_BLOCK.getDefaultStack(), ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND, LightmapTextureManager.MAX_LIGHT_COORDINATE, 1, ctx.matrixStack(), ctx.consumers(), 1);
        //MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
        //       Blocks.DIAMOND_BLOCK.getDefaultState(),
        //        ctx.matrixStack(), ctx.consumers(), 15728880, OverlayTexture.DEFAULT_UV);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(SprayMadness::getSprayShader);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        ctx.matrixStack().push();
        ctx.matrixStack().translate(-ctx.camera().getPos().x, -ctx.camera().getPos().y, -ctx.camera().getPos().z);

        BufferBuilder bb = new BufferBuilder(6);

        for (Spray spray : SprayMadness.totalSprays) {
            RenderSystem.setShaderTexture(0, spray.getTextureIdentifier());

            ctx.matrixStack().push();
            ctx.matrixStack().translate(spray.getPos().getX(), spray.getPos().getY(), spray.getPos().getZ());

            Matrix4f pos = ctx.matrixStack().peek().getPositionMatrix();

            uploadCubeData(bb, pos);


            BufferRenderer.draw(bb);

            ctx.matrixStack().pop();
        }

        RenderSystem.enableCull();
        ctx.matrixStack().pop();

        return true;
    }

    private static void uploadCubeData(BufferBuilder bufferBuilder, Matrix4f position) {
        //front
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);

        bufferBuilder.vertex(position, -0.5f, -0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(0, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position, -0.5f, 0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(0, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position, 0.5f, 0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(1, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position, 0.5f, -0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(1, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();

        //left
        bufferBuilder.vertex(position,0.5f, -0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(0, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,0.5f, 0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(0, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,0.5f, 0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(1, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,0.5f, -0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(1, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();

        //back
        bufferBuilder.vertex(position,0.5f, -0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(0, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,0.5f, 0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(0, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,-0.5f, 0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(1, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,-0.5f, -0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(1, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();

        //right
        bufferBuilder.vertex(position,-0.5f, -0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(0, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,-0.5f, 0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(0, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,-0.5f, 0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(1, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,-0.5f, -0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(1, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();

        //top
        bufferBuilder.vertex(position,0.5f, 0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(0, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,0.5f, 0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(0, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,-0.5f, 0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(1, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,-0.5f, 0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(1, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();

        //bottom
        bufferBuilder.vertex(position,-0.5f, -0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(0, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,0.5f, -0.5f, -0.5f).color(1, 1, 1, 1.0f).texture(0, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,0.5f, -0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(1, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
        bufferBuilder.vertex(position,-0.5f, -0.5f, 0.5f).color(1, 1, 1, 1.0f).texture(1, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();

        bufferBuilder.end();
    }
}
