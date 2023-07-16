package de.melanx.skyguis.client.widget.sizable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class SizableButton extends Button {

    public SizableButton(Builder builder) {
        super(builder);
    }

    // [Vanilla copy]
    @Override
    public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getTextureY();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, i);
        // [Start] modification
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1, this.height / 20f, 1);
        guiGraphics.blit(WIDGETS_LOCATION, this.x, this.y * 20 / this.height, 0, 46 + i * 20, this.width / 2, 20);
        guiGraphics.blit(WIDGETS_LOCATION, this.x + this.width / 2, this.y * 20 / this.height, 200 - this.width / 2, 46 + i * 20, this.width / 2, 20);
        guiGraphics.pose().popPose();
        int j = this.getFGColor();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(this.height / 20f, this.height / 20f, 1);
        guiGraphics.drawCenteredString(font, this.getMessage(), (int) ((this.x + this.width / 2) / (this.height / 20f)), this.y + 6, j | Mth.ceil(this.alpha * 255.0F) << 24);
        guiGraphics.pose().popPose();
        // [End]   modification
    }
}
