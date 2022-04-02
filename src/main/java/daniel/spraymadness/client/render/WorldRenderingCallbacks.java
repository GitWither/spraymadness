package daniel.spraymadness.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.util.Spray;
import daniel.spraymadness.client.util.gui.DrawHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.block.enums.StairShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
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
    private static final float RADIUS = 0.5f;
    private static final float SCALE = 2.0f;
    private static final float OFFSET = 0.5f;
    
    public static boolean renderSprays(WorldRenderContext ctx, HitResult ctx2) {
        ctx.matrixStack().push();
        ctx.matrixStack().translate(-ctx.camera().getPos().x, -ctx.camera().getPos().y, -ctx.camera().getPos().z);

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL_LEQUAL);
        RenderSystem.disableCull();

        RenderSystem.setShader(SprayMadness::getSprayShader);

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.scale(0.99975586f, 0.99975586f, 0.99975586f);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        Identifier dimensionId = ctx.world().getRegistryKey().getValue();
        for (Spray spray : SprayMadness.totalSprays) {
            if (!spray.getDimension().equals(dimensionId)) continue;

            BUFFER_BUILDER.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
            RenderSystem.setShaderTexture(0, spray.getTextureIdentifier());

            ctx.matrixStack().push();
            ctx.matrixStack().translate(spray.getPos().getX(), spray.getPos().getY(), spray.getPos().getZ());

            Vec3f sprayPos = spray.getPos();

            Vec3f facingVector = spray.getFace().getOpposite().getUnitVector();
            int x1 = MathHelper.floor(sprayPos.getX() - RADIUS - 0.4 - 0.3 * facingVector.getX());
            int x2 = MathHelper.floor(sprayPos.getX() + RADIUS + 0.4 - 0.3 * facingVector.getX());
            int y1 = MathHelper.floor(sprayPos.getY() - RADIUS - 0.4 - 0.3 * facingVector.getY());
            int y2 = MathHelper.floor(sprayPos.getY() + RADIUS + 0.4 - 0.3 * facingVector.getY());
            int z1 = MathHelper.floor(sprayPos.getZ() - RADIUS - 0.4 - 0.3 * facingVector.getZ());
            int z2 = MathHelper.floor(sprayPos.getZ() + RADIUS + 0.4 - 0.3 * facingVector.getZ());
            MatrixStack.Entry entry = ctx.matrixStack().peek();

            BlockPos blockPos1 = new BlockPos(x1, y1, z1);
            BlockPos blockPos2 = new BlockPos(x2, y2, z2);
            //DrawHelper.drawDebugSprayRange(x1, y1, z1, x2, y2, z2);
            //MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.WAX_ON.getType(), sprayPos.getX(), sprayPos.getY(), sprayPos.getZ(), 0, 0, 0);

            //VertexConsumer vertices = ctx.consumers().getBuffer(SHADOW_LAYER);
            for (BlockPos blockPos : BlockPos.iterate(blockPos1, blockPos2)) {
                //EntityRenderDispatcher.renderShadowPart(entry, vertexConsumer, world, blockPos, mobEntity2, d, e, f, opacity);
                //MinecraftClient.getInstance().getBlockRenderManager().renderBlock(Blocks.ACACIA_BUTTON.getDefaultState(), blockPos, ctx.world(), ctx.matrixStack(), BUFFER_BUILDER, false, MinecraftClient.getInstance().world.getRandom());
                renderSprayPart(BUFFER_BUILDER, ctx.world(), blockPos, entry, spray.getFace(), sprayPos.getX(), sprayPos.getY(), sprayPos.getZ());
            }


            ctx.matrixStack().pop();
            BUFFER_BUILDER.end();

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);


            BufferRenderer.draw(BUFFER_BUILDER);
        }

        MatrixStack matrixStack2 = RenderSystem.getModelViewStack();
        matrixStack2.pop();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        ctx.matrixStack().pop();

        return true;
    }

    private static void renderSprayPartUp(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minX, float minZ, float maxX, float maxZ, float minY) {
        float x1 = minX - x;
        float x2 = maxX - x;
        float y1 = minY - y - 1;
        float z1 = minZ - z;
        float z2 = maxZ - z;

        float u1 = -x1 / SCALE / RADIUS + OFFSET;
        float v1 = -x2 / SCALE / RADIUS + OFFSET;
        float u2 = -z1 / SCALE / RADIUS + OFFSET;
        float v2 = -z2 / SCALE / RADIUS + OFFSET;


        builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(u1, u2).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(u1, v2).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(v1, v2).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(v1, u2).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
    }

    private static void renderSprayPartDown(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minX, float minZ, float maxX, float maxZ, float minY) {
        float x1 = minX - x;
        float x2 = maxX - x;
        float y1 = minY - y + 1;
        float z1 = minZ - z;
        float z2 = maxZ - z;

        float u1 = -x1 / SCALE / RADIUS + OFFSET;
        float v1 = -x2 / SCALE / RADIUS + OFFSET;
        float u2 = -z1 / SCALE / RADIUS + OFFSET;
        float v2 = -z2 / SCALE / RADIUS + OFFSET;


        builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(u1, u2).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(u1, v2).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(v1, v2).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(v1, u2).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
    }

    private static void renderSprayPartNorth(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minX, float minY, float maxX, float maxY, float minZ) {
        float x1 = minX - x;
        float x2 = maxX - x;
        float y1 = minY - y;
        float y2 = maxY - y;
        float z1 = minZ - z + 1;

        float u1 = -x1 / SCALE / RADIUS + OFFSET;
        float v1 = -x2 / SCALE / RADIUS + OFFSET;
        float u2 = -y1 / SCALE / RADIUS + OFFSET;
        float v2 = -y2 / SCALE / RADIUS + OFFSET;

        DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x2, y2, z1, u1, v1, u2, v2);
    }

    private static void renderSprayPartSouth(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minX, float minY, float maxX, float maxY, float minZ) {
        float x1 = minX - x;
        float x2 = maxX - x;
        float y1 = minY - y;
        float y2 = maxY - y;
        float z1 = minZ - z - 1;

        float u1 = -x1 / SCALE / RADIUS + OFFSET;
        float v1 = -x2 / SCALE / RADIUS + OFFSET;
        float u2 = -y1 / SCALE / RADIUS + OFFSET;
        float v2 = -y2 / SCALE / RADIUS + OFFSET;

        DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x2, y2, z1, u1, v1, u2, v2);
    }

    private static void renderSprayPartEast(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minZ, float minY, float maxZ, float maxY, float minX) {
        //minY - y - (1.0f - (float) box.maxY)
        float x1 = minX - x - 1;
        float y1 = minY - y;
        float y2 = maxY - y;
        float z1 = minZ - z;
        float z2 = maxZ - z;

        float u1 = -z1 / SCALE / RADIUS + OFFSET;
        float v1 = -z2 / SCALE / RADIUS + OFFSET;
        float u2 = -y1 / SCALE / RADIUS + OFFSET;
        float v2 = -y2 / SCALE / RADIUS + OFFSET;


        //String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light
        DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x1, y2, z2, u1, v1, u2, v2);

    }

    private static void renderSprayPartWest(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minZ, float minY, float maxZ, float maxY, float minX) {
        //minY - y - (1.0f - (float) box.maxY)
        float x1 = minX - x + 1;
        float y1 = minY - y;
        float y2 = maxY - y;
        float z1 = minZ - z;
        float z2 = maxZ - z;

        float u1 = -z1 / SCALE / RADIUS + OFFSET;
        float v1 = -z2 / SCALE / RADIUS + OFFSET;
        float u2 = -y1 / SCALE / RADIUS + OFFSET;
        float v2 = -y2 / SCALE / RADIUS + OFFSET;


        //String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light
        DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x1, y2, z2, u1, v1, u2, v2);

    }

    private static void renderSprayPart(BufferBuilder builder, WorldView world, BlockPos pos, MatrixStack.Entry matrixEntry, Direction direction, float x, float y, float z) {
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);


        if (blockState.getRenderType() == BlockRenderType.INVISIBLE) {
            return;
        }

        VoxelShape voxelShape = blockState.getOutlineShape(world, blockPos);
        if (voxelShape.isEmpty()) {
            return;
        }


        Box box = voxelShape.getBoundingBox();

        float minX = (float) (pos.getX() + box.minX);
        float maxX = (float) (pos.getX() + box.maxX);
        float minY = (float) (pos.getY() + box.minY);
        float maxY = (float) (pos.getY() + box.maxY);
        float minZ = (float) (pos.getZ() + box.minZ);
        float maxZ = (float) (pos.getZ() + box.maxZ);

        //DrawHelper.drawDebugSprayRange(minX, minY, minZ, maxX, maxY, maxZ);

        //TODO: Make this work for stairs

        if (direction == Direction.UP) {
            renderSprayPartUp(builder, matrixEntry, x, y, z, minX, minZ, maxX, maxZ, maxY);
        }
        if (direction == Direction.DOWN) {
            renderSprayPartDown(builder, matrixEntry, x, y, z, minX, minZ, maxX, maxZ, minY);
        }
        if (direction == Direction.NORTH) {
            renderSprayPartNorth(builder, matrixEntry, x, y, z, minX, minY, maxX, maxY, minZ);
        }
        if (direction == Direction.SOUTH) {
            renderSprayPartSouth(builder, matrixEntry, x, y, z, minX, minY, maxX, maxY, maxZ);
        }
        if (direction == Direction.EAST) {
            renderSprayPartEast(builder, matrixEntry, x, y, z, minZ, minY, maxZ, maxY, maxX);
        }
        if (direction == Direction.WEST) {
            renderSprayPartWest(builder, matrixEntry, x, y, z, minZ, minY, maxZ, maxY, minX);
        }


        /*
        if (blockState.getBlock() instanceof StairsBlock && blockState.get(StairsBlock.SHAPE) == StairShape.STRAIGHT) {

            //System.out.println(blockState.get(HorizontalFacingBlock.FACING).ordinal() + " : " + blockState.get(HorizontalFacingBlock.FACING).asString());
            switch (blockState.get(HorizontalFacingBlock.FACING).ordinal()) {
                //North
                case 2 -> {
                    z2 -= 0.5;
                }
                //South
                case 3 -> {
                    z1 += 0.5;
                }
                //West
                case 4 -> {
                    x2 -= 0.5;
                }
                //East
                case 5 -> {
                    x1 += 0.5;
                }
            }

            o = -x1 / SCALE / RADIUS + OFFSET;
            p = -x2 / SCALE / RADIUS + OFFSET;
            q = -z1 / SCALE / RADIUS + OFFSET;
            r = -z2 / SCALE / RADIUS + OFFSET;

            DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1 - 0.5f, z1, x2, y1 - 0.5f, z2, o, p, q - 0.5f, r - 0.5f);
            //builder.vertex(matrixEntry.getPositionMatrix(), x1, y1 - 0.5f, z1 + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, q - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            //builder.vertex(matrixEntry.getPositionMatrix(), x1, y1 - 0.5f, z2 + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, r - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            //builder.vertex(matrixEntry.getPositionMatrix(), x2, y1 - 0.5f, z2 + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, r - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            //builder.vertex(matrixEntry.getPositionMatrix(), x2, y1 - 0.5f, z1 + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, q - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();

        }

         */



        //System.out.println(MinecraftClient.getInstance().getNetworkHandler().getConnection().getAddress());

        //DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x2, y1, z2, o, p, q, r);
       // builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, q).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        //builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, r).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        //builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, r).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        //builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, q).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
    }

}
