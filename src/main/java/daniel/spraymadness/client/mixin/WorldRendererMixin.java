package daniel.spraymadness.client.mixin;

import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.util.Spray;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render",  at = @At(value = "CONSTANT", args = "stringValue=destroyProgress", ordinal = 0))
    public void renderSprays(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo cb) {

        for (Spray spray :
                SprayMadness.totalSprays) {
            matrices.push();

            matrices.translate(0, 0, 2);
            VertexConsumerProvider.Immediate consumerProvider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer consumer = consumerProvider.getBuffer(RenderLayer.getLines());

            //Matrix3f normal = ctx.matrixStack().peek().getNormalMatrix();

            //MinecraftClient.getInstance().gameRenderer.getMapRenderer().draw(ctx.matrixStack(), ctx.consumers(), 0, state, false, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            // MinecraftClient.getInstance().getItemRenderer().renderItem(Items.BONE_BLOCK.getDefaultStack(), ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND, LightmapTextureManager.MAX_LIGHT_COORDINATE, 1, ctx.matrixStack(), ctx.consumers(), 1);
            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                    Blocks.DIAMOND_BLOCK.getDefaultState(),
                    matrices, consumerProvider, 15728880, OverlayTexture.DEFAULT_UV);
            //consumer.vertex( 0, 0, 0).color(1, 1, 1, 1).texture(0, 0).light(1, 1).normal(normal, 1, 1, 1).next();
            //consumer.vertex(0, 1, 0).color(1, 1, 1, 1).texture(0, 1).light(1, 1).normal(normal, 1, 1, 1).next();
            consumer.vertex(1, 1, 0).color(1, 1, 1, 1).next();
            consumer.vertex(1, 0, 0).color(1, 1, 1, 1).next();
            matrices.pop();
        }
    }
}
