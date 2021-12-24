package daniel.spraymadness.client.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.*;

public class SprayTexture extends AbstractTexture {

    private NativeImage texture;
    private Identifier identifier;

    public SprayTexture(File source) {
        try {
            if (!source.exists()) return;

            InputStream inputStream = new FileInputStream(source);
            NativeImage nativeImage = NativeImage.read(inputStream);
            nativeImage.mirrorVertically();
            texture = nativeImage;

            this.identifier = new Identifier(SprayMadness.MOD_ID, source.getName());

            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> {
                    TextureUtil.prepareImage(this.getGlId(), this.getWidth(), this.getHeight());
                    this.upload();
                });
            } else {
                TextureUtil.prepareImage(this.getGlId(), this.getWidth(), this.getHeight());
                this.upload();
            }

            MinecraftClient.getInstance().getTextureManager().registerTexture(this.identifier, this);
        } catch (IOException e) {
            SprayMadness.LOGGER.error("Couldn't load spray texture " + source.getPath());
        }
    }

    public void upload() {
        if (this.texture != null) {
            this.bindTexture();
            this.texture.upload(0, 0, 0, false);
        } else {
            SprayMadness.LOGGER.warn("Trying to upload disposed texture {}", this.getGlId());
        }
    }

    @Override
    public void load(ResourceManager manager) throws IOException {

    }

    public NativeImage getTexture() {
        return texture;
    }

    public int getWidth() {
        return texture.getWidth();
    }

    public int getHeight() {
        return texture.getHeight();
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }
}
