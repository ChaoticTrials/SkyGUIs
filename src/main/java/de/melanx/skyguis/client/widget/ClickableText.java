package de.melanx.skyguis.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class ClickableText extends Button {

    private final int color;

    public ClickableText(int x, int y, int color, Component message, OnPress onPress) {
        super(x, y, TextHelper.stringLength(message), 12, message, onPress);
        this.color = color;
    }

    public ClickableText(int x, int y, int color, Component message, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, TextHelper.stringLength(message), 12, message, onPress, onTooltip);
        this.color = color;
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft.getInstance().font.draw(poseStack, this.getMessage(), this.x, this.y, this.color);
        if (this.isHovered) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
