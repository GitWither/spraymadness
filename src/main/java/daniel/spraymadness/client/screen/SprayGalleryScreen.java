package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.gui.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

public class SprayGalleryScreen extends Screen {
    private static final int WHITE = (255 << 16) + (255 << 8) + 255;
    private static final int MAX_TEXTURES_PER_ROW = 4;
    private static final int TEXTURE_OFFSET = 80;
    private static final int GALLERY_OFFSET = 100;

    private static final TranslatableText ADD_SPRAY_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.add_spray.tooltip");
    private static final TranslatableText DELETE_SPRAY_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.delete_spray.tooltip");
    private static final OrderedText TITLE = OrderedText.styledForwardsVisitedString(I18n.translate("gui.spray_madness.spray_gallery.title"), Style.EMPTY.withUnderline(true));

    protected static final Identifier SPRAY_WHEEL = new Identifier(SprayMadness.MOD_ID, "textures/gui/spray_wheel.png");

    private AddSprayScreen addSprayScreen;

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
    private boolean addingSpray;

    private int currentSprayTextureIndex;


    public SprayGalleryScreen() {
        super(Text.of(""));
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
                        this.width / 2 - 10 + 50 - GALLERY_OFFSET, this.height / 2 + 50,
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
                        this.width / 2 - 10 - 50 - GALLERY_OFFSET, this.height / 2 + 50,
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

        if (addingSpray) {
            toggleChildrenVisibility(false);
            addSprayScreen.init(client, width, height);
        }
    }

    private void deleteCurrentSpray(ButtonWidget button) {
        SprayMadness.sprayTextures.remove(currentSprayTextureIndex);
    }

    private void showSelectDialog(ButtonWidget buttonWidget) {
        if (client == null) return;

        this.toggleChildrenVisibility(false);

        addSprayScreen = new AddSprayScreen(this::sprayAdded);
        addSprayScreen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        addingSpray = true;
    }

    private void sprayAdded(boolean b) {
        addingSpray = false;
        toggleChildrenVisibility(true);
    }

    private void toggleChildrenVisibility(boolean visible) {
        for (Element child: this.children()) {
            if (child instanceof ClickableWidget widget) {
                widget.active = visible;
                widget.visible = visible;
            }
        }
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

        DrawableHelper.drawCenteredTextWithShadow(matrices, this.textRenderer, TITLE, this.titleX, this.titleY, WHITE);

        int currentSprayCount = SprayMadness.sprayTextures.size();

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, SPRAY_WHEEL);
        this.drawTexture(matrices, (this.width - 170) / 2 + GALLERY_OFFSET, (this.height - 170) / 2 - 10, 0, 0, 170, 170);
        RenderSystem.disableBlend();

        DrawableHelper.drawCenteredText(matrices, this.textRenderer, new LiteralText(currentSprayCount > 0 ? (currentSprayTextureIndex + 1 + "/" + currentSprayCount) : "No sprays!"), this.width / 2 - GALLERY_OFFSET, this.height / 2 + 56, WHITE);

        if (currentSprayTextureIndex > -1 && currentSprayTextureIndex < currentSprayCount) {
            SprayTexture texture = SprayMadness.sprayTextures.get(currentSprayTextureIndex);
            RenderSystem.setShaderTexture(0, texture.getIdentifier());
            DrawHelper.drawSprayTexture(matrices, texture, this.width / 2 - TEXTURE_WIDTH - GALLERY_OFFSET, this.height / 2 - TEXTURE_HEIGHT, TEXTURE_WIDTH * 2, TEXTURE_HEIGHT * 2);
            DrawableHelper.drawCenteredText(matrices, this.textRenderer, new LiteralText(texture.getTitle()), this.width / 2 - GALLERY_OFFSET, this.height / 2 - 60, WHITE);

            if (currentSprayTextureIndex > 0) {
                texture = SprayMadness.sprayTextures.get(currentSprayTextureIndex - 1);
                RenderSystem.setShaderTexture(0, texture.getIdentifier());
                DrawHelper.drawSprayTexture(matrices, texture, this.width / 2 - 80 - GALLERY_OFFSET, this.height / 2 - 20, TEXTURE_WIDTH - 10, TEXTURE_HEIGHT - 10, 0.5f);
            }

            if (currentSprayTextureIndex + 1 < SprayMadness.sprayTextures.size()) {
                texture = SprayMadness.sprayTextures.get(currentSprayTextureIndex + 1);
                RenderSystem.setShaderTexture(0, texture.getIdentifier());
                DrawHelper.drawSprayTexture(matrices, texture, this.width / 2 + 50 - GALLERY_OFFSET, this.height / 2 - TEXTURE_HEIGHT / 2, TEXTURE_WIDTH - 10, TEXTURE_HEIGHT - 10, 0.5f);

            }
        }

        if (addingSpray) {
            matrices.push();
            matrices.translate(0, 0, 1);
            addSprayScreen.render(matrices, mouseX, mouseY, delta);
            matrices.pop();
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println(currentSprayTextureIndex);
        if (deleting && currentSprayTextureIndex >= 0 && button == 1) {
            SprayMadness.sprayTextures.remove(currentSprayTextureIndex);
            currentSprayTextureIndex = -1;
        }
        if (addingSpray) {
            addSprayScreen.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (addingSpray) {
            addSprayScreen.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (addingSpray) {
            addSprayScreen.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (addingSpray) {
            addSprayScreen.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }


    private int getScrollAmount() {
        return 5;
    }
}
