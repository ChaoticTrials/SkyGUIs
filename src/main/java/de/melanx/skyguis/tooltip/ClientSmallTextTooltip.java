package de.melanx.skyguis.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class ClientSmallTextTooltip implements ClientTooltipComponent {

    private static final int LINE_HEIGHT = 8;

    private final List<Component> tooltips;
    private final int color;

    public ClientSmallTextTooltip(SmallTextTooltip tooltip) {
        this.tooltips = tooltip.getTooltips();
        this.color = tooltip.getColor();
    }

    @Override
    public void extractText(@Nonnull GuiGraphicsExtractor graphics, @Nonnull Font font, int x, int y) {
        boolean unicode = Minecraft.getInstance().isEnforceUnicode();
        float textScale = unicode ? 1 : 0.7F;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y - (unicode ? 2 : 0));
        graphics.pose().scale(textScale, textScale);
        int i = 0;
        for (Component tooltip : this.tooltips) {
            graphics.text(font, tooltip, 0, Math.round(i * LINE_HEIGHT / textScale), this.color, true);
            i++;
        }

        graphics.pose().popMatrix();
    }

    @Override
    public int getHeight(@Nonnull Font font) {
        return this.tooltips.size() * LINE_HEIGHT;
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
