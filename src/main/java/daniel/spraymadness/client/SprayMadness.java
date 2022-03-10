package daniel.spraymadness.client;

import daniel.spraymadness.client.render.WorldRenderingCallbacks;
import daniel.spraymadness.client.screen.SprayWheelScreen;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.Spray;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.nbt.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class SprayMadness implements ClientModInitializer {
    public static final boolean SIMPLIFIED_RENDERING = true;

    public static List<Spray> totalSprays = new ArrayList<>();
    public static List<SprayTexture> sprayTextures = new ArrayList<>();

    public static final String MOD_ID = "spray_madness";
    public static final String NAME = "Spray Madness";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static Shader SPRAY_SHADER;

    public static KeyBinding SPAWN_SPRAY_KEYBIND;
    public static KeyBinding SPRAY_WHEEL_KEYBIND;

    public static Shader getSprayShader() {
        return SPRAY_SHADER;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing!");


        //TODO: Change category to proper name
        SPAWN_SPRAY_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spray_madness.spray",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "test"
        ));

        SPRAY_WHEEL_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
           "key.spray_madness.spray_wheel",
           InputUtil.Type.KEYSYM,
           GLFW.GLFW_KEY_Y,
           "test"
        ));

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(WorldRenderingCallbacks::renderSprays);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            //TODO: load sprays
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            //TODO: Save sprays
            totalSprays.clear();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
            LOGGER.info("QUITTING");
        });


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (SPRAY_WHEEL_KEYBIND.isPressed() && !(client.currentScreen instanceof SprayWheelScreen)) {
                client.setScreen(new SprayWheelScreen());
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
