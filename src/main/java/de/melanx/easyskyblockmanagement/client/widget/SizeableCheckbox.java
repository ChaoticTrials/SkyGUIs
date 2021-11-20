package de.melanx.easyskyblockmanagement.client.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class SizeableCheckbox extends Checkbox {

    private static final OnTooltip NO_TOOLTIP = (checkbox, poseStack, mouseX, mouseY) -> {
    };
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private final OnTooltip onTooltip;

    public SizeableCheckbox(int x, int y, int size, boolean selected) {
        this(x, y, size, selected, NO_TOOLTIP);
    }

    public SizeableCheckbox(int x, int y, int size, boolean selected, OnTooltip onTooltip) {
        super(x, y, size, size, TextComponent.EMPTY, selected, false);
        this.onTooltip = onTooltip;
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        poseStack.pushPose();
        poseStack.scale(this.width / 20f, this.height / 20f, 1);
        blit(poseStack, this.x * 20 / this.width, this.y * 20 / this.height, this.isFocused() ? 20.0F : 0.0F, this.selected() ? 20.0F : 0.0F, 20, 20, 64, 64);
        poseStack.popPose();
        this.renderBg(poseStack, minecraft, mouseX, mouseY);

        if (this.isHovered()) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack poseStack, int mouseX, int mouseY) {
        this.onTooltip.onTooltip(this, poseStack, mouseX, mouseY);
    }

    public interface OnTooltip {
        void onTooltip(SizeableCheckbox checkbox, PoseStack poseStack, int mouseX, int mouseY);
    }
}
