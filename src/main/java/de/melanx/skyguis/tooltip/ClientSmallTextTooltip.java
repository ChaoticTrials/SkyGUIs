package de.melanx.skyguis.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.util.List;

public class ClientSmallTextTooltip implements ClientTooltipComponent {

    private final List<Component> tooltips;
    private final int color;

    public ClientSmallTextTooltip(SmallTextTooltip tooltip) {
        this.tooltips = tooltip.getTooltips();
        this.color = tooltip.getColor();
    }

    @Override
    public void renderText(@Nonnull Font font, int x, int y, @Nonnull Matrix4f matrix, @Nonnull MultiBufferSource.BufferSource bufferSource) {
        float textScale = Minecraft.getInstance().isEnforceUnicode() ? 1 : 0.7F;
        int i = 0;
        for (Component tooltip : this.tooltips) {
            PoseStack poseStack = new PoseStack();
            poseStack.translate(0, 0, 400);
            poseStack.scale(textScale, textScale, 1);
            font.drawInBatch(tooltip,
                    x / textScale,
                    (y - (Minecraft.getInstance().isEnforceUnicode() ? 2 : 0) + i * 8) / textScale,
                    this.color,
                    true,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    LightTexture.FULL_BRIGHT
            );
            i++;
        }
    }

    @Override
    public int getHeight() {
        return (this.tooltips.size() * 8);
    }

    @Override
    public int getWidth(@Nonnull Font font) {
        int w = -1;
        float textScale = Minecraft.getInstance().isEnforceUnicode() ? 1 : 0.7F;
        for (Component tooltip : this.tooltips) {
            int width = (int) (font.width(tooltip) * textScale);
            if (width > w) {
                w = width;
            }
        }

        return w;
    }
}
