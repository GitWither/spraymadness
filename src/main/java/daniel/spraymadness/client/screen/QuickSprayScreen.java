package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.Objects;

public class QuickSprayScreen extends Screen {
    protected static final Identifier TEXTURE = new Identifier(SprayMadness.MOD_ID, "textures/gui/spray_wheel.png");
    //TODO: Change this
    private static final Text TITLE = new TranslatableText("gui.spray_madness.spray_wheel.title");

    private int x;
    private int y;

    private int titleX;
    private int titleY;

    private final int backgroundWidth = 128;
    private final int backgroundHeight = 128;


    private static final HashMap<Integer, Integer> HEIGHT_MAPPING = new HashMap<>() {{

        put(0, 60);
        put(1, 85);
        put(2, 85);
        put(3, 60);
        put(4, 35);
        put(5, 35);
    }};

    public QuickSprayScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(TITLE)) / 2 + this.x;
        this.titleY = this.y - 10;

        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(MatrixStack matrices, int vOffset) {
        super.renderBackground(matrices, vOffset);
    }

    @Override
    public void tick() {
        super.tick();

        if (!SprayMadness.SPRAY_WHEEL_KEYBIND.isPressed()) {
            this.onClose();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        this.textRenderer.drawWithShadow(matrices, TITLE, this.titleX, this.titleY, (255 << 16) + (255 << 8) + 255);

        //RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DstFactor.SRC_ALPHA);
        RenderSystem.enableBlend();
        //RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.drawTexture(matrices, this.x, this.y, 64, 64, 128, 128);
        RenderSystem.disableBlend();

        SprayTexture texture1 = SprayMadness.sprayTextures.get(0);
        RenderSystem.setShaderTexture(0, texture1.getIdentifier());
        matrices.push();
        matrices.translate((this.width - texture1.getWidth()) / 2f + 190, 115f, 0);
        matrices.scale(0.1f, 0.1f, 1f);
        this.drawTexture(matrices, 0, 0, 0, 0,  texture1.getWidth() / 2, texture1.getHeight() / 2);
        matrices.pop();

        matrices.push();
        matrices.translate((this.width - texture1.getWidth()) / 2f + 225, 80f, 0);
        matrices.scale(0.1f, 0.1f, 1f);
        this.drawTexture(matrices, 0, 0, 0, 0,  texture1.getWidth() / 2, texture1.getHeight() / 2);
        matrices.pop();

        matrices.push();
        matrices.translate((this.width - texture1.getWidth()) / 2f + 265, 80f, 0);
        matrices.scale(0.1f, 0.1f, 1f);
        this.drawTexture(matrices, 0, 0, 0, 0,  texture1.getWidth() / 2, texture1.getHeight() / 2);
        matrices.pop();

        matrices.push();
        matrices.translate((this.width - texture1.getWidth()) / 2f + 300, 115f, 0);
        matrices.scale(0.1f, 0.1f, 1f);
        this.drawTexture(matrices, 0, 0, 0, 0,  texture1.getWidth() / 2, texture1.getHeight() / 2);
        matrices.pop();


        /*
        for (int i = 0; i < 7; i++) {
            if (i < SprayMadness.sprayTextures.size()) {
                matrices.push();
                int x = ((Objects.equals(HEIGHT_MAPPING.get(i), HEIGHT_MAPPING.getOrDefault( i - 1, 0))) ? i : i - 1) * 300;
                int y = HEIGHT_MAPPING.get(i) + 250;

                matrices.scale(0.1f, 0.1f, 0.1f);

                RenderSystem.setShaderTexture(0, SprayMadness.sprayTextures.get(i).getIdentifier());
                this.drawTexture(matrices, x, y, 0, 0,  SprayMadness.sprayTextures.get(i).getWidth() / 2, SprayMadness.sprayTextures.get(i).getHeight() / 2);

                matrices.pop();
            }
        }
         */
    }
}
