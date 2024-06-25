package de.melanx.skyguis.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.widget.LoadingCircle;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

public abstract class BaseScreen extends Screen {

    protected static final ResourceLocation GENERIC = new ResourceLocation(SkyGUIs.getInstance().modid, "textures/gui/generic.png");
    protected static final MutableComponent PREV_SCREEN_COMPONENT = ComponentBuilder.text("previous_screen");
    public static final OnAbort DEFAULT_ABORT = () -> Minecraft.getInstance().popGuiLayer();
    public static final MutableComponent OPEN_NEW_SCREEN = ComponentBuilder.text("new_screen").withStyle(ChatFormatting.ITALIC);

    protected final int xSize;
    protected final int ySize;
    protected int relX;
    protected int relY;
    protected boolean preventUserInput;
    private LoadingResult loadingResult;
    @Nullable
    private LoadingCircle loadingCircle;

    public BaseScreen(Component component, int xSize, int ySize) {
        super(component);
        this.xSize = xSize;
        this.ySize = ySize;
        MinecraftForge.EVENT_BUS.addListener(this::guiInitPre);
        MinecraftForge.EVENT_BUS.addListener(this::guiInitPost);
    }

    private void guiInitPre(ScreenEvent.Init.Pre event) {
        this.relX = (event.getScreen().width - this.xSize) / 2;
        this.relY = (event.getScreen().height - this.ySize) / 2;
    }

    private void guiInitPost(ScreenEvent.Init.Post event) {
        if (this instanceof LoadingResultHandler loadingResultHandler) {
            boolean prevActive = this.loadingCircle != null && this.loadingCircle.isActive();
            this.loadingCircle = loadingResultHandler.createLoadingCircle(this);
            if (this.loadingCircle != null) {
                this.loadingCircle.setActive(prevActive);
            }
        }
    }

    @Override
    public final void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean loadingCircleActive = this.loadingCircle != null && this.loadingCircle.isActive();

        if (!loadingCircleActive) {
            this.renderBackground(guiGraphics);
        }
        this.render_(guiGraphics, mouseX, mouseY, partialTick);
        if (loadingCircleActive) {
            this.renderBackground(guiGraphics);
            this.loadingCircle.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public void render_(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderHelper.renderGuiBackground(guiGraphics, this.relX, this.relY, this.xSize, this.ySize, GENERIC, 128, 64, 4, 125, 4, 60);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Nullable
    public LoadingCircle getLoadingCircle() {
        return this.loadingCircle;
    }

    public void renderTitle(@Nonnull GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, this.title, this.centeredX(this.font.width(this.title.getVisualOrderText())), this.y(10), Color.DARK_GRAY.getRGB(), false);
    }

    public float centeredX(float width) {
        return this.x(((float) this.xSize / 2) - width);
    }

    public float centeredY(float height) {
        return this.x(((float) this.xSize / 2) - height);
    }

    public int centeredX(int width) {
        return (int) this.x(((float) this.xSize / 2) - ((float) width / 2));
    }

    public int centeredY(int height) {
        return (int) this.y(((float) this.ySize / 2) - ((float) height / 2));
    }

    public int getSizeX() {
        return this.xSize;
    }

    public int getSizeY() {
        return this.ySize;
    }

    public int getRelX() {
        return this.relX;
    }

    public int getRelY() {
        return this.relY;
    }

    public int x(int x) {
        return this.relX + x;
    }

    public int y(int y) {
        return this.relY + y;
    }

    public float x(float x) {
        return this.relX + x;
    }

    public float y(float y) {
        return this.relY + y;
    }

    @Override
    public void tick() {
        if (this instanceof LoadingResultHandler loadingResultHandler && this.loadingCircle != null && this.loadingCircle.isActive()) {
            this.preventUserInput = true;
            LoadingResult result = this.getResult();
            if (result != null) {
                loadingResultHandler.onLoadingResult(result);
                this.loadingCircle.setActive(false);
                this.preventUserInput = false;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.preventUserInput) {
            return false;
        }

        InputConstants.Key mapping = InputConstants.getKey(keyCode, scanCode);
        //noinspection ConstantConditions
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mapping) && !(this.getFocused() instanceof EditBox)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.preventUserInput) {
            return false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.preventUserInput) {
            return false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.preventUserInput) {
            return false;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.preventUserInput) {
            return false;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.preventUserInput) {
            return false;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.preventUserInput) {
            return false;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void changeFocus(@Nonnull ComponentPath path) {
        if (!this.preventUserInput) {
            super.changeFocus(path);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected LoadingResult getResult() {
        LoadingResult tempResult = null;
        if (this.loadingResult != null) {
            tempResult = this.loadingResult;
            this.loadingResult = null;
        }

        return tempResult;
    }

    public void setResult(@Nonnull LoadingResult result) {
        this.loadingResult = result;
    }

    public interface OnConfirm {
        void onConfirm();
    }

    public interface OnAbort {
        void onAbort();
    }
}
