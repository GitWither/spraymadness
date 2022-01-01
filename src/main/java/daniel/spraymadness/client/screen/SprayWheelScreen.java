package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
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

    private static final int SPRAY_SPACING = 40;

    private static final int SPRAY_TEXTURE_WIDTH = 25;
    private static final int SPRAY_TEXTURE_HEIGHT = 25;

    private static final int SPRAY_QUAD_WIDTH = (SPRAY_TEXTURE_WIDTH * 3 + SPRAY_SPACING * 2);

    private double mouseDeltaX = 0;
    private double mouseDeltaY = 0;

    private int x;
    private int y;

    private int titleX;
    private int titleY;

    private static final int BACKGROUND_TEXTURE_WIDTH = 170;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 170;

    private static final int WHITE = (255 << 16) + (255 << 8) + 255;

    public SprayWheelScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        this.x = (this.width - BACKGROUND_TEXTURE_WIDTH) / 2;
        this.y = (this.height - BACKGROUND_TEXTURE_HEIGHT) / 2;

        this.titleX = (BACKGROUND_TEXTURE_WIDTH - this.textRenderer.getWidth(TITLE)) / 2 + this.x;
        this.titleY = this.y + 6;

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

        if (!SprayMadness.SPRAY_WHEEL_KEYBIND.isPressed()) {
            this.onClose();
        }
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
                //DrawableHelper.drawTexture(matrices, 0, 0, 0, 0,  texture.getWidth(), texture.getHeight(), texture.getWidth(), texture.getHeight());
                //MatrixStack matrices, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight
                SprayWheelScreen.drawSprayTexture(matrices, texture, x, y);
                matrices.pop();

                if (deltaToIndex(mouseDeltaX, mouseDeltaY) == i) {
                    DrawableHelper.fill(matrices, 0, 0, SPRAY_TEXTURE_WIDTH, SPRAY_TEXTURE_HEIGHT, 0x11111111);
                }
                matrices.push();
                matrices.translate(34.5f, -5, 1);
                DrawableHelper.fill(matrices, 0, 0, 38, 38, 0x11111111);
                matrices.pop();

                String sprayName = texture.getTitle();

                OrderedText text = OrderedText.styledForwardsVisitedString(sprayName, Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.AQUA)));
                matrices.push();
                matrices.translate((int) (x * SPRAY_SPACING + SPRAY_TEXTURE_WIDTH / 2f) + x, y * SPRAY_SPACING + SPRAY_TEXTURE_HEIGHT + y + 2, 0);
                matrices.scale(0.5f, 0.5f, 1);
                
                //here both x and y are 0 since they are adjusted within the matrix stack - if I translate after scaling it's not going to work
                DrawableHelper.drawCenteredTextWithShadow(matrices, this.textRenderer, text, 0, 0, WHITE);
                matrices.pop();
                //DrawableHelper.drawCenteredTextWithShadow(matrices, this.textRenderer, text, (int) (x * SPRAY_SPACING + SPRAY_TEXTURE_WIDTH / 2f), y * SPRAY_SPACING + SPRAY_TEXTURE_HEIGHT + 1,(255 << 16) + (255 << 8) + 255);

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

        System.out.println(deltaToIndex(mouseDeltaX, mouseDeltaY));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        //just so IntelliJ can stfu
        if (this.client == null) return;


        mouseDeltaX = mouseX - this.client.mouse.getX() / 2f;
        mouseDeltaY = mouseY - this.client.mouse.getY() / 2f;
    }

    private static int deltaToIndex(double mouseDeltaX, double mouseDeltaY) {
        if (mouseDeltaX == 0 && mouseDeltaY == 0) return -1;

        if (mouseDeltaX <= -0.5 && mouseDeltaY <= -0.5) {
            return 0;
        }

        if (mouseDeltaX <= -0.5 && mouseDeltaY == 0.0) {
            return 1;
        }

        if (mouseDeltaX <= -0.5 && mouseDeltaY >= 0.5) {
            return 2;
        }

        if (mouseDeltaX == 0.0 && mouseDeltaY >= 0.5) {
            return 3;
        }

        if (mouseDeltaX >= 0.5 && mouseDeltaY >= 0.5) {
            return 4;
        }

        if (mouseDeltaX >= 0.5 && mouseDeltaY == 0.0) {
            return 5;
        }

        if (mouseDeltaX >= 0.5 && mouseDeltaY <= -0.5) {
            return 6;
        }

        if (mouseDeltaX == 0.0 && mouseDeltaY <= 0.5) {
            return 7;
        }

        return -1;
    }


    private static void drawSprayTexture(MatrixStack matrices, SprayTexture texture, int x, int y) {
        Matrix4f posMat = matrices.peek().getPositionMatrix();

        int x1 = x + SPRAY_TEXTURE_WIDTH;
        int y1 = y + SPRAY_TEXTURE_HEIGHT;

        RenderSystem.setShaderTexture(0, texture.getIdentifier());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(posMat, x, y1, 0).texture(0, 1).next();
        bufferBuilder.vertex(posMat, x1, y1, 0).texture(1, 1).next();
        bufferBuilder.vertex(posMat, x1, y, 0).texture(1, 0).next();
        bufferBuilder.vertex(posMat, x, y, 0).texture(0, 0).next();

        bufferBuilder.end();

        BufferRenderer.draw(bufferBuilder);
    }
}
