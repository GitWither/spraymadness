package daniel.spraymadness.client.util;

import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

import java.io.*;

public class Spray {
    Vec3f pos;
    Direction face;

    SprayTexture texture;

    public Spray(SprayTexture texture, Vec3f pos, Direction face) {
        this.texture = texture;
        this.pos = pos;
        this.face = face;
    }

    public Direction getFace() {
        return face;
    }

    public Vec3f getPos() {
        return pos;
    }

    public Identifier getTextureIdentifier() {
        return this.texture.getIdentifier();
    }

    public AbstractTexture getTexture() {
        return this.texture;
    }
}
