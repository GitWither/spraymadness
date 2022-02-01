package daniel.spraymadness.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.util.Spray;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.block.enums.StairShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11C.*;

public class WorldRenderingCallbacks
{
    private static final BufferBuilder BUFFER_BUILDER = new BufferBuilder(256);
    
    public static boolean renderSprays(WorldRenderContext ctx, HitResult ctx2) {
        ctx.matrixStack().push();
        ctx.matrixStack().translate(-ctx.camera().getPos().x, -ctx.camera().getPos().y, -ctx.camera().getPos().z);

        BUFFER_BUILDER.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);

        for (Spray spray : SprayMadness.totalSprays) {
            RenderSystem.setShaderTexture(0, spray.getTextureIdentifier());

            ctx.matrixStack().push();
            ctx.matrixStack().translate(spray.getPos().getX(), spray.getPos().getY(), spray.getPos().getZ());

            Vec3f sprayPos = spray.getPos();

            final float radius = 0.5f;
            int x1 = MathHelper.floor(sprayPos.getX() - radius);
            int x2 = MathHelper.floor(sprayPos.getX() + radius);
            int y1 = MathHelper.floor(sprayPos.getY() - radius);
            //this is to make it placeable on low blocks
            int y2 = MathHelper.ceil(sprayPos.getY() + radius);
            int z1 = MathHelper.floor(sprayPos.getZ() - radius);
            int z2 = MathHelper.floor(sprayPos.getZ() + radius);
            MatrixStack.Entry entry = ctx.matrixStack().peek();

            //VertexConsumer vertices = ctx.consumers().getBuffer(SHADOW_LAYER);
            for (BlockPos blockPos : BlockPos.iterate(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2))) {
                //EntityRenderDispatcher.renderShadowPart(entry, vertexConsumer, world, blockPos, mobEntity2, d, e, f, opacity);
                //MinecraftClient.getInstance().getBlockRenderManager().renderBlock(Blocks.ACACIA_BUTTON.getDefaultState(), blockPos, ctx.world(), ctx.matrixStack(), BUFFER_BUILDER, false, MinecraftClient.getInstance().world.getRandom());
                renderSprayPart(BUFFER_BUILDER, ctx.world(), blockPos, entry, sprayPos.getX(), sprayPos.getY(), sprayPos.getZ());
            }

            ctx.matrixStack().pop();
        }

        BUFFER_BUILDER.end();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(SprayMadness::getSprayShader);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL_LEQUAL);

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.scale(0.99975586f, 0.99975586f, 0.99975586f);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);


        BufferRenderer.draw(BUFFER_BUILDER);


        MatrixStack matrixStack2 = RenderSystem.getModelViewStack();
        matrixStack2.pop();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        ctx.matrixStack().pop();

        return true;
    }

    private static void renderSprayPart(BufferBuilder builder, WorldView world, BlockPos pos, MatrixStack.Entry matrixEntry, double x, double y, double z) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.getRenderType() == BlockRenderType.INVISIBLE) {
            return;
        }

        VoxelShape voxelShape = blockState.getOutlineShape(world, pos.down());
        if (voxelShape.isEmpty()) {
            return;
        }

        final float radius = 0.5f;

        Box box = voxelShape.getBoundingBox();
        double d = (double)pos.getX() + box.minX;
        double e = (double)pos.getX() + box.maxX;
        double g = (double)pos.getY() + box.minY;
        double h = (double)pos.getZ() + box.minZ;
        double i = (double)pos.getZ() + box.maxZ;

        float j = (float)(d - x);
        float k = (float)(e - x);
        float l = (float)(g - y) - (1.0f - (float)box.maxY);
        float m = (float)(h - z);
        float n = (float)(i - z);

        final float scale = 2.0f;
        final float offset = 0.5f;

        //TODO: Make this work for stairs

        float o = -j / scale / radius + offset;
        float p = -k / scale / radius + offset;
        float q = -m / scale / radius + offset;
        float r = -n / scale / radius + offset;

        if (blockState.getBlock() instanceof StairsBlock && blockState.get(StairsBlock.SHAPE) == StairShape.STRAIGHT) {

            //System.out.println(blockState.get(HorizontalFacingBlock.FACING).ordinal() + " : " + blockState.get(HorizontalFacingBlock.FACING).asString());
            switch (blockState.get(HorizontalFacingBlock.FACING).ordinal()) {
                //North
                case 2 -> {
                    n -= 0.5;
                }
                //South
                case 3 -> {
                    m += 0.5;
                }
                //West
                case 4 -> {
                    k -= 0.5;
                }
                //East
                case 5 -> {
                    j += 0.5;
                }
            }

            o = -j / scale / radius + offset;
            p = -k / scale / radius + offset;
            q = -m / scale / radius + offset;
            r = -n / scale / radius + offset;

            builder.vertex(matrixEntry.getPositionMatrix(), j, l - 0.5f, m + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, q - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            builder.vertex(matrixEntry.getPositionMatrix(), j, l - 0.5f, n + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, r - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            builder.vertex(matrixEntry.getPositionMatrix(), k, l - 0.5f, n + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, r - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            builder.vertex(matrixEntry.getPositionMatrix(), k, l - 0.5f, m + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, q - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();

        }



        //System.out.println(MinecraftClient.getInstance().getNetworkHandler().getConnection().getAddress());
        builder.vertex(matrixEntry.getPositionMatrix(), j, l, m).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, q).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), j, l, n).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, r).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), k, l, n).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, r).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), k, l, m).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, q).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
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
