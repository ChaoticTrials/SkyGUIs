package de.melanx.skyguis.client.widget;

import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class ClickableText extends Button {

    private final int color;

    public ClickableText(int x, int y, int color, Component message, OnPress onPress) {
        super(x, y, TextHelper.stringLength(message), 12, message, onPress, Button.DEFAULT_NARRATION);
        this.color = color;
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.x, this.y, this.color, false);
    }
}
