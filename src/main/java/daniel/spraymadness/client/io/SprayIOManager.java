package daniel.spraymadness.client.io;

import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.render.SprayRenderer;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.Spray;
import daniel.spraymadness.client.util.SprayStorage;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.nbt.*;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

import java.io.File;
import java.io.IOException;

public class SprayIOManager {
    private String currentWorldKey;
    private SprayStorage storage;

    public SprayIOManager(SprayStorage storage) {
        this.storage = storage;
    }
    
    private static String getWorldKey(ClientPlayNetworkHandler handler, MinecraftClient client) {
        IntegratedServer server = client.getServer();
        if (server != null) {
            return server.getSavePath(WorldSavePath.ROOT).normalize().getFileName().toString();
        }
        return handler.getConnection().getAddress().toString();
    }

    public void loadSprays(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        currentWorldKey = getWorldKey(handler, client);

        try {
            File spraysFile = new File(client.runDirectory, "sprays.dat");

            if (spraysFile.exists()) {
                NbtCompound sprays = NbtIo.read(new File(client.runDirectory, "sprays.dat"));

                if (sprays == null) return;

                NbtCompound worldSprays = sprays.getCompound("world_sprays");
                if (worldSprays == null) return;

                NbtList currentWorldSprays = worldSprays.getList(currentWorldKey, NbtElement.COMPOUND_TYPE);
                if (currentWorldSprays != null) {
                    for (NbtElement sprayNbt : currentWorldSprays) {
                        if (sprayNbt instanceof NbtCompound sprayCompound) {
                            float x = sprayCompound.getFloat("x");
                            float y = sprayCompound.getFloat("y");
                            float z = sprayCompound.getFloat("z");

                            Direction face = Direction.byId(sprayCompound.getInt("face_id"));

                            Identifier dimension = Identifier.tryParse(sprayCompound.getString("dimension"));
                            Identifier textureId = Identifier.tryParse(sprayCompound.getString("texture"));

                            SprayTexture sprayTexture = storage.loadedTextures.stream().filter(texture -> texture.getIdentifier().equals(textureId)).findFirst().orElse(null);

                            storage.totalWorldSprays.add(new Spray(sprayTexture, new Vec3f(x, y, z), face, dimension));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSprays(ClientPlayNetworkHandler handler, MinecraftClient client) {
        if (storage.totalWorldSprays.size() == 0) return;

        try {
            File spraysFile = new File(client.runDirectory, "sprays.dat");

            if (spraysFile.exists()) {
                NbtCompound sprays = NbtIo.read(new File(client.runDirectory, "sprays.dat"));

                if (sprays == null) {
                    sprays = new NbtCompound();
                }

                NbtCompound worldSprays = sprays.getCompound("world_sprays");



                NbtList currentWorldSprays = worldSprays.getList(currentWorldKey, NbtElement.COMPOUND_TYPE);

                currentWorldSprays.clear();

                for (Spray spray : storage.totalWorldSprays) {
                    NbtCompound sprayNbt = new NbtCompound();

                    sprayNbt.put("x", NbtFloat.of(spray.getPos().getX()));
                    sprayNbt.put("y", NbtFloat.of(spray.getPos().getY()));
                    sprayNbt.put("z", NbtFloat.of(spray.getPos().getZ()));

                    sprayNbt.put("face_id", NbtInt.of(spray.getFace().getId()));
                    sprayNbt.put("dimension", NbtString.of(spray.getDimension().toString()));
                    sprayNbt.put("texture", NbtString.of(spray.getTextureIdentifier().toString()));

                    currentWorldSprays.add(sprayNbt);
                }

                storage.totalWorldSprays.clear();

                worldSprays.put(currentWorldKey, currentWorldSprays);
                sprays.put("world_sprays", worldSprays);

                NbtIo.write(sprays, spraysFile);
            }
        }
        catch (IOException e) {
            SprayMadness.LOGGER.error(e.getMessage());
        }
        storage.totalWorldSprays.clear();
    }

    public void loadSprayTextures(MinecraftClient client) {

        try {
            File spraysFile = new File(client.runDirectory, "sprays.dat");

            if (spraysFile.exists()) {
                NbtCompound sprays = NbtIo.read(new File(client.runDirectory, "sprays.dat"));
                if (sprays != null) {

                    NbtList spraysList = sprays.getList("spray_textures", NbtElement.COMPOUND_TYPE);

                    for (NbtElement spray : spraysList) {
                        if (spray instanceof NbtCompound sprayCompound) {
                            if (sprayCompound.contains("source", NbtElement.STRING_TYPE))
                            {
                                String source = sprayCompound.getString("source");
                                boolean emissive = sprayCompound.getBoolean("emissive");
                                boolean fromPack = sprayCompound.getBoolean("from_pack");
                                String title = sprayCompound.getString("title");

                                SprayTexture newSprayTexture = fromPack ?
                                        new SprayTexture(Identifier.tryParse(source), emissive, title) :
                                        new SprayTexture(new File(source), emissive, title);
                                storage.loadedTextures.add(newSprayTexture);
                            }
                        }
                    }

                    NbtList sprayWheelList = sprays.getList("spray_wheel", NbtElement.INT_TYPE);
                    for (NbtElement sprayIndex : sprayWheelList) {
                        if (storage.loadedTextures.size() == 0) break;
                        if (sprayIndex instanceof NbtInt spray) {
                            int value = spray.intValue();

                            if (storage.loadedTextures.size() <= value) continue;
                            storage.sprayWheelTextures.add(storage.loadedTextures.get(value));
                        }
                    }
                }
            }
            else {
                NbtIo.write(new NbtCompound(), spraysFile);
            }
        }
        catch (IOException e) {
            SprayMadness.LOGGER.error(e.getMessage());
        }
    }

    public Shader loadSprayShader(MinecraftClient client) {
        try {
            return new Shader(client.getResourceManager(), "spray", VertexFormats.POSITION_COLOR_TEXTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
