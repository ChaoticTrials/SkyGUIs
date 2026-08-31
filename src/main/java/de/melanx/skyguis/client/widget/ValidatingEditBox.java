package de.melanx.skyguis.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.awt.Color;

public class ValidatingEditBox extends EditBox {

    private final Runnable onTextChanged;
    private boolean valid = true;
    private boolean renderOutline = false;

    public ValidatingEditBox(Font font, int x, int y, int width, int height, Component message, Runnable onTextChanged) {
        super(font, x, y, width, height, message);
        this.onTextChanged = onTextChanged;
    }

    @Override
    public void extractWidgetRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

        if (this.renderOutline) {
            this.renderOutline(graphics, (this.valid ? Color.GREEN : Color.RED).getRGB());
        }
    }

    public void renderOutline(GuiGraphicsExtractor guiGraphics, int color) {
        guiGraphics.fill(this.x, this.y, this.x + this.width, this.y + 1, color);
        guiGraphics.fill(this.x, this.y, this.x + 1, this.y + this.height, color);
        guiGraphics.fill(this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, color);
        guiGraphics.fill(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, color);
    }

    public void setValid() {
        this.valid = true;
    }

    public void setInvalid() {
        this.valid = false;
    }

    public void updateOutlineRendering(boolean renderOutline) {
        this.renderOutline = renderOutline;
    }

    @Override
    public void onValueChange(@Nonnull String suggestion) {
        super.onValueChange(suggestion);
        this.onTextChanged.run();
    }
}
