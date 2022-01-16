package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.gui.DrawHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class SprayWheelScreen extends Screen {
    protected static final Identifier TEXTURE = new Identifier(SprayMadness.MOD_ID, "textures/gui/spray_wheel.png");

    private static final Text TITLE = new TranslatableText("gui.spray_madness.spray_wheel.title");

    private static final int SPRAY_SPACING = 38;

    private static final int SPRAY_TEXTURE_WIDTH = 25;
    private static final int SPRAY_TEXTURE_HEIGHT = 25;

    private static final int SPRAY_QUAD_WIDTH = (SPRAY_TEXTURE_WIDTH * 3 + SPRAY_SPACING * 2);

    private int x;
    private int y;

    private int titleX;
    private int titleY;

    private double selectionOriginX;
    private double selectionOriginY;

    private int selectedIndex = -1;

    private static final int BACKGROUND_TEXTURE_WIDTH = 170;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 170;

    private static final int BACKGROUND_TEXTURE_OFFSET = 21;

    private static final int WHITE = (255 << 16) + (255 << 8) + 255;

    //ARGB format (this sucks)
    public static final int SELECTION_COLOR = (122 << 24) + (51 << 16) + (255 << 8) + 106;

    public SprayWheelScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        this.x = (this.width - BACKGROUND_TEXTURE_WIDTH) / 2;
        this.y = (this.height - BACKGROUND_TEXTURE_HEIGHT) / 2;

        this.titleX = (BACKGROUND_TEXTURE_WIDTH - this.textRenderer.getWidth(TITLE)) / 2 + this.x;
        this.titleY = this.y + 6;

        this.selectionOriginX = this.x + BACKGROUND_TEXTURE_OFFSET;
        this.selectionOriginY = this.y + BACKGROUND_TEXTURE_OFFSET;

        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(MatrixStack matrices, int vOffset) {
        super.renderBackground(matrices, vOffset);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (SprayMadness.SPRAY_WHEEL_KEYBIND.matchesKey(keyCode, scanCode)) {
            this.onClose();

            return true;
        }

        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        this.textRenderer.drawWithShadow(matrices, TITLE, this.titleX, this.titleY, WHITE);

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.drawTexture(matrices, this.x, this.y, 0, 0, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        RenderSystem.disableBlend();


        matrices.push();
        matrices.translate((this.width - SPRAY_QUAD_WIDTH + SPRAY_TEXTURE_WIDTH * 2 - 2) / 2f, this.y + 32, 0);
        {
            int x = 0;
            int y = 0;

            for (int i = 0; i < SprayMadness.sprayTextures.size(); i++) {
                if (i > 7) break;

                SprayTexture texture = SprayMadness.sprayTextures.get(i);

                matrices.push();
                matrices.translate(x * SPRAY_SPACING, y * SPRAY_SPACING, 0);
                RenderSystem.setShaderTexture(0, texture.getIdentifier());
                DrawHelper.drawSprayTexture(matrices, texture, x, y, SPRAY_TEXTURE_WIDTH, SPRAY_TEXTURE_HEIGHT);
                matrices.pop();

                if (selectedIndex == i) {
                    matrices.push();
                    //1.5 is puuuuuuuurely a magic number, cry about it
                    matrices.translate(x * SPRAY_SPACING + SPRAY_TEXTURE_WIDTH / 2f + x, y * SPRAY_SPACING + SPRAY_TEXTURE_HEIGHT / 2f + y + 1.5, 0);
                    matrices.scale(19, 19, 1);

                    DrawableHelper.fill(matrices, -1, -1, 1, 1,SELECTION_COLOR);

                    matrices.pop();
                }

                String sprayName = texture.getTitle();

                OrderedText text = OrderedText.styledForwardsVisitedString(sprayName, Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.AQUA)));
                matrices.push();
                matrices.translate((int) (x * SPRAY_SPACING + SPRAY_TEXTURE_WIDTH / 2f) + x, y * SPRAY_SPACING + SPRAY_TEXTURE_HEIGHT + y + 2, 0);
                matrices.scale(0.5f, 0.5f, 1);

                //here both x and y are 0 since they are adjusted within the matrix stack - if I translate after scaling it's not going to work
                DrawableHelper.drawCenteredTextWithShadow(matrices, this.textRenderer, text, 0, 0, WHITE);
                matrices.pop();

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
        }
        matrices.pop();
    }

    @Override
    public void mouseMoved(double posX, double posY) {
        //just so IntelliJ can stfu
        if (this.client == null) return;

        if (posX >= selectionOriginX && posY >= selectionOriginY && posX <= SPRAY_SPACING + selectionOriginX && posY <= SPRAY_SPACING + selectionOriginY) {
            selectedIndex = 0;
        }
        if (posX >= selectionOriginX && posY >= SPRAY_SPACING + selectionOriginY && posX <= SPRAY_SPACING + selectionOriginX && posY <= 2 * SPRAY_SPACING + selectionOriginY) {
            selectedIndex = 1;
        }
        if (posX >= selectionOriginX && posY >= 2 * SPRAY_SPACING + selectionOriginY && posX <= SPRAY_SPACING + selectionOriginX && posY <= 3 * SPRAY_SPACING + selectionOriginY) {
            selectedIndex = 2;
        }
        if (posX >= SPRAY_SPACING + selectionOriginX && posY >= 2 * SPRAY_SPACING + selectionOriginY && posX <= 2 * SPRAY_SPACING + selectionOriginX && posY <= 3 * SPRAY_SPACING + selectionOriginY) {
            selectedIndex = 3;
        }
        if (posX >= 2 * SPRAY_SPACING + selectionOriginX && posY >= 2 * SPRAY_SPACING + selectionOriginY && posX <= 3 * SPRAY_SPACING + selectionOriginX && posY <= 3 * SPRAY_SPACING + selectionOriginY) {
            selectedIndex = 4;
        }
        if (posX >= 2 * SPRAY_SPACING + selectionOriginX && posY >= SPRAY_SPACING + selectionOriginY && posX <= 3 * SPRAY_SPACING + selectionOriginX && posY <= 2 * SPRAY_SPACING + selectionOriginY) {
            selectedIndex = 5;
        }
        if (posX >= 2 * SPRAY_SPACING + selectionOriginX && posY >= selectionOriginY && posX <= 3 * SPRAY_SPACING + selectionOriginX && posY <= SPRAY_SPACING + selectionOriginY) {
            selectedIndex = 6;
        }
        if (posX >= SPRAY_SPACING + selectionOriginX && posY >= selectionOriginY && posX <= 2 * SPRAY_SPACING + selectionOriginX && posY <= SPRAY_SPACING + selectionOriginY) {
            selectedIndex = 7;
        }

    }
}
