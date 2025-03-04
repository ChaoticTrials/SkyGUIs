package de.melanx.skyguis.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.util.ComponentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.awt.Color;

public abstract class BaseScreen extends Screen {

    protected static final ResourceLocation GENERIC = ResourceLocation.fromNamespaceAndPath(SkyGUIs.getInstance().modid, "textures/gui/generic.png");
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
    public final void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.renderForeground(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        RenderHelper.renderGuiBackground(guiGraphics, this.relX, this.relY, this.xSize, this.ySize, GENERIC, 128, 64, 4, 125, 4, 60);
    }

    public void renderForeground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mapping = InputConstants.getKey(keyCode, scanCode);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mapping) && !(this.getFocused() instanceof EditBox)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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
