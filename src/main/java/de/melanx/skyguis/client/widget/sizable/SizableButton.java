package de.melanx.skyguis.client.widget.sizable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;

import javax.annotation.Nonnull;

public class SizableButton extends Button {

    private static final float DEFAULT_HEIGHT = 20f;

    public SizableButton(Builder builder) {
        super(builder);
    }

    @Override
    protected void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractDefaultSprite(graphics);

        float scale = this.height / DEFAULT_HEIGHT;
        float centerX = this.getX() + this.getWidth() / 2f;
        float centerY = this.getY() + this.getHeight() / 2f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-centerX, -centerY);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
        graphics.pose().popMatrix();
    }
}
