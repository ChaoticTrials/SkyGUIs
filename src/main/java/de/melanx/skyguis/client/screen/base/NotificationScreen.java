package de.melanx.skyguis.client.screen.base;

import com.mojang.blaze3d.platform.InputConstants;
import de.melanx.skyguis.client.screen.BaseScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

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
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        this.renderTitle(graphics);
    }

    @Override
    public boolean keyPressed(@Nonnull KeyEvent event) {
        int keyCode = event.key();

        if (keyCode == InputConstants.KEY_ESCAPE) {
            this.onAbort.onAbort();
            this.minecraft.setScreen(null);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.onConfirm.onConfirm();
            this.minecraft.setScreen(null);
            return true;
        }

        return super.keyPressed(event);
    }
}
