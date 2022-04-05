package daniel.spraymadness.client.util;

import daniel.spraymadness.client.texture.SprayTexture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SprayStorage {
    private static SprayStorage instance;

    private final List<Spray> totalWorldSprays = new ArrayList<>();
    private final List<SprayTexture> loadedTextures = new ArrayList<>();
    private final List<SprayTexture> sprayWheelTextures = new ArrayList<>(8);

    public static SprayStorage getInstance() {
        if (instance == null) {
            instance = new SprayStorage();
        }
        return instance;
    }

    //TOTAL TEXTURES
    public void addTexture(SprayTexture texture) {
        loadedTextures.add(texture);
    }
    public List<SprayTexture> getLoadedTextures() {
        return loadedTextures;
    }

    public SprayTexture getLoadedTexture(int index) {
        return loadedTextures.get(index);
    }

    public int getLoadedTextureSize() {
        return loadedTextures.size();
    }

    public void removeLoadedTexture(int index) {
        loadedTextures.remove(index);
    }

    //SPRAY WHEEL
    public void addTextureToSprayWheel(SprayTexture texture) {
        sprayWheelTextures.add(texture);
    }

    public void removeSprayWheelTexture(SprayTexture texture) {
        sprayWheelTextures.remove(texture);
    }

    public SprayTexture getSprayWheelTexture(int index) {
        return sprayWheelTextures.get(index);
    }

    public boolean sprayWheelContainsTexture(SprayTexture texture) {
        return sprayWheelTextures.contains(texture);
    }

    public int getSprayWheelTextureSize() {
        return sprayWheelTextures.size();
    }


    //WORLD SPRAYS
    public int getWorldSpraySize() {
        return totalWorldSprays.size();
    }

    public void addWorldSpray(Spray spray) {
        totalWorldSprays.add(spray);
    }

    public void clearWorldSprays() {
        totalWorldSprays.clear();
    }

    public List<Spray> getTotalWorldSprays() {
        return this.totalWorldSprays;
    }
}
