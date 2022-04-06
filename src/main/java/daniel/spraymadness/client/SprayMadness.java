package daniel.spraymadness.client;

import daniel.spraymadness.client.io.SprayIOManager;
import daniel.spraymadness.client.render.SprayRenderer;
import daniel.spraymadness.client.screen.SprayWheelScreen;
import daniel.spraymadness.client.util.Spray;
import daniel.spraymadness.client.util.SprayStorage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class SprayMadness implements ClientModInitializer {
    public static final String MOD_ID = "spray_madness";
    public static final String NAME = "Spray Madness";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static KeyBinding SPAWN_SPRAY_KEYBIND;
    public static KeyBinding SPRAY_WHEEL_KEYBIND;
    public static KeyBinding DELETE_SPRAY_KEYBIND;

    private Shader sprayShader;

    private SprayIOManager sprayIOManager;
    private SprayRenderer sprayRenderer;
    private SprayStorage storage;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing!");

        storage = SprayStorage.getInstance();
        sprayIOManager = new SprayIOManager(storage);

        //TODO: Change category to proper name
        SPAWN_SPRAY_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spray_madness.spray",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.spray_madness"
        ));

        SPRAY_WHEEL_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
           "key.spray_madness.spray_wheel",
           InputUtil.Type.KEYSYM,
           GLFW.GLFW_KEY_Y,
           "category.spray_madness"
        ));

        DELETE_SPRAY_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spray_madness.delete_spray",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.spray_madness"
        ));


        ClientPlayConnectionEvents.JOIN.register(sprayIOManager::loadSprays);
        ClientPlayConnectionEvents.DISCONNECT.register(sprayIOManager::saveSprays);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (SPRAY_WHEEL_KEYBIND.isPressed() && !(client.currentScreen instanceof SprayWheelScreen)) {
                client.setScreen(new SprayWheelScreen(storage));
            }

            while (DELETE_SPRAY_KEYBIND.wasPressed()) {
                HitResult hit = client.crosshairTarget;
                if (hit != null) {
                    if (hit.getType() == HitResult.Type.BLOCK) {//kind of a naive approach but cant really think of anything smarter
                        storage.totalWorldSprays.removeIf(spray ->
                                hit.getPos().isInRange(new Vec3d(spray.getPos().getX(), spray.getPos().getY(), spray.getPos().getZ()), 0.2));
                    }
                }
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            sprayIOManager.loadSprayTextures(client);
            sprayShader = sprayIOManager.loadSprayShader(client);
            sprayRenderer = new SprayRenderer(storage, sprayShader);

            WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(sprayRenderer::renderSprays);
        });
    }
}
