package de.melanx.skyguis.client.screen.base;

import com.mojang.blaze3d.platform.InputConstants;
import de.melanx.skyguis.client.screen.BaseScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class NotificationScreen extends BaseScreen {

    protected final BaseScreen.OnConfirm onConfirm;
    protected final BaseScreen.OnAbort onAbort;

    public NotificationScreen(Component component, int xSize, int ySize, BaseScreen.OnConfirm onConfirm) {
        this(component, xSize, ySize, onConfirm, BaseScreen.DEFAULT_ABORT);
    }

    public NotificationScreen(Component component, int xSize, int ySize, BaseScreen.OnConfirm onConfirm, BaseScreen.OnAbort onAbort) {
        super(component, xSize, ySize);
        this.onConfirm = onConfirm;
        this.onAbort = onAbort;
    }

    @Override
    public void render_(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render_(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTitle(guiGraphics);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE && this.minecraft != null) {
            this.onAbort.onAbort();
            this.minecraft.setScreen(null);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
