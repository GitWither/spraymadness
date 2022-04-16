package daniel.spraymadness.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.util.Spray;
import daniel.spraymadness.client.util.SprayStorage;
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
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11C.*;

public class SprayRenderer
{
    private final MinecraftClient client;
    private final SprayStorage storage;
    private final Shader sprayShader;
    private final BufferBuilder bufferBuilder;
    private static final float RADIUS = 0.5f;
    private static final float SCALE = 2.0f;
    private static final float OFFSET = 0.5f;

    public SprayRenderer(MinecraftClient client, SprayStorage storage, Shader sprayShader) {
        this.storage = storage;
        this.sprayShader = sprayShader;
        this.client = client;
        this.bufferBuilder = new BufferBuilder(256);
    }

    private Shader getSprayShader() {
        return sprayShader;
    }
    
    public boolean renderSprays(WorldRenderContext ctx, HitResult ctx2) {
        ctx.matrixStack().push();
        ctx.matrixStack().translate(-ctx.camera().getPos().x, -ctx.camera().getPos().y, -ctx.camera().getPos().z);

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL_LEQUAL);
        RenderSystem.disableCull();

        this.client.gameRenderer.getLightmapTextureManager().enable();
        RenderSystem.setShader(this::getSprayShader);

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.scale(0.99975586f, 0.99975586f, 0.99975586f);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        Identifier dimensionId = ctx.world().getRegistryKey().getValue();
        for (Spray spray : storage.totalWorldSprays) {
            if (!spray.getDimension().equals(dimensionId)) continue;

            this.bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
            RenderSystem.setShaderTexture(0, spray.getTextureIdentifier());

            ctx.matrixStack().push();
            ctx.matrixStack().translate(spray.getPos().getX(), spray.getPos().getY(), spray.getPos().getZ());

            MatrixStack.Entry entry = ctx.matrixStack().peek();

            Vec3f sprayPos = spray.getPos();

            Vec3f facingVector = spray.getFace().getOpposite().getUnitVector();
            double factorX = -0.3 * facingVector.getX();
            double factorY = -0.3 * facingVector.getY();
            double factorZ = -0.3 * facingVector.getZ();

            int x1 = MathHelper.floor(sprayPos.getX() - RADIUS - 0.4 + factorX);
            int x2 = MathHelper.floor(sprayPos.getX() + RADIUS + 0.4 + factorX);
            int y1 = MathHelper.floor(sprayPos.getY() - RADIUS - 0.4 + factorY);
            int y2 = MathHelper.floor(sprayPos.getY() + RADIUS + 0.4 + factorY);
            int z1 = MathHelper.floor(sprayPos.getZ() - RADIUS - 0.4 + factorZ);
            int z2 = MathHelper.floor(sprayPos.getZ() + RADIUS + 0.4 + factorZ);

            BlockPos blockPos1 = new BlockPos(x1, y1, z1);
            BlockPos blockPos2 = new BlockPos(x2, y2, z2);

            for (BlockPos blockPos : BlockPos.iterate(blockPos1, blockPos2)) {
                if (!Block.shouldDrawSide(ctx.world().getBlockState(blockPos), ctx.world(), blockPos, spray.getFace(), blockPos)) continue;

                int light = spray.isEmissive() ? LightmapTextureManager.MAX_LIGHT_COORDINATE : WorldRenderer.getLightmapCoordinates(ctx.world(), blockPos);

                //could probably pass the spray directly at this point but ¯\_(ツ)_/¯
                renderSprayPart(this.bufferBuilder, ctx.world(), blockPos, entry, spray.getFace(), facingVector, sprayPos.getX(), sprayPos.getY(), sprayPos.getZ(), light, spray.getFacing());
            }


            ctx.matrixStack().pop();
            this.bufferBuilder.end();

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            BufferRenderer.draw(this.bufferBuilder);
        }
        this.client.gameRenderer.getLightmapTextureManager().disable();

        MatrixStack matrixStack2 = RenderSystem.getModelViewStack();
        matrixStack2.pop();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        ctx.matrixStack().pop();

        return true;
    }

    private void renderSprayPartVertical(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int factor, int light, int facing) {
        float x1 = minX - x;
        float x2 = maxX - x;
        float y1 = (factor > 0 ? minY : maxY) - y + factor;
        float z1 = minZ - z;
        float z2 = maxZ - z;

        float deltaX = x2 - x1;
        float deltaZ = z2 - z1;

        Direction dir = Direction.fromHorizontal(facing);
        Vec3f unit = dir.getUnitVector();


        if (dir.getAxis() == Direction.Axis.Z) {
            float factorZ = unit.getZ();

            float u1 = factorZ * -x1 / SCALE / RADIUS + OFFSET;
            float v1 = factorZ * -x2 / SCALE / RADIUS + OFFSET;
            float u2 = factorZ * -z1 / SCALE / RADIUS + OFFSET;
            float v2 = factorZ * -z2 / SCALE / RADIUS + OFFSET;

            DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x2, y1, z2, u1, v1, u2, v2, light, false);
        }
        else {
            float factorX = unit.getX();

            float u1 = factorX * z1 / SCALE / RADIUS + OFFSET;
            float v1 = factorX * z2 / SCALE / RADIUS + OFFSET;
            float u2 = factorX * -x2 / SCALE / RADIUS + OFFSET;
            float v2 = factorX * -x1 / SCALE / RADIUS + OFFSET;

            DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x2, y1, z2, u1, v1, u2, v2, light, true);
        }

    }

    private void renderSprayPartNorthSouth(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int factor, int light) {
        float x1 = minX - x;
        float x2 = maxX - x;
        float y1 = minY - y;
        float y2 = maxY - y;
        float z1 = (factor > 0 ? minZ : maxZ) - z + factor;

        float u1 = -factor * x1 / SCALE / RADIUS + OFFSET;
        float v1 = -factor * x2 / SCALE / RADIUS + OFFSET;
        float u2 = -y1 / SCALE / RADIUS + OFFSET;
        float v2 = -y2 / SCALE / RADIUS + OFFSET;

        DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x2, y2, z1, u1, v1, u2, v2, light);
    }

    private void renderSprayPartEastWest(BufferBuilder builder, MatrixStack.Entry matrixEntry, float x, float y, float z, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int factor, int light) {
        float x1 = (factor > 0 ? minX : maxX) - x + factor;
        float y1 = minY - y;
        float y2 = maxY - y;
        float z1 = minZ - z;
        float z2 = maxZ - z;

        float u1 = factor * z1 / SCALE / RADIUS + OFFSET;
        float v1 = factor * z2 / SCALE / RADIUS + OFFSET;
        float u2 = -y1 / SCALE / RADIUS + OFFSET;
        float v2 = -y2 / SCALE / RADIUS + OFFSET;

        DrawHelper.drawSprayTextureQuad(builder, matrixEntry.getPositionMatrix(), x1, y1, z1, x1, y2, z2, u1, v1, u2, v2, light);

    }

    private void renderSprayPart(BufferBuilder builder, WorldView world, BlockPos pos, MatrixStack.Entry matrixEntry, Direction direction, Vec3f directionUnitVector, float x, float y, float z, int light, int facing) {
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);


        if (blockState.getRenderType() == BlockRenderType.INVISIBLE) {
            return;
        }

        VoxelShape voxelShape = blockState.getOutlineShape(world, blockPos);
        if (voxelShape.isEmpty()) {
            return;
        }

        voxelShape.forEachBox((boxMinX, boxMinY, boxMinZ, boxMaxX, boxMaxY, boxMaxZ) -> {
            float minX  = (float) (pos.getX() + boxMinX);
            float maxX = (float) (pos.getX() + boxMaxX);
            float minY = (float) (pos.getY() + boxMinY);
            float maxY = (float) (pos.getY() + boxMaxY);
            float minZ = (float) (pos.getZ() + boxMinZ);
            float maxZ = (float) (pos.getZ() + boxMaxZ);

            if (direction.getAxis() == Direction.Axis.Y) {
                renderSprayPartVertical(builder, matrixEntry, x, y, z, minX, minY, minZ, maxX, maxY, maxZ, (int) directionUnitVector.getY(), light, facing);
            }
            if (direction.getAxis() == Direction.Axis.Z) {
                renderSprayPartNorthSouth(builder, matrixEntry, x, y, z, minX, minY, minZ, maxX, maxY, maxZ, (int) directionUnitVector.getZ(), light);
            }
            if (direction.getAxis() == Direction.Axis.X) {
                renderSprayPartEastWest(builder, matrixEntry, x, y, z, minX, minY, minZ, maxX, maxY, maxZ, (int) directionUnitVector.getX(), light);
            }
        });
    }

}
