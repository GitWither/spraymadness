package daniel.spraymadness.client;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.render.WorldRenderingCallbacks;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.Spray;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.nbt.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class SprayMadness implements ClientModInitializer {

    public static List<Spray> totalSprays = new ArrayList<>();
    public static List<SprayTexture> sprayTextures = new ArrayList<>();

    public static final String MOD_ID = "spray_madness";
    public static final String NAME = "Spray Madness";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static Shader SPRAY_SHADER;

    private static KeyBinding spawnSprayKeybinding;



    public static Shader getSprayShader() {
        return SPRAY_SHADER;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing!");


        spawnSprayKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spray_madness.spray",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "test"
        ));

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(WorldRenderingCallbacks::renderSprays);

        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
            LOGGER.info("QUITTING");
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (spawnSprayKeybinding.wasPressed()) {
                HitResult hit = client.crosshairTarget;
                if (hit != null) {
                    switch (hit.getType()) {
                        case MISS:
                            break;
                        case BLOCK:
                            Spray spray = new Spray(sprayTextures.get(0), new Vec3f((float)hit.getPos().x, (float)hit.getPos().y, (float)hit.getPos().z), ((BlockHitResult)hit).getSide());
                            client.player.sendMessage(new LiteralText("Spray added "), false);
                            totalSprays.add(spray);
                    }
                }
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                SPRAY_SHADER = new Shader(MinecraftClient.getInstance().getResourceManager(), "spray", VertexFormats.POSITION_COLOR_TEXTURE);
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                File spraysFile = new File(client.runDirectory, "sprays.dat");

                if (spraysFile.exists()) {
                    NbtCompound sprays = NbtIo.read(new File(MinecraftClient.getInstance().runDirectory, "sprays.dat"));
                    if (sprays != null) {

                        NbtList spraysList = sprays.getList("spray_textures", NbtElement.COMPOUND_TYPE);

                        for (NbtElement spray : spraysList) {
                            if (spray instanceof NbtCompound sprayCompound) {
                                if (sprayCompound.contains("source", NbtElement.STRING_TYPE))
                                {
                                    String source = sprayCompound.getString("source");

                                    SprayTexture newSprayTexture = new SprayTexture(new File(sprayCompound.getString("source")));
                                    if (newSprayTexture.getTexture() != null) {
                                        sprayTextures.add(newSprayTexture);
                                    }
                                }
                            }
                        }

                        LOGGER.info(sprays.getSize());
                    }
                }
                else {
                    NbtCompound compound = new NbtCompound();
                    NbtList sprays = new NbtList();

                    compound.put("sprays", sprays);

                    NbtIo.write(compound, spraysFile);
                }
            }
            catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });
    }
}
