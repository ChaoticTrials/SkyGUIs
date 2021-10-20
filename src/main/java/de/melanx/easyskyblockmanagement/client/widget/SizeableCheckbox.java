package de.melanx.easyskyblockmanagement.client.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class SizeableCheckbox extends Checkbox {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");

    public SizeableCheckbox(int x, int y, int size, Component message, boolean selected) {
        super(x, y, size, size, message, selected, false);
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        poseStack.pushPose();
        poseStack.scale(this.width / 20f, this.height / 20f, 1);
        blit(poseStack, this.x, this.y, this.isFocused() ? 20.0F : 0.0F, this.selected() ? 20.0F : 0.0F, 20, 20, 64, 64);
        poseStack.popPose();
        this.renderBg(poseStack, minecraft, mouseX, mouseY);
    }
}
