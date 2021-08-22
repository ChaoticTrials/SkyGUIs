package de.melanx.easyskyblockmanagement.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

public abstract class BaseScreen extends Screen {

    protected final int xSize;
    protected final int ySize;
    protected int relX;
    protected int relY;

    public BaseScreen(Component component, int xSize, int ySize) {
        super(component);
        this.xSize = xSize;
        this.ySize = ySize;
        MinecraftForge.EVENT_BUS.addListener(this::onGuiInit);
    }

    private void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
        this.relX = (event.getGui().width - this.xSize) / 2;
        this.relY = (event.getGui().height - this.ySize) / 2;
        this.init(event);
    }

    abstract protected void init(GuiScreenEvent.InitGuiEvent event);

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderHelper.renderGuiBackground(poseStack, this.relX, this.relY, this.xSize, this.ySize);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    abstract public void tick();

    public int getRelX() {
        return this.relX;
    }

    public int getRelY() {
        return this.relY;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mapping = InputConstants.getKey(keyCode, scanCode);
        //noinspection ConstantConditions
        if (keyCode != 256 && (this.minecraft.options.keyInventory.isActiveAndMatches(mapping)
                || this.minecraft.options.keyDrop.isActiveAndMatches(mapping))) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
