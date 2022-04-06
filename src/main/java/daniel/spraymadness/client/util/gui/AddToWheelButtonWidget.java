package daniel.spraymadness.client.util.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class AddToWheelButtonWidget extends ButtonWidget {
    private State state;
    private static final TranslatableText REMOVE = new TranslatableText("gui.spray_madness.spray_gallery.spray_wheel.remove");
    private static final TranslatableText ADD = new TranslatableText("gui.spray_madness.spray_gallery.spray_wheel.add");
    private static final TranslatableText FULL = new TranslatableText("gui.spray_madness.spray_gallery.spray_wheel.full");

    private static final TranslatableText REMOVE_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.spray_wheel.remove.tooltip");
    private static final TranslatableText FULL_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.spray_wheel.full.tooltip");
    private static final TranslatableText ADD_TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.spray_wheel.add.tooltip");

    public AddToWheelButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
    }

    public void setRemove() {
        this.active = true;
        this.state = State.REMOVE;
        this.setMessage(REMOVE);
    }

    public void setFull() {
        this.active = false;
        this.state = State.FULL;
        this.setMessage(FULL);
    }

    public void setAddMessage() {
        this.state = State.ADD;
        this.setMessage(ADD);
    }

    public TranslatableText getTooltipMessage() {
        switch (this.state) {
            case FULL -> {
                return FULL_TOOLTIP;
            }
            case REMOVE -> {
                return REMOVE_TOOLTIP;
            }
            case ADD -> {
                return ADD_TOOLTIP;
            }
        }
        return ADD_TOOLTIP;
    }

    private enum State {
        FULL,
        REMOVE,
        ADD
    }
}
