package daniel.spraymadness.client.util.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class LabelledTextFieldWidget extends TextFieldWidget {
    private Text label;
    //i don't want to use access wideners so deal with it
    private TextRenderer textRenderer;

    public LabelledTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, Text label) {
        super(textRenderer, x, y, width, height, text);

        this.textRenderer = textRenderer;
        this.label = label;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.textRenderer.drawWithShadow(matrices, label, this.x, this.y - this.height / 2f, 0xA0A0A0);
        super.render(matrices, mouseX, mouseY, delta);
    }


}
