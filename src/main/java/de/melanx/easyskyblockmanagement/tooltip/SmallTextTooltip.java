package de.melanx.easyskyblockmanagement.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.awt.Color;
import java.util.List;

public class SmallTextTooltip implements TooltipComponent {

    private final List<Component> tooltips;
    private final int color;

    public SmallTextTooltip(List<Component> tooltips, Color color) {
        this.tooltips = tooltips;
        this.color = color.getRGB();
    }

    public List<Component> getTooltips() {
        return this.tooltips;
    }

    public int getColor() {
        return this.color;
    }
}
