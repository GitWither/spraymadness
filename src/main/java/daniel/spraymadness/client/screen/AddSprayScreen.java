package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.SprayStorage;
import daniel.spraymadness.client.util.gui.LabelledTextFieldWidget;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.io.File;

public class AddSprayScreen extends Screen {
    private static final Identifier BACKGROUND = new Identifier("textures/gui/demo_background.png");

    private static final Text GLOWS_LABEL = new TranslatableText("gui.spray_madness.spray_gallery.add_spray.emissive");

    private static final int BACKGROUND_WIDTH = 248;
    private static final int BACKGROUND_HEIGHT = 166;

    private int x;
    private int y;

    private boolean shouldRenderFileWarning = false;

    private final BooleanConsumer callback;
    private final SprayStorage storage;

    private LabelledTextFieldWidget sprayTitle;
    private LabelledTextFieldWidget sprayPath;
    private CheckboxWidget emissive;

    private String path;

    protected AddSprayScreen(BooleanConsumer callback, SprayStorage storage) {
        super(Text.of("Add Spray"));
        this.callback = callback;
        this.storage = storage;
    }

    @Override
    protected void init() {
        this.x = (this.width - BACKGROUND_WIDTH) / 2;
        this.y = (this.height - BACKGROUND_HEIGHT) / 2;

        sprayTitle = this.addDrawableChild(new LabelledTextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 55, 200, 20, new TranslatableText("addServer.enterName"), new TranslatableText("gui.spray_madness.spray_gallery.add_spray.spray_name")));
        sprayTitle.setMaxLength(48);

        sprayPath = this.addDrawableChild(new LabelledTextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 20, 200, 20, new TranslatableText("addServer.enterName"), new TranslatableText("gui.spray_madness.spray_gallery.add_spray.file")));
        sprayPath.setMaxLength(128);
        sprayPath.setText(path);
        sprayPath.setChangedListener(text -> {
            File file = new File(text);
            shouldRenderFileWarning = (!file.exists() || file.isDirectory());
        });

        emissive = this.addDrawableChild(new CheckboxWidget(this.width / 2 - 100, this.height / 2 + 15, 20, 20, new TranslatableText(""), false, false));

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 2 + 55, 99, 20, new TranslatableText("Cancel"), (button -> {
            callback.accept(false);
        })));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 1, this.height / 2 + 55, 99, 20, new TranslatableText("Add Spray"), (button -> {
            if (shouldRenderFileWarning) return;
            
            File file = new File(sprayPath.getText());
            if (file.exists()) {
                this.storage.loadedTextures.add(new SprayTexture(new File(sprayPath.getText()), emissive.isChecked(), sprayTitle.getText()));
            }
            callback.accept(true);
        })));
    }

    public void setPath(String path) {
        this.path = path;
        this.sprayPath.setText(path);
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        this.drawTexture(matrices, this.x, this.y, 0, 0, 248, 166);

        super.render(matrices, mouseX, mouseY, delta);

        this.textRenderer.draw(matrices, GLOWS_LABEL, this.width / 2f - 75, this.height / 2f + 21, 0);

        if (shouldRenderFileWarning) {
            this.textRenderer.draw(matrices, new TranslatableText("gui.spray_madness.spray_gallery.add_spray.no_file"), this.width / 2f - 100, this.height / 2f + 3, 0xFF0000);
        }
    }
}
