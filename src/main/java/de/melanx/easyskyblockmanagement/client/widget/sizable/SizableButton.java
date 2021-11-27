package de.melanx.easyskyblockmanagement.client.widget.sizable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class SizableButton extends Button {

    public SizableButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress);
    }

    public SizableButton(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, width, height, message, onPress, onTooltip);
    }

    // [Vanilla copy]
    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        // [Start] modification
        poseStack.pushPose();
        poseStack.scale(1, this.height / 20f, 1);
        this.blit(poseStack, this.x, this.y * 20 / this.height, 0, 46 + i * 20, this.width / 2, 20);
        this.blit(poseStack, this.x + this.width / 2, this.y * 20 / this.height, 200 - this.width / 2, 46 + i * 20, this.width / 2, 20);
        this.renderBg(poseStack, minecraft, mouseX, mouseY);
        poseStack.popPose();
        int j = this.getFGColor();
        poseStack.pushPose();
        poseStack.scale(this.height / 20f, this.height / 20f, 1);
        drawCenteredString(poseStack, font, this.getMessage(), (int) ((this.x + this.width / 2) / (this.height / 20f)), this.y + 6, j | Mth.ceil(this.alpha * 255.0F) << 24);
        poseStack.popPose();
        // [End]   modification

        if (this.isHovered()) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
