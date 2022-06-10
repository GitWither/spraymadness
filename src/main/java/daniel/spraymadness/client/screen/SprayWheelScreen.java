package daniel.spraymadness.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import daniel.spraymadness.client.SprayMadness;
import daniel.spraymadness.client.texture.SprayTexture;
import daniel.spraymadness.client.util.Colors;
import daniel.spraymadness.client.util.Spray;
import daniel.spraymadness.client.util.SprayStorage;
import daniel.spraymadness.client.util.gui.DrawHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class SprayWheelScreen extends Screen {
    public static final Identifier TEXTURE = new Identifier(SprayMadness.MOD_ID, "textures/gui/spray_wheel.png");

    private static final Text TITLE = Text.translatable("gui.spray_madness.spray_wheel.title");


    private final SprayStorage storage;

    public static final int SPRAY_SPACING = 38;

    public static final int SPRAY_TEXTURE_WIDTH = 25;
    public static final int SPRAY_TEXTURE_HEIGHT = 25;

    public static final int SPRAY_QUAD_WIDTH = SPRAY_SPACING * 2 + SPRAY_TEXTURE_WIDTH;

    private int x;
    private int y;

    private int titleX;
    private int titleY;

    private int selectedIndex = -1;

    private static final int BACKGROUND_TEXTURE_WIDTH = 170;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 170;

    private static final int BACKGROUND_TEXTURE_OFFSET = 21;


    public SprayWheelScreen(SprayStorage storage) {
        super(TITLE);
        this.storage = storage;
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
    public boolean shouldPause() {
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
            if (selectedIndex >= 0) {
                HitResult hit = client.crosshairTarget;
                if (hit != null) {
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        Spray spray = new Spray(
                                storage.sprayWheelTextures.get(selectedIndex),
                                new Vec3f((float) hit.getPos().x, (float) hit.getPos().y, (float) hit.getPos().z),
                                ((BlockHitResult) hit).getSide(),
                                client.player.world.getRegistryKey().getValue(),
                                client.player.getHorizontalFacing().getHorizontal()
                        );
                        client.world.playSound(((BlockHitResult) hit).getBlockPos(), SprayMadness.ENTITY_PLAYER_SPRAY, SoundCategory.PLAYERS, 5, 1, true);
                        storage.totalWorldSprays.add(spray);
                    }
                }
            }

            this.close();

            return true;
        }

        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        this.textRenderer.drawWithShadow(matrices, TITLE, this.titleX, this.titleY, Colors.WHITE);

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.drawTexture(matrices, this.x, this.y, 0, 0, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        RenderSystem.disableBlend();

        matrices.push();
        matrices.translate((this.width / 2f - (SPRAY_QUAD_WIDTH) / 2f), this.y + 32, 0);
        {
            int x = 0;
            int y = 0;

            for (int i = 0; i < storage.sprayWheelTextures.size(); i++) {
                if (i > 7) break;

                SprayTexture texture = storage.sprayWheelTextures.get(i);

                RenderSystem.setShaderTexture(0, texture.getIdentifier());
                DrawHelper.drawSprayTexture(matrices, texture, x * SPRAY_SPACING, y * SPRAY_SPACING + y, SPRAY_TEXTURE_WIDTH, SPRAY_TEXTURE_HEIGHT);
                //DrawableHelper.fill(matrices, 0, 0,  SPRAY_SPACING - 7, SPRAY_SPACING, SELECTION_COLOR);
                int x1 = (int) (x * (SPRAY_SPACING) + x - 7.5f + (this.width / 2f - (SPRAY_QUAD_WIDTH) / 2f));
                int y1 = this.y + 32 + y * (SPRAY_SPACING) + y - 5;

                int x2 = x1 + SPRAY_TEXTURE_WIDTH + 13;
                int y2 = y1 + SPRAY_TEXTURE_HEIGHT + 13;
                if (mouseX >= x1 && mouseY >= y1 && mouseX <= x2 && mouseY <= y2) {
                    DrawHelper.drawSolidColor(matrices.peek().getPositionMatrix(), x * (SPRAY_SPACING) + x - 7.5f, y * (SPRAY_SPACING) + y - 5,  x * (SPRAY_SPACING) + SPRAY_TEXTURE_HEIGHT + x + 5.5f, y * (SPRAY_SPACING) + SPRAY_TEXTURE_HEIGHT + y + 8, Colors.SPRAY_WHEEL_SELECTION_COLOR);
                    selectedIndex = i;
                }

                String sprayName = this.textRenderer.trimToWidth(texture.getTitle(), 75);

                OrderedText text = OrderedText.styledForwardsVisitedString(sprayName, Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.AQUA)));
                matrices.push();
                matrices.translate((int) (x * SPRAY_SPACING + SPRAY_TEXTURE_WIDTH / 2f) + 0.5f, y * SPRAY_SPACING + SPRAY_TEXTURE_HEIGHT + y + 2, 0);
                matrices.scale(0.5f, 0.5f, 1);

                //here both x and y are 0 since they are adjusted within the matrix stack - if I translate after scaling it's not going to work
                DrawableHelper.drawCenteredTextWithShadow(matrices, this.textRenderer, text, 0, 0, Colors.WHITE);
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
}
