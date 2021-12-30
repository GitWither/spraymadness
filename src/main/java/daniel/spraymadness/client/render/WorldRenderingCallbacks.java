package daniel.spraymadness.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.util.Spray;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11C.*;

public class WorldRenderingCallbacks
{
    private static final BufferBuilder BUFFER_BUILDER = new BufferBuilder(4);
    
    public static boolean renderSprays(WorldRenderContext ctx, HitResult ctx2) {
        ctx.matrixStack().push();
        ctx.matrixStack().translate(-ctx.camera().getPos().x, -ctx.camera().getPos().y, -ctx.camera().getPos().z);

        BUFFER_BUILDER.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);

        for (Spray spray : SprayMadness.totalSprays) {

            RenderSystem.setShaderTexture(0, spray.getTextureIdentifier());

            ctx.matrixStack().push();
            ctx.matrixStack().translate(spray.getPos().getX(), spray.getPos().getY(), spray.getPos().getZ());

            ctx.matrixStack().push();

            int direction = spray.getFace().getDirection().offset();
            float rotation = (float) Math.toRadians(spray.getFace().asRotation());
            Direction.Axis axis = spray.getFace().getAxis();


            if (axis.isHorizontal()) {
                ctx.matrixStack().translate(0.0f, 0, 0.001f * direction);
                ctx.matrixStack().multiply(Quaternion.fromEulerXyz(new Vec3f(0, rotation, 0)));
            }
            else {
                ctx.matrixStack().translate(0, 0.001f * direction, 0);
                ctx.matrixStack().multiply(Quaternion.fromEulerXyz(new Vec3f(rotation, 0, 0)));
            }

            //ctx.matrixStack().translate(0.0f, 0, 0.01f);
            //ctx.matrixStack().translate(0.499f, 0, 0);

            Matrix4f pos = ctx.matrixStack().peek().getPositionMatrix();

            //uploadCubeData(bb, pos);
            BUFFER_BUILDER.vertex(pos, - 0.5f, -0.5f, 0).color(1, 1, 1, 1.0f).texture(0, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
            BUFFER_BUILDER.vertex(pos, -0.5f, 0.5f, 0).color(1, 1, 1, 1.0f).texture(0, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
            BUFFER_BUILDER.vertex(pos, 0.5f, 0.5f, 0).color(1, 1, 1, 1.0f).texture(1, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();
            BUFFER_BUILDER.vertex(pos,  0.5f, -0.5f, 0).color(1, 1, 1, 1.0f).texture(1, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 1, 1, 1).next();


            ctx.matrixStack().pop();
            ctx.matrixStack().pop();
        }

        BUFFER_BUILDER.end();

        RenderSystem.setShaderColor(1f, 0.5f, 1f, 1f);
        RenderSystem.setShader(SprayMadness::getSprayShader);
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-0.0000000000000000001f, 2);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        //sprays' fragments will only be rendered if their depth value is equals the current depth value in the depth buffer
        //RenderSystem.depthFunc(GL_LESS);


        BufferRenderer.draw(BUFFER_BUILDER);


        RenderSystem.polygonOffset(0, 0);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableDepthTest();
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
