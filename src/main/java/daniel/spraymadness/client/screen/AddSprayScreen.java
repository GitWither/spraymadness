package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class AddSprayScreen extends Screen {
    private static final Identifier BACKGROUND = new Identifier("textures/gui/demo_background.png");

    private static final TranslatableText GLOWS_LABEL = new TranslatableText("gui.spray_madness.spray_gallery.add_spray.emissive");

    private static final int BACKGROUND_WIDTH = 248;
    private static final int BACKGROUND_HEIGHT = 166;

    private int x;
    private int y;

    private BooleanConsumer callback;

    protected AddSprayScreen(BooleanConsumer callback) {
        super(Text.of("Add Spray"));
        this.callback = callback;
    }

    @Override
    protected void init() {
        this.x = (this.width - BACKGROUND_WIDTH) / 2;
        this.y = (this.height - BACKGROUND_HEIGHT) / 2;

        this.addDrawableChild(new LabelledTextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 55, 200, 20, new TranslatableText("addServer.enterName"), new LiteralText("Spray Name")));
        this.addDrawableChild(new LabelledTextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 20, 179, 20, new TranslatableText("addServer.enterName"), new LiteralText("File")));
        this.addDrawableChild(new CheckboxWidget(this.width / 2 - 100, this.height / 2 + 15, 20, 20, new TranslatableText(""), false, false));

        this.addDrawableChild(new ButtonWidget(this.width / 2 + 81, this.height / 2 - 20, 20, 20, new LiteralText("..."), (button -> {
        })));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 2 + 55, 99, 20, new LiteralText("Cancel"), (button -> {
            callback.accept(false);
        })));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 1, this.height / 2 + 55, 99, 20, new LiteralText("Add Spray"), (button -> {
            callback.accept(true);
        })));
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        this.drawTexture(matrices, this.x, this.y, 0, 0, 248, 166);

        super.render(matrices, mouseX, mouseY, delta);

        this.textRenderer.draw(matrices, GLOWS_LABEL, this.width / 2 - 75, this.height / 2 + 21, 0);

    }
}
