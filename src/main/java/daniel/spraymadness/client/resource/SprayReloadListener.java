package daniel.spraymadness.client.resource;

import com.google.gson.*;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.SprayStorage;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.core.jackson.JsonConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SprayReloadListener implements SimpleSynchronousResourceReloadListener {
    public SprayReloadListener() {

    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(SprayMadness.MOD_ID, "sprays");
    }

    @Override
    public void reload(ResourceManager manager) {

        for (Identifier identifier : manager.findResources("", path -> path.equals("sprays.json"))) {
            try (InputStream stream = manager.getResource(identifier).getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                JsonObject json = JsonHelper.deserialize(reader);
                if (json == null) continue;
                if (!JsonHelper.hasArray(json, "sprays")) continue;

                JsonArray sprays = json.getAsJsonArray("sprays");

                for (JsonElement element : sprays) {
                    //if (!(element instanceof JsonObject object)) continue;

                    if (JsonHelper.isString(element)) {
                        System.out.println(element);
                        continue;
                        //storage.loadedTextures.add(new SprayTexture(Identifier.tryParse(element.getAsString()), false));
                    }
                    JsonObject spray = element.getAsJsonObject();
                    System.out.println(spray.get("source"));
                    //if (JsonHelper.hasString(object, "source") && JsonHelper.hasBoolean(object, "emissive")) {
                        //storage.loadedTextures.add(new SprayTexture(
                                //Identifier.tryParse(JsonHelper.getString(object, "source")),
                                //JsonHelper.getBoolean(object, "emissive"))
                        //);
                    //}
                }
                //System.out.println(json.getAsJsonArray().get(0));
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
}
