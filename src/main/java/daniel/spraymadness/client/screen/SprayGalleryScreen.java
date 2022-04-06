package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.Colors;
import daniel.spraymadness.client.util.Spray;
import daniel.spraymadness.client.util.SprayStorage;
import daniel.spraymadness.client.util.gui.AddToWheelButtonWidget;
import daniel.spraymadness.client.util.gui.DrawHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.*;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SprayGalleryScreen extends Screen {
    private static final int MAX_TEXTURES_PER_ROW = 4;
    private static final int TEXTURE_OFFSET = 80;
    private static final int GALLERY_OFFSET = 100;

    private static final Identifier WIDGETS = new Identifier(SprayMadness.MOD_ID, "textures/gui/widgets.png");

    private static final TranslatableText TIP_LABEL = new TranslatableText("gui.spray_madness.spray_gallery.tip");
    private static final TranslatableText ADD_SPRAY_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.add_spray.tooltip");
    private static final TranslatableText DELETE_SPRAY_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.delete_spray.tooltip");
    private static final OrderedText TITLE = OrderedText.styledForwardsVisitedString(I18n.translate("gui.spray_madness.spray_gallery.title"), Style.EMPTY.withUnderline(true));

    private SprayStorage sprayStorage;
    private AddSprayScreen addSprayScreen;

    private AddToWheelButtonWidget addToWheelButton;

    private static final int TEXTURE_WIDTH = 40;
    private static final int TEXTURE_HEIGHT = 40;

    private int titleX;
    private int titleY;

    private int bottom;
    private int top;

    private boolean deleting;
    private boolean addingSpray;

    private int currentSprayTextureIndex;


    public SprayGalleryScreen(SprayStorage storage) {
        super(Text.of(""));
        this.sprayStorage = storage;
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = this.width / 2;
        this.titleY = 15;

        this.bottom = this.height - 75;
        this.top = 55;

        this.addDrawableChild(
                new ButtonWidget(
                        this.width / 2 - 10 + 50 - GALLERY_OFFSET, this.height / 2 + 50,
                        20, 20,
                        new LiteralText(">"),
                        button -> {
                            if (currentSprayTextureIndex + 1 < sprayStorage.loadedTextures.size()) {
                                currentSprayTextureIndex++;
                            }

                            updateAddToWheelButtonMessage();
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

                            updateAddToWheelButtonMessage();
                        }
                )
        );

        this.addDrawableChild(
                new TexturedButtonWidget(
                        this.width / 2 - 10 - 50 - GALLERY_OFFSET, this.height / 4 + 135,
                        20, 20,
                        0, 0, 20,
                        WIDGETS,
                        256, 256,
                        this::showSelectDialog,
                        (button, matrices, mouseX, mouseY) -> SprayGalleryScreen.this.renderTooltip(matrices, ADD_SPRAY_TOOLTIP, mouseX, mouseY),
                        LiteralText.EMPTY
                )
        );

        this.addDrawableChild(
                new TexturedButtonWidget(
                        this.width / 2 - 10 + 50 - GALLERY_OFFSET, this.height / 4 + 135,
                        20, 20,
                        40, 0, 20,
                        WIDGETS,
                        256, 256,
                        this::deleteCurrentSpray,
                        (button, matrices, mouseX, mouseY) -> SprayGalleryScreen.this.renderTooltip(matrices, DELETE_SPRAY_TOOLTIP, mouseX, mouseY),
                        LiteralText.EMPTY
                )
        );

        addToWheelButton = this.addDrawableChild(
                new AddToWheelButtonWidget(this.width / 2 - GALLERY_OFFSET - 30, this.height / 4 + 135, 60, 20, new TranslatableText("add_to_wheel"), button -> {
                    SprayTexture texture = sprayStorage.loadedTextures.get(currentSprayTextureIndex);

                    if (sprayStorage.sprayWheelTextures.size() > 7) {
                        ((AddToWheelButtonWidget)button).setFull();
                    }
                    if (sprayStorage.sprayWheelTextures.contains(texture)) {
                        sprayStorage.sprayWheelTextures.remove(texture);
                        ((AddToWheelButtonWidget)button).setAddMessage();
                        button.active = true;
                    }
                    else {
                        sprayStorage.sprayWheelTextures.add(sprayStorage.loadedTextures.get(currentSprayTextureIndex));
                        ((AddToWheelButtonWidget)button).setRemove();
                    }
                }, (button, matrices, mouseX, mouseY) -> {
                    SprayGalleryScreen.this.renderTooltip(matrices, ((AddToWheelButtonWidget)button).getTooltipMessage(), mouseX, mouseY);
                })
        );

        updateAddToWheelButtonMessage();

        if (addingSpray) {
            toggleChildrenVisibility(false);
            addSprayScreen.init(client, width, height);
        }
    }

    private void updateAddToWheelButtonMessage() {
        if (sprayStorage.loadedTextures.size() == 0) return;

        SprayTexture texture = sprayStorage.loadedTextures.get(currentSprayTextureIndex);
        if (sprayStorage.sprayWheelTextures.contains(texture)) {
            addToWheelButton.setRemove();
        }
        else if (sprayStorage.sprayWheelTextures.size() > 7) {
            addToWheelButton.setFull();
        }
        else {
            addToWheelButton.setAddMessage();
        }
    }

    private void deleteCurrentSpray(ButtonWidget button) {
        if (sprayStorage.loadedTextures.size() == 0) return;

        sprayStorage.loadedTextures.remove(currentSprayTextureIndex);

        if (currentSprayTextureIndex > 0) {
            currentSprayTextureIndex--;
        }
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

    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        DrawHelper.drawOptionsGradient(top, bottom, 0, this.width, getScrollAmount());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        super.render(matrices, mouseX, mouseY, delta);

        int test = (125 << 24) + (255 << 16) + (255 << 8) + 255;
        DrawableHelper.fill(matrices, this.width / 2, this.top, this.width / 2 + 1, this.bottom, test);

        DrawableHelper.drawCenteredTextWithShadow(matrices, this.textRenderer, TITLE, this.titleX, this.titleY, Colors.WHITE);
        DrawableHelper.drawCenteredTextWithShadow(matrices, this.textRenderer, TIP_LABEL.asOrderedText(), this.titleX, this.titleY + 220, Colors.WHITE);

        int currentSprayCount = sprayStorage.loadedTextures.size();


        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, SprayWheelScreen.TEXTURE);
        this.drawTexture(matrices, (this.width - 170) / 2 + GALLERY_OFFSET, (this.height - 170) / 2 - 10, 0, 0, 170, 170);
        RenderSystem.disableBlend();

        this.textRenderer.drawTrimmed(StringVisitable.styled("Spray Wheel", Style.EMPTY.withBold(true)), (this.width + 166) / 2, (this.height - 40) / 2, 35, Colors.WHITE);

        DrawableHelper.drawCenteredText(matrices, this.textRenderer, new LiteralText(currentSprayCount > 0 ? (currentSprayTextureIndex + 1 + "/" + currentSprayCount) : "No sprays!"), this.width / 2 - GALLERY_OFFSET, this.height / 2 + 56, Colors.WHITE);

        if (currentSprayTextureIndex > -1 && currentSprayTextureIndex < currentSprayCount) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            SprayTexture texture = sprayStorage.loadedTextures.get(currentSprayTextureIndex);
            String path = texture.getPath();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShaderTexture(0, texture.getIdentifier());

            int x1 = this.width / 2 - TEXTURE_WIDTH - GALLERY_OFFSET;
            int y1 = this.height / 2 - TEXTURE_HEIGHT;

            int x2 = x1 + TEXTURE_WIDTH * 2;
            int y2 = y1 + TEXTURE_WIDTH * 2;

            DrawHelper.drawSprayTexture(matrices, texture, x1, y1, TEXTURE_WIDTH * 2, TEXTURE_HEIGHT * 2);
            DrawableHelper.drawCenteredText(matrices, this.textRenderer, new LiteralText(texture.getTitle()), this.width / 2 - GALLERY_OFFSET, this.height / 2 - 60, Colors.WHITE);

            if (currentSprayTextureIndex > 0) {
                texture = sprayStorage.loadedTextures.get(currentSprayTextureIndex - 1);
                RenderSystem.setShaderTexture(0, texture.getIdentifier());
                //RenderSystem.setShaderColor(1.0f, 3.0f, 1.0f, 0.0f);
                DrawHelper.drawSprayTexture(matrices, texture, this.width / 2 - 80 - GALLERY_OFFSET, this.height / 2 - 20, TEXTURE_WIDTH - 10, TEXTURE_HEIGHT - 10, 0.5f);
            }

            if (currentSprayTextureIndex + 1 < sprayStorage.loadedTextures.size()) {
                texture = sprayStorage.loadedTextures.get(currentSprayTextureIndex + 1);
                RenderSystem.setShaderTexture(0, texture.getIdentifier());
                DrawHelper.drawSprayTexture(matrices, texture, this.width / 2 + 50 - GALLERY_OFFSET, this.height / 2 - TEXTURE_HEIGHT / 2, TEXTURE_WIDTH - 10, TEXTURE_HEIGHT - 10, 0.5f);

            }

            if (mouseX > x1 && mouseY > y1 && mouseX < x2 && mouseY < y2) {
                this.renderTooltip(matrices, new LiteralText(path), mouseX, mouseY);
            }

        }


        matrices.push();
        matrices.translate((this.width - 101) / 2f + GALLERY_OFFSET, (this.height - 123) / 2f, 0);
        int x = 0;
        int y = 0;
        for (int i = 0; i < sprayStorage.sprayWheelTextures.size(); i++) {
            if (i > 7) break;

            SprayTexture texture = sprayStorage.sprayWheelTextures.get(i);

            RenderSystem.setShaderTexture(0, texture.getIdentifier());
            DrawHelper.drawSprayTexture(matrices, texture, x * SprayWheelScreen.SPRAY_SPACING, y * SprayWheelScreen.SPRAY_SPACING + y, SprayWheelScreen.SPRAY_TEXTURE_WIDTH, SprayWheelScreen.SPRAY_TEXTURE_HEIGHT);

            if (i >= 2 && i < 4) {
                x++;
            }
            if (i == 6) {
                x--;
            }
            if (i <= 1) {
                y++;
            }
            else if (i >= 4 && i <= 5) {
                y--;
            }
        }
        matrices.pop();

        if (addingSpray) {
            matrices.push();
            matrices.translate(0, 0, 1);
            addSprayScreen.render(matrices, mouseX, mouseY, delta);
            matrices.pop();
        }

    }

    @Override
    public void filesDragged(List<Path> paths) {
        super.filesDragged(paths);

        for (Path path : paths) {
            if (FilenameUtils.isExtension(path.getFileName().toString(), "png")) {
                sprayStorage.loadedTextures.add(new SprayTexture(path.toFile()));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

    @Override
    public void onClose() {

        try {
            File spraysFile = new File(client.runDirectory, "sprays.dat");

            if (spraysFile.exists()) {
                NbtCompound sprays = NbtIo.read(new File(client.runDirectory, "sprays.dat"));

                if (sprays == null) {
                    sprays = new NbtCompound();
                }

                NbtList sprayTextures = sprays.getList("spray_textures", NbtElement.COMPOUND_TYPE);
                NbtList sprayWheelTextures = sprays.getList("spray_wheel", NbtElement.INT_TYPE);

                sprayTextures.clear();
                sprayWheelTextures.clear();

                for (SprayTexture spray : sprayStorage.loadedTextures) {
                    NbtCompound sprayNbt = new NbtCompound();

                    sprayNbt.put("source", NbtString.of(spray.getPath()));

                    sprayTextures.add(sprayNbt);

                    if (sprayStorage.sprayWheelTextures.contains(spray)) {
                        sprayWheelTextures.add(NbtInt.of(sprayStorage.loadedTextures.indexOf(spray)));
                    }
                }

                sprays.put("spray_textures", sprayTextures);
                sprays.put("spray_wheel", sprayWheelTextures);

                NbtIo.write(sprays, spraysFile);
            }
        }
        catch (IOException e) {
            SprayMadness.LOGGER.error(e.getMessage());
        }

        super.onClose();
    }
}
