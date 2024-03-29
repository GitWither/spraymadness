package daniel.spraymadness.client.util;

import daniel.spraymadness.client.texture.SprayTexture;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SprayStorage {
    private static SprayStorage instance;

    public final List<Spray> totalWorldSprays = new LinkedList<>();
    public final List<SprayTexture> loadedTextures = new ArrayList<>();
    public final List<SprayTexture> sprayWheelTextures = new ArrayList<>(8);
    public final List<Identifier> removedPackTextures = new ArrayList<>();

    public static SprayStorage getInstance() {
        if (instance == null) {
            instance = new SprayStorage();
        }
        return instance;
    }
}
