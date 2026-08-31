package de.melanx.skyguis.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.util.ComponentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.awt.Color;

public abstract class BaseScreen extends Screen {

    protected static final Identifier GENERIC = Identifier.fromNamespaceAndPath(SkyGUIs.getInstance().modid, "textures/gui/generic.png");
    protected static final MutableComponent PREV_SCREEN_COMPONENT = ComponentBuilder.text("previous_screen");
    public static final OnAbort DEFAULT_ABORT = () -> Minecraft.getInstance().popGuiLayer();
    public static final MutableComponent OPEN_NEW_SCREEN = ComponentBuilder.text("new_screen").withStyle(ChatFormatting.ITALIC);

    @Nonnull
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final int xSize;
    protected final int ySize;
    protected int relX;
    protected int relY;

    public BaseScreen(Component component, int xSize, int ySize) {
        super(component);
        this.xSize = xSize;
        this.ySize = ySize;
        NeoForge.EVENT_BUS.addListener(this::guiInitPre);
    }

    private void guiInitPre(ScreenEvent.Init.Pre event) {
        this.relX = (event.getScreen().width - this.xSize) / 2;
        this.relY = (event.getScreen().height - this.ySize) / 2;
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        this.extractForeground(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        RenderHelper.renderGuiBackground(RenderPipelines.GUI_TEXTURED, graphics, this.relX, this.relY, this.xSize, this.ySize, GENERIC, 128, 64, 4, 125, 4, 60);
    }

    @Override
    protected void extractBlurredBackground(@Nonnull GuiGraphicsExtractor graphics) {
        if (Minecraft.getInstance().screen != this) {
            return;
        }

        super.extractBlurredBackground(graphics);
    }

    public void extractForeground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        //
    }

    public void renderTitle(@Nonnull GuiGraphicsExtractor graphics) {
        graphics.text(this.font, this.title, this.centeredX(this.font.width(this.title.getVisualOrderText())), this.y(10), Color.DARK_GRAY.getRGB(), false);
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
    public boolean keyPressed(@Nonnull KeyEvent event) {
        InputConstants.Key mapping = InputConstants.getKey(event);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mapping) && !(this.getFocused() instanceof EditBox)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public interface OnConfirm {
        void onConfirm();
    }

    public interface OnAbort {
        void onAbort();
    }
}
