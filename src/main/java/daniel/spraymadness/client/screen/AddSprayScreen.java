package daniel.spraymadness.client.screen;

import daniel.spraymadness.client.util.gui.LabelledTextFieldWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class AddSprayScreen extends Screen {
    protected AddSprayScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        this.addDrawableChild(new LabelledTextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2, 200, 20, new TranslatableText("addServer.enterName"), new LiteralText("Spray Name")));
        this.addDrawableChild(new LabelledTextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 + 35, 179, 20, new TranslatableText("addServer.enterName"), new LiteralText("File")));

        this.addDrawableChild(new ButtonWidget(this.width / 2 + 81, this.height / 2 + 35, 20, 20, new LiteralText("..."), (button -> {

        })));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 2 + 70, 200, 20, new LiteralText("Add Spray"), (button -> {

        })));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }
}
