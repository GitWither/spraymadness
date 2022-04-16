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
    Identifier dimension;
    SprayTexture texture;
    int facing;

    public Spray(SprayTexture texture, Vec3f pos, Direction face, Identifier dimension) {
        this.texture = texture;
        this.pos = pos;
        this.face = face;
        this.dimension = dimension;
    }

    public Spray(SprayTexture texture, Vec3f pos, Direction face, Identifier dimension, int facing) {
        this.texture = texture;
        this.pos = pos;
        this.face = face;
        this.dimension = dimension;
        this.facing = facing;
    }

    public int getFacing() {
        return facing;
    }

    public Direction getFace() {
        return face;
    }

    public Vec3f getPos() {
        return pos;
    }

    public boolean isEmissive() {
        return texture.isEmissive();
    }

    public Identifier getDimension() {return dimension;}

    public Identifier getTextureIdentifier() {
        return this.texture.getIdentifier();
    }

    public AbstractTexture getTexture() {
        return this.texture;
    }
}
