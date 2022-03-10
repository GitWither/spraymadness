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

        for (Spray spray : SprayMadness.totalSprays) {
            BUFFER_BUILDER.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
            RenderSystem.setShaderTexture(0, spray.getTextureIdentifier());

            ctx.matrixStack().push();
            ctx.matrixStack().translate(spray.getPos().getX(), spray.getPos().getY(), spray.getPos().getZ());

            Vec3f sprayPos = spray.getPos();

            if (spray.getFace() == Direction.UP) {
                int x1 = MathHelper.floor(sprayPos.getX() - RADIUS);
                int x2 = MathHelper.floor(sprayPos.getX() + RADIUS);
                int y1 = MathHelper.floor(sprayPos.getY() - RADIUS);
                //this is to make it placeable on low blocks
                int y2 = MathHelper.ceil(sprayPos.getY() + RADIUS);
                int z1 = MathHelper.floor(sprayPos.getZ() - RADIUS);
                int z2 = MathHelper.floor(sprayPos.getZ() + RADIUS);
                MatrixStack.Entry entry = ctx.matrixStack().peek();

                MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), sprayPos.getX(), sprayPos.getY(), sprayPos.getZ(), 0, 0, 0);

                //VertexConsumer vertices = ctx.consumers().getBuffer(SHADOW_LAYER);
                for (BlockPos blockPos : BlockPos.iterate(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2))) {
                    //EntityRenderDispatcher.renderShadowPart(entry, vertexConsumer, world, blockPos, mobEntity2, d, e, f, opacity);
                    //MinecraftClient.getInstance().getBlockRenderManager().renderBlock(Blocks.ACACIA_BUTTON.getDefaultState(), blockPos, ctx.world(), ctx.matrixStack(), BUFFER_BUILDER, false, MinecraftClient.getInstance().world.getRandom());
                    renderSprayPart(BUFFER_BUILDER, ctx.world(), blockPos, entry, sprayPos.getX(), sprayPos.getY(), sprayPos.getZ());
                }
            }
            else if (spray.getFace() == Direction.NORTH) {
                int x1 = MathHelper.floor(sprayPos.getX() - RADIUS);
                int x2 = MathHelper.floor(sprayPos.getX() + RADIUS);
                int y1 = MathHelper.floor(sprayPos.getY() - RADIUS);
                //this is to make it placeable on low blocks
                int y2 = MathHelper.ceil(sprayPos.getY() + RADIUS);
                int z1 = MathHelper.floor(sprayPos.getZ() - RADIUS);
                int z2 = MathHelper.floor(sprayPos.getZ() + RADIUS);
                MatrixStack.Entry entry = ctx.matrixStack().peek();

                MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.BUBBLE.getType(), sprayPos.getX(), sprayPos.getY(), sprayPos.getZ(), 0, 0, 0);

                //VertexConsumer vertices = ctx.consumers().getBuffer(SHADOW_LAYER);
                for (BlockPos blockPos : BlockPos.iterate(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2))) {
                    //EntityRenderDispatcher.renderShadowPart(entry, vertexConsumer, world, blockPos, mobEntity2, d, e, f, opacity);
                    //MinecraftClient.getInstance().getBlockRenderManager().renderBlock(Blocks.ACACIA_BUTTON.getDefaultState(), blockPos, ctx.world(), ctx.matrixStack(), BUFFER_BUILDER, false, MinecraftClient.getInstance().world.getRandom());renderSprayPart(BUFFER_BUILDER, ctx.world(), blockPos, entry, sprayPos.getX(), sprayPos.getY(), sprayPos.getZ());
                    renderSprayPartNorth(BUFFER_BUILDER, ctx.world(), blockPos, entry, sprayPos.getX(), sprayPos.getY(), sprayPos.getZ());
                }
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

    private static void renderSprayPartNorth(BufferBuilder builder, WorldView world, BlockPos pos, MatrixStack.Entry matrixEntry, double x, double y, double z) {
        BlockPos blockPos = pos.south();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.getRenderType() == BlockRenderType.INVISIBLE) {
            return;
        }

        VoxelShape voxelShape = blockState.getOutlineShape(world, pos);
        if (voxelShape.isEmpty()) {
            return;
        }

        final float radius = 0.5f;

        Box box = voxelShape.getBoundingBox();
        double minX = (double)pos.getX() + box.minX;
        double maxX = (double)pos.getX() + box.maxX;
        double minY = (double)pos.getY() + box.minY;
        double maxY = (double)pos.getY() + box.maxY;
        double minZ = (double)pos.getZ() + box.minZ;
        double maxZ = (double)pos.getZ() + box.maxZ;

        //MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.END_ROD.getType(), minX, minY, minZ, 0, 0, 0);
        //MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.END_ROD.getType(), maxX, maxY, maxZ, 0, 0, 0);



        float x1 = (float)(minX - x);
        float x2 = (float)(maxX - x);
        float y1 = (float)(minY - y);
        float y2 = (float)(maxY - y);
        float z1 = (float)(minZ - z);
        float z2 = (float)(maxZ - z);

        //MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.SOUL.getType(), x1 + x, y1 + y, z1 + z, 0, 0, 0);
        //MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.SOUL.getType(), x2 + x, y2 + y, z2 + z, 0, 0, 0);

        final float scale = 2.0f;
        final float offset = 0.5f;

        //TODO: Make this work for stairs

        float o = -x1 / scale / radius + offset;
        float p = -x2 / scale / radius + offset;
        float q = -y1 / scale / radius + offset;
        float r = -y2 / scale / radius + offset;



        //System.out.println(MinecraftClient.getInstance().getNetworkHandler().getConnection().getAddress());

        builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, q).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x1, y2, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, r).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x2, y2, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, r).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, q).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
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


        Box box = voxelShape.getBoundingBox();
        double minX = (double)pos.getX() + box.minX;
        double maxX = (double)pos.getX() + box.maxX;
        double minY = (double)pos.getY() + box.minY;
        double minZ = (double)pos.getZ() + box.minZ;
        double maxZ = (double)pos.getZ() + box.maxZ;

        float x1 = (float)(minX - x);
        float x2 = (float)(maxX - x);
        float y1 = (float)(minY - y) - (1.0f - (float)box.maxY);
        float z1 = (float)(minZ - z);
        float z2 = (float)(maxZ - z);

        final float scale = 2.0f;
        final float offset = 0.5f;

        //TODO: Make this work for stairs

        float o = -x1 / scale / RADIUS + offset;
        float p = -x2 / scale / RADIUS + offset;
        float q = -z1 / scale / RADIUS + offset;
        float r = -z2 / scale / RADIUS + offset;

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

            o = -x1 / scale / RADIUS + offset;
            p = -x2 / scale / RADIUS + offset;
            q = -z1 / scale / RADIUS + offset;
            r = -z2 / scale / RADIUS + offset;

            builder.vertex(matrixEntry.getPositionMatrix(), x1, y1 - 0.5f, z1 + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, q - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            builder.vertex(matrixEntry.getPositionMatrix(), x1, y1 - 0.5f, z2 + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, r - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            builder.vertex(matrixEntry.getPositionMatrix(), x2, y1 - 0.5f, z2 + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, r - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
            builder.vertex(matrixEntry.getPositionMatrix(), x2, y1 - 0.5f, z1 + 0.5f).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, q - 0.5f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();

        }



        //System.out.println(MinecraftClient.getInstance().getNetworkHandler().getConnection().getAddress());

        builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, q).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x1, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(o, r).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z2).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, r).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
        builder.vertex(matrixEntry.getPositionMatrix(), x2, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f).texture(p, q).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal( 0, 1.0f, 0).next();
    }
}
