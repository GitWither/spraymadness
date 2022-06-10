package daniel.spraymadness.client.resource;

import com.google.gson.*;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.SprayStorage;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.core.jackson.JsonConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class SprayReloadListener implements SimpleSynchronousResourceReloadListener {
    private final SprayStorage storage;
    public SprayReloadListener(SprayStorage storage) {
        this.storage = storage;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(SprayMadness.MOD_ID, "sprays");
    }

    @Override
    public void reload(ResourceManager manager) {
        storage.loadedTextures.removeIf(SprayTexture::isFromPack);

        for (Map.Entry<Identifier, Resource> entry : manager.findResources("", path -> Objects.equals(path.getPath(), "sprays.json")).entrySet()) {
            try (InputStream stream = entry.getValue().getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                JsonObject json = JsonHelper.deserialize(reader);
                if (json == null) continue;
                if (!JsonHelper.hasArray(json, "sprays")) continue;

                JsonArray sprays = json.getAsJsonArray("sprays");

                for (JsonElement element : sprays) {
                    //if (!(element instanceof JsonObject object)) continue;

                    Identifier id;
                    if (JsonHelper.isString(element)) {
                        id = Identifier.tryParse(element.getAsString());
                        if (id == null) continue;
                        if (storage.removedPackTextures.stream().anyMatch(textureId -> textureId.equals(id))) continue;
                        if (storage.loadedTextures.stream().anyMatch(sprayTexture -> sprayTexture.getIdentifier() == id)) continue;

                        storage.loadedTextures.add(new SprayTexture(id, false, id.getPath()));
                        continue;
                    }

                    JsonObject spray = element.getAsJsonObject();

                    if (!spray.has("source")) continue;

                    id = Identifier.tryParse(spray.get("source").getAsString());
                    if (id == null) continue;
                    if (storage.removedPackTextures.stream().anyMatch(textureId -> textureId.equals(id))) continue;
                    if (storage.loadedTextures.stream().anyMatch(sprayTexture -> sprayTexture.getIdentifier() == id)) continue;

                    boolean emissive = false;
                    if (spray.has("emissive")) {
                        emissive = spray.get("emissive").getAsBoolean();
                    }

                    String title = id.getPath();
                    if (spray.has("title")) {
                        title = spray.get("title").getAsString();
                    }

                    SprayTexture texture = new SprayTexture(id, emissive, title);

                    storage.loadedTextures.add(texture);
                }
                //System.out.println(json.getAsJsonArray().get(0));
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
}
