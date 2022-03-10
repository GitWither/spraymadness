package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.gui.DrawHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class SprayGalleryScreen extends Screen {
    private static final int WHITE = (255 << 16) + (255 << 8) + 255;
    private static final int MAX_TEXTURES_PER_ROW = 4;
    private static final int TEXTURE_OFFSET = 80;
    private static final TranslatableText ADD_SPRAY_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.add_spray.tooltip");
    private static final TranslatableText DELETE_SPRAY_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.delete_spray.tooltip");

    //TODO: Move this to a shared constants class
    public static final int DELETE_SELECTION_COLOR = (122 << 24) + (255 << 16) + (71 << 8) + 71;

    private static final int TEXTURE_WIDTH = 40;
    private static final int TEXTURE_HEIGHT = 40;

    private int galleryX;
    private int galleryY;

    private int titleX;
    private int titleY;

    private int bottom;
    private int top;

    private boolean deleting;

    private int currentSprayTextureIndex;

    private static final Text TITLE = new TranslatableText("gui.spray_madness.spray_gallery.title");

    public SprayGalleryScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = this.width / 2;
        this.titleY = 15;

        this.galleryX = (int) (this.width / 2f - (TEXTURE_OFFSET * MAX_TEXTURES_PER_ROW - TEXTURE_WIDTH) / 2f);
        this.galleryY = 50;

        this.bottom = this.height - 75;
        this.top = 55;

        this.addDrawableChild(
                new ButtonWidget(
                        this.width / 2 - 10 + 50, this.height / 2 + 50,
                        20, 20,
                        new LiteralText(">"),
                        button -> {
                            if (currentSprayTextureIndex + 1 < SprayMadness.sprayTextures.size()) {
                                currentSprayTextureIndex++;
                            }
                        }
                )
        );

        this.addDrawableChild(
                new ButtonWidget(
                        this.width / 2 - 10 - 50, this.height / 2 + 50,
                        20, 20,
                        new LiteralText("<"),
                        button -> {
                            if (currentSprayTextureIndex > 0) {
                                currentSprayTextureIndex--;
                            }
                        }
                )
        );

        this.addDrawableChild(
                new TexturedButtonWidget(
                        this.width / 2 - 10, this.height / 4 + 132,
                        20, 20,
                        0, 106, 20,
                        ButtonWidget.WIDGETS_TEXTURE,
                        256, 256,
                        this::showSelectDialog,
                        (button, matrices, mouseX, mouseY) -> SprayGalleryScreen.this.renderTooltip(matrices, ADD_SPRAY_TOOLTIP, mouseX, mouseY),
                        LiteralText.EMPTY
                )
        );

        this.addDrawableChild(
                new TexturedButtonWidget(
                        this.width / 2 - 50, this.height / 4 + 132,
                        20, 20,
                        0, 106, 20,
                        ButtonWidget.WIDGETS_TEXTURE,
                        256, 256,
                        this::deleteCurrentSpray,
                        (button, matrices, mouseX, mouseY) -> SprayGalleryScreen.this.renderTooltip(matrices, DELETE_SPRAY_TOOLTIP, mouseX, mouseY),
                        LiteralText.EMPTY
                )
        );
    }

    private void deleteCurrentSpray(ButtonWidget button) {
        SprayMadness.sprayTextures.remove(currentSprayTextureIndex);
    }

    private void showSelectDialog(ButtonWidget buttonWidget) {
        if (client == null) return;

        //TODO: Change constructor to not accept text
        this.client.setScreen(new AddSprayScreen(new LiteralText("temp")));
    }

    protected int getScrollbarPositionX() {
        return this.width / 2 + 124;
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        DrawHelper.drawOptionsGradient(top, bottom, 0, this.width, getScrollAmount());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);


        super.render(matrices, mouseX, mouseY, delta);

        DrawableHelper.drawCenteredText(matrices, this.textRenderer, new LiteralText(currentSprayTextureIndex + 1 + "/" + SprayMadness.sprayTextures.size()), this.width / 2, this.height / 2 + 56, WHITE);
        if (currentSprayTextureIndex > -1 && currentSprayTextureIndex < SprayMadness.sprayTextures.size()) {
            SprayTexture texture = SprayMadness.sprayTextures.get(currentSprayTextureIndex);
            RenderSystem.setShaderTexture(0, texture.getIdentifier());
            DrawHelper.drawSprayTexture(matrices, texture, this.width / 2 - TEXTURE_WIDTH, this.height / 2 - TEXTURE_HEIGHT, TEXTURE_WIDTH * 2, TEXTURE_HEIGHT * 2);
            DrawableHelper.drawCenteredText(matrices, this.textRenderer, new LiteralText(texture.getTitle()), this.width / 2, this.height / 2 - 60, WHITE);

            if (currentSprayTextureIndex > 0) {
                texture = SprayMadness.sprayTextures.get(currentSprayTextureIndex - 1);
                RenderSystem.setShaderTexture(0, texture.getIdentifier());
                DrawHelper.drawSprayTexture(matrices, texture, this.width / 2 - 80, this.height / 2 - 20, TEXTURE_WIDTH - 10, TEXTURE_HEIGHT - 10, 0.5f);
            }

            if (currentSprayTextureIndex + 1 < SprayMadness.sprayTextures.size()) {
                texture = SprayMadness.sprayTextures.get(currentSprayTextureIndex + 1);
                RenderSystem.setShaderTexture(0, texture.getIdentifier());
                DrawHelper.drawSprayTexture(matrices, texture, this.width / 2 + 50, this.height / 2 - TEXTURE_HEIGHT / 2, TEXTURE_WIDTH - 10, TEXTURE_HEIGHT - 10, 0.5f);

            }
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println(currentSprayTextureIndex);
        if (deleting && currentSprayTextureIndex >= 0 && button == 1) {
            SprayMadness.sprayTextures.remove(currentSprayTextureIndex);
            currentSprayTextureIndex = -1;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getScrollAmount() {
        return 5;
    }
}
