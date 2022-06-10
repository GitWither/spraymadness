package daniel.spraymadness.client.util.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AddToWheelButtonWidget extends ButtonWidget {
    private State state;
    private static final Text REMOVE = Text.translatable("gui.spray_madness.spray_gallery.spray_wheel.remove");
    private static final Text ADD = Text.translatable("gui.spray_madness.spray_gallery.spray_wheel.add");
    private static final Text FULL = Text.translatable("gui.spray_madness.spray_gallery.spray_wheel.full");
    private static final Text EMPTY = Text.translatable("gui.spray_madness.spray_gallery.spray_wheel.empty");

    private static final Text REMOVE_TOOLTIP = Text.translatable("gui.spray_madness.spray_gallery.spray_wheel.remove.tooltip");
    private static final Text FULL_TOOLTIP = Text.translatable("gui.spray_madness.spray_gallery.spray_wheel.full.tooltip");
    private static final Text ADD_TOOLTIP = Text.translatable("gui.spray_madness.spray_gallery.spray_wheel.add.tooltip");
    private static final Text EMPTY_TOOLTIP = Text.translatable("gui.spray_madness.spray_gallery.spray_wheel.empty.tooltip");

    public AddToWheelButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
        this.setEmptyMessage();
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
        this.active = true;
        this.state = State.ADD;
        this.setMessage(ADD);
    }

    public void setEmptyMessage() {
        this.active = false;
        this.state = State.EMPTY;
        this.setMessage(EMPTY);
    }

    public Text getTooltipMessage() {
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
            case EMPTY -> {
                return EMPTY_TOOLTIP;
            }
        }
        return ADD_TOOLTIP;
    }

    private enum State {
        FULL,
        REMOVE,
        ADD,
        EMPTY
    }
}
