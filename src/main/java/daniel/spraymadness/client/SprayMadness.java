package daniel.spraymadness.client;

import daniel.spraymadness.client.io.SprayIOManager;
import daniel.spraymadness.client.render.SprayRenderer;
import daniel.spraymadness.client.resource.SprayReloadListener;
import daniel.spraymadness.client.screen.SprayGalleryScreen;
import daniel.spraymadness.client.screen.SprayWheelScreen;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.Spray;
import daniel.spraymadness.client.util.SprayStorage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class SprayMadness implements ClientModInitializer {
    public static final String MOD_ID = "spray_madness";
    public static final String NAME = "Spray Madness";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static SoundEvent ENTITY_PLAYER_SPRAY = new SoundEvent(new Identifier(MOD_ID, "entity.player.spray"));

    private static KeyBinding SPRAY_GALLERY_KEYBIND;
    public static KeyBinding SPRAY_WHEEL_KEYBIND;
    public static KeyBinding DELETE_SPRAY_KEYBIND;

    private static final KeyBinding[] SPRAY_WHEEL_KEYBINDS = new KeyBinding[8];

    private Shader sprayShader;

    private SprayIOManager sprayIOManager;
    private SprayRenderer sprayRenderer;
    private SprayStorage storage;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing!");

        storage = SprayStorage.getInstance();
        sprayIOManager = new SprayIOManager(storage);

        Registry.register(Registry.SOUND_EVENT, ENTITY_PLAYER_SPRAY.getId(), ENTITY_PLAYER_SPRAY);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SprayReloadListener(storage));

        SPRAY_WHEEL_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
           "key.spray_madness.spray_wheel",
           InputUtil.Type.KEYSYM,
           GLFW.GLFW_KEY_Y,
           "category.spray_madness"
        ));

        DELETE_SPRAY_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spray_madness.delete_spray",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.spray_madness"
        ));

        SPRAY_GALLERY_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spray_madness.spray_gallery",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.spray_madness"
        ));

        for (int i = 0; i < SPRAY_WHEEL_KEYBINDS.length; i++) {
            SPRAY_WHEEL_KEYBINDS[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.spray_madness.spray_wheel_" + i,
                    InputUtil.Type.KEYSYM,
                    InputUtil.UNKNOWN_KEY.getCode(),
                    "category.spray_madness"
            ));
        }


        ClientPlayConnectionEvents.JOIN.register(sprayIOManager::loadSprays);
        ClientPlayConnectionEvents.DISCONNECT.register(sprayIOManager::saveSprays);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (SPRAY_WHEEL_KEYBIND.isPressed() && !(client.currentScreen instanceof SprayWheelScreen)) {
                client.setScreen(new SprayWheelScreen(storage));
            }

            while (DELETE_SPRAY_KEYBIND.wasPressed()) {
                HitResult hit = client.crosshairTarget;
                if (hit != null) {
                    if (hit.getType() == HitResult.Type.BLOCK) {//kind of a naive approach
                        storage.totalWorldSprays.removeIf(spray ->
                                hit.getPos().isInRange(new Vec3d(spray.getPos().getX(), spray.getPos().getY(), spray.getPos().getZ()), 0.2));
                    }
                }
            }

            while (SPRAY_GALLERY_KEYBIND.wasPressed()) {
                client.setScreen(new SprayGalleryScreen(storage));
            }

            for (int i = 0; i < SPRAY_WHEEL_KEYBINDS.length; i++) {
                while (SPRAY_WHEEL_KEYBINDS[i].wasPressed()) {
                    HitResult hit = client.crosshairTarget;
                    if (hit != null) {
                        if (hit.getType() == HitResult.Type.BLOCK) {
                            Spray spray = new Spray(
                                    storage.sprayWheelTextures.get(i),
                                    new Vec3f((float) hit.getPos().x, (float) hit.getPos().y, (float) hit.getPos().z),
                                    ((BlockHitResult) hit).getSide(),
                                    client.player.world.getRegistryKey().getValue(),
                                    client.player.getHorizontalFacing().getHorizontal()
                            );
                            client.world.playSound(((BlockHitResult) hit).getBlockPos(), SprayMadness.ENTITY_PLAYER_SPRAY, SoundCategory.PLAYERS, 5, 1, true);
                            storage.totalWorldSprays.add(spray);
                        }
                    }
                }
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            sprayIOManager.loadSprayTextures(client);
            sprayShader = sprayIOManager.loadSprayShader(client);
            sprayRenderer = new SprayRenderer(client, storage, sprayShader);


            WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(sprayRenderer::renderSprays);
        });
    }
}
