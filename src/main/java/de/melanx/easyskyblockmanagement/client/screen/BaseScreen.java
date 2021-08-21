package de.melanx.easyskyblockmanagement.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

public abstract class BaseScreen extends Screen {

    protected final int xSize;
    protected final int ySize;
    protected int relX;
    protected int relY;

    protected BaseScreen(Component component, int xSize, int ySize) {
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
    abstract public void tick();

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
