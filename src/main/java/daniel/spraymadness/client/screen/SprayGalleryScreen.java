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

        this.bottom = this.height - 64;
        this.top = 32;

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
                        this::toggleDeleteMode,
                        (button, matrices, mouseX, mouseY) -> SprayGalleryScreen.this.renderTooltip(matrices, ADD_SPRAY_TOOLTIP, mouseX, mouseY),
                        LiteralText.EMPTY
                )
        );
    }

    private void toggleDeleteMode(ButtonWidget button) {
        deleting = !deleting;
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        DrawableHelper.drawCenteredText(matrices, this.textRenderer, title, titleX, titleY, 0xFFFFFFFF);

        DrawHelper.drawOptionsGradient(top, bottom, 0, this.width, getScrollAmount());

        super.render(matrices, mouseX, mouseY, delta);


        matrices.push();
        matrices.translate(galleryX, galleryY, 0);
        for (int i = 0; i < SprayMadness.sprayTextures.size(); i++) {
            SprayTexture texture = SprayMadness.sprayTextures.get(i);

            final int relativeOffset = i / MAX_TEXTURES_PER_ROW;

            final int xPos = i * TEXTURE_OFFSET - (relativeOffset * TEXTURE_OFFSET * MAX_TEXTURES_PER_ROW);
            final int yPos = relativeOffset * TEXTURE_OFFSET;

            RenderSystem.setShaderTexture(0, texture.getIdentifier());
            DrawHelper.drawSprayTexture(matrices, texture, xPos, yPos, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            final int xPosAdjusted = xPos + galleryX;
            final int yPosAdjusted = yPos + galleryY;

            if (mouseX >= xPosAdjusted && mouseY >= yPosAdjusted && mouseX <= xPosAdjusted + TEXTURE_WIDTH && mouseY <= yPosAdjusted + TEXTURE_HEIGHT) {
                currentSprayTextureIndex = i;
                DrawableHelper.fill(matrices, xPos, yPos, xPos + TEXTURE_WIDTH, yPos + TEXTURE_HEIGHT, deleting ? DELETE_SELECTION_COLOR : SprayWheelScreen.SELECTION_COLOR );
            }
            else currentSprayTextureIndex = -1;
            DrawableHelper.drawCenteredText(matrices, this.textRenderer, new LiteralText(texture.getTitle()), xPos + TEXTURE_WIDTH / 2, yPos + TEXTURE_HEIGHT, WHITE);
        }
        matrices.pop();
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
