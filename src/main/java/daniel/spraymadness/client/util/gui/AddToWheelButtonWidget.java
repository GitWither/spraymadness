package daniel.spraymadness.client.util.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class AddToWheelButtonWidget extends ButtonWidget {
    public AddToWheelButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
    }

    public void setRemove() {
        this.active = true;
        this.setMessage(new TranslatableText("remove"));
    }

    public void setFull() {
        this.active = false;
        this.setMessage(new TranslatableText("wheel full"));
    }

    public void setAddMessage() {
        this.setMessage(new TranslatableText("add"));
    }
}
