package daniel.spraymadness.client.io;

import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.Spray;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.minecraft.client.ClientGameSession;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.MinecraftClientGame;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.nbt.*;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.File;
import java.io.IOException;

public class SprayIOCallbacks {
    private static String CURRENT_WORLD_KEY;

    private static String getWorldKey(ClientPlayNetworkHandler handler, MinecraftClient client) {
        IntegratedServer server = client.getServer();
        if (server != null) {
            return server.getSavePath(WorldSavePath.ROOT).normalize().getFileName().toString();
        }
        return handler.getConnection().getAddress().toString();
    }

    public static void loadSprays(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        CURRENT_WORLD_KEY = getWorldKey(handler, client);

        try {
            File spraysFile = new File(client.runDirectory, "sprays.dat");

            if (spraysFile.exists()) {
                NbtCompound sprays = NbtIo.read(new File(client.runDirectory, "sprays.dat"));

                if (sprays == null) return;

                //System.out.println(handler.getConnection().);

                NbtCompound worldSprays = sprays.getCompound("world_sprays");
                if (worldSprays == null) return;

                NbtList currentWorldSprays = worldSprays.getList(CURRENT_WORLD_KEY, NbtElement.COMPOUND_TYPE);
                if (currentWorldSprays != null) {
                    for (NbtElement sprayNbt : currentWorldSprays) {
                        if (sprayNbt instanceof NbtCompound sprayCompound) {
                            float x = sprayCompound.getFloat("x");
                            float y = sprayCompound.getFloat("y");
                            float z = sprayCompound.getFloat("z");

                            Direction face = Direction.byId(sprayCompound.getInt("face_id"));

                            Identifier dimension = Identifier.tryParse(sprayCompound.getString("dimension"));
                            Identifier textureId = Identifier.tryParse(sprayCompound.getString("texture"));

                            SprayTexture texture = (SprayTexture) client.getTextureManager().getOrDefault(textureId, MissingSprite.getMissingSpriteTexture());

                            SprayMadness.totalSprays.add(new Spray(texture, new Vec3f(x, y, z), face, dimension));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSprays(ClientPlayNetworkHandler handler, MinecraftClient client) {
        if (SprayMadness.totalSprays.size() == 0) return;

        try {
            File spraysFile = new File(client.runDirectory, "sprays.dat");

            if (spraysFile.exists()) {
                NbtCompound sprays = NbtIo.read(new File(client.runDirectory, "sprays.dat"));

                if (sprays == null) {
                    sprays = new NbtCompound();
                }

                NbtCompound worldSprays = sprays.getCompound("world_sprays");



                NbtList currentWorldSprays = worldSprays.getList(CURRENT_WORLD_KEY, NbtElement.COMPOUND_TYPE);

                currentWorldSprays.clear();

                for (Spray spray : SprayMadness.totalSprays) {
                    NbtCompound sprayNbt = new NbtCompound();

                    sprayNbt.put("x", NbtFloat.of(spray.getPos().getX()));
                    sprayNbt.put("y", NbtFloat.of(spray.getPos().getY()));
                    sprayNbt.put("z", NbtFloat.of(spray.getPos().getZ()));

                    sprayNbt.put("face_id", NbtInt.of(spray.getFace().getId()));
                    sprayNbt.put("dimension", NbtString.of(spray.getDimension().toString()));
                    sprayNbt.put("texture", NbtString.of(spray.getTextureIdentifier().toString()));

                    currentWorldSprays.add(sprayNbt);
                }

                SprayMadness.totalSprays.clear();

                worldSprays.put(CURRENT_WORLD_KEY, currentWorldSprays);
                sprays.put("world_sprays", worldSprays);

                NbtIo.write(sprays, spraysFile);
            }
        }
        catch (IOException e) {
            SprayMadness.LOGGER.error(e.getMessage());
        }
        SprayMadness.totalSprays.clear();
    }
}
