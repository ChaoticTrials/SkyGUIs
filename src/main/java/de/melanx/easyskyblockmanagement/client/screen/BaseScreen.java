package de.melanx.easyskyblockmanagement.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.client.widget.LoadingCircle;
import de.melanx.easyskyblockmanagement.util.LoadingResult;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

public abstract class BaseScreen extends Screen {

    private static final ResourceLocation GENERIC = new ResourceLocation(EasySkyblockManagement.getInstance().modid, "textures/gui/generic.png");
    protected static final MutableComponent PREV_SCREEN_COMPONENT = new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".text.previous_screen");
    public static final OnAbort DEFAULT_ABORT = () -> ForgeHooksClient.popGuiLayer(Minecraft.getInstance());

    protected final int xSize;
    protected final int ySize;
    protected int relX;
    protected int relY;
    protected boolean preventUserInput;
    private LoadingResult loadingResult;
    private LoadingCircle loadingCircle;

    public BaseScreen(Component component, int xSize, int ySize) {
        super(component);
        this.xSize = xSize;
        this.ySize = ySize;
        MinecraftForge.EVENT_BUS.addListener(this::guiInitPre);
        MinecraftForge.EVENT_BUS.addListener(this::guiInitPost);
    }

    private void guiInitPre(GuiScreenEvent.InitGuiEvent.Pre event) {
        this.relX = (event.getGui().width - this.xSize) / 2;
        this.relY = (event.getGui().height - this.ySize) / 2;
    }

    private void guiInitPost(GuiScreenEvent.InitGuiEvent.Post event) {
        boolean prevActive = this.loadingCircle != null && this.loadingCircle.isActive();
        this.loadingCircle = this.createLoadingCircle();
        if (this.loadingCircle != null) {
            this.loadingCircle.setActive(prevActive);
        }
    }

    @Override
    public final void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        boolean loadingCircleActive = this.loadingCircle != null && this.loadingCircle.isActive();

        if (!loadingCircleActive) {
            this.renderBackground(poseStack);
        }
        this.render_(poseStack, mouseX, mouseY, partialTick);
        if (loadingCircleActive) {
            this.renderBackground(poseStack);
            this.loadingCircle.render(poseStack, mouseX, mouseY, partialTick);
        }
    }

    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderHelper.renderGuiBackground(poseStack, this.relX, this.relY, this.xSize, this.ySize, GENERIC, 128, 64, 4, 125, 4, 60);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    public LoadingCircle createLoadingCircle() {
        return null;
    }

    @Nullable
    public LoadingCircle getLoadingCircle() {
        return this.loadingCircle;
    }

    public void onLoadingResult(LoadingResult result) {
    }

    public void renderTitle(@Nonnull PoseStack poseStack) {
        this.font.draw(poseStack, this.title, this.centeredX(this.font.width(this.title.getVisualOrderText())), this.y(10), Color.DARK_GRAY.getRGB());
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
        if (this.loadingCircle != null && this.loadingCircle.isActive()) {
            this.preventUserInput = true;
            LoadingResult result = this.getResult();
            if (result != null) {
                this.onLoadingResult(result);
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
    public boolean changeFocus(boolean focus) {
        if (this.preventUserInput) {
            return false;
        }

        return super.changeFocus(focus);
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
