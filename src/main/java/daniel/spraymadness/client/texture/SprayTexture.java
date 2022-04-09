package daniel.spraymadness.client.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.*;
import java.util.Locale;

public class SprayTexture extends AbstractTexture {

    private Identifier identifier;
    private String path;
    private String title;
    private final boolean emissive;
    private final boolean fromPack;

    public SprayTexture(File source, boolean emissive) {
        this.emissive = emissive;
        this.fromPack = false;
        try {
            if (!source.exists()) return;

            InputStream inputStream = new FileInputStream(source);
            NativeImage texture = NativeImage.read(inputStream);
            path = source.getPath();

            String id = Util.replaceInvalidChars(source.getName(), Identifier::isPathCharacterValid);

            this.identifier = new Identifier(SprayMadness.MOD_ID, id);
            this.title = StringUtils.capitalize(id);

            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> {
                    TextureUtil.prepareImage(this.getGlId(), texture.getWidth(), texture.getHeight());
                    this.upload(texture);
                });
            } else {
                TextureUtil.prepareImage(this.getGlId(), texture.getWidth(), texture.getHeight());
                this.upload(texture);
            }

            MinecraftClient.getInstance().getTextureManager().registerTexture(this.identifier, this);

            texture.close();
            inputStream.close();
        } catch (IOException e) {
            SprayMadness.LOGGER.error("Couldn't load spray texture " + source.getPath());
            this.identifier = MissingSprite.getMissingSpriteId();
        }
    }

    public SprayTexture(File source, boolean emissive, String title) {
        this(source, emissive);
        this.title = title;
    }

    public SprayTexture(Identifier identifier, boolean emissive, String title) {
        this.identifier = identifier;
        this.emissive = emissive;
        this.fromPack = true;
        this.title = title;
    }

    private void upload(NativeImage texture) {
        if (texture != null) {
            this.bindTexture();
            texture.upload(0, 0, 0, false);
        } else {
            SprayMadness.LOGGER.warn("Trying to upload disposed texture {}", this.getGlId());
        }
    }

    @Override
    public void load(ResourceManager manager) throws IOException {

    }

    public boolean isEmissive() {
        return emissive;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public String getTitle() {
        return title;
    }

    public boolean isFromPack() {
        return fromPack;
    }

    public String getPath() {
        return isFromPack() ? identifier.getPath() : path;
    }
}
