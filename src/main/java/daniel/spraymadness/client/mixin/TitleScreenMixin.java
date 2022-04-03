package daniel.spraymadness.client.mixin;

import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.screen.SprayGalleryScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final TranslatableText TOOLTIP = new TranslatableText("gui.spray_madness.spray_gallery.title");
    @Unique
    private static final Identifier SPRAY_CAN = new Identifier(SprayMadness.MOD_ID, "textures/gui/prototypes/spray_can_vertical.png");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void addSprayGalleryButton(CallbackInfo cb) {
        this.addDrawableChild(new TexturedButtonWidget(this.width / 2 + 150, this.height / 4 + 132, 20, 20, 0, 0, 20, SPRAY_CAN, 32, 64, (button) -> {
            this.client.setScreen(new SprayGalleryScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            TitleScreenMixin.super.renderTooltip(matrices, TOOLTIP, mouseX, mouseY);
        }, LiteralText.EMPTY));
    }
}