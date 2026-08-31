package de.melanx.skyguis.client.widget;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2f;
import org.moddingx.libx.render.FilterGuiGraphicsExtractor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ScreenSpaceGuiGraphicsExtractor extends FilterGuiGraphicsExtractor {

    private final Vector2f scratch = new Vector2f();

    public ScreenSpaceGuiGraphicsExtractor(GuiGraphicsExtractor parent) {
        super(parent);
    }

    private Vector2f toScreenSpace(int x, int y) {
        return this.pose().transformPosition(x, y, this.scratch);
    }

    @Override
    public boolean containsPointInScissor(int x, int y) {
        Vector2f pos = this.toScreenSpace(x, y);
        return this.parent.containsPointInScissor((int) pos.x, (int) pos.y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Component component, int x, int y) {
        Vector2f pos = this.toScreenSpace(x, y);
        this.parent.setTooltipForNextFrame(component, (int) pos.x, (int) pos.y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull List<FormattedCharSequence> formattedCharSequences, int x, int y) {
        Vector2f pos = this.toScreenSpace(x, y);
        this.parent.setTooltipForNextFrame(formattedCharSequences, (int) pos.x, (int) pos.y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull ItemStack itemStack, int xo, int yo) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, itemStack, (int) pos.x, (int) pos.y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> textComponents, @Nonnull Optional<TooltipComponent> tooltipComponent, @Nonnull ItemStack stack, int mouseX, int mouseY) {
        Vector2f pos = this.toScreenSpace(mouseX, mouseY);
        this.parent.setTooltipForNextFrame(font, textComponents, tooltipComponent, stack, (int) pos.x, (int) pos.y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> textComponents, @Nonnull Optional<TooltipComponent> tooltipComponent, @Nonnull ItemStack stack, int mouseX, int mouseY, @Nullable Identifier backgroundTexture) {
        Vector2f pos = this.toScreenSpace(mouseX, mouseY);
        this.parent.setTooltipForNextFrame(font, textComponents, tooltipComponent, stack, (int) pos.x, (int) pos.y, backgroundTexture);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> texts, @Nonnull Optional<TooltipComponent> optionalImage, int xo, int yo) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, texts, optionalImage, (int) pos.x, (int) pos.y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> texts, @Nonnull Optional<TooltipComponent> optionalImage, int xo, int yo, @Nullable Identifier style) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, texts, optionalImage, (int) pos.x, (int) pos.y, style);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<FormattedCharSequence> tooltip, @Nonnull Optional<TooltipComponent> component, @Nonnull ClientTooltipPositioner positioner, int xo, int yo, boolean replaceExisting, @Nullable Identifier style) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, tooltip, component, positioner, (int) pos.x, (int) pos.y, replaceExisting, style);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull Component text, int xo, int yo) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, text, (int) pos.x, (int) pos.y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull Component text, int xo, int yo, @Nullable Identifier style) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, text, (int) pos.x, (int) pos.y, style);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedCharSequence> lines, int xo, int yo) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, lines, (int) pos.x, (int) pos.y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedCharSequence> lines, int xo, int yo, @Nullable Identifier style) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, lines, (int) pos.x, (int) pos.y, style);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<FormattedCharSequence> tooltip, @Nonnull ClientTooltipPositioner positioner, int xo, int yo, boolean replaceExisting) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setTooltipForNextFrame(font, tooltip, positioner, (int) pos.x, (int) pos.y, replaceExisting);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> lines, int xo, int yo) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setComponentTooltipForNextFrame(font, lines, (int) pos.x, (int) pos.y);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> lines, int xo, int yo, @Nullable Identifier style) {
        Vector2f pos = this.toScreenSpace(xo, yo);
        this.parent.setComponentTooltipForNextFrame(font, lines, (int) pos.x, (int) pos.y, style);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        Vector2f pos = this.toScreenSpace(mouseX, mouseY);
        this.parent.setComponentTooltipForNextFrame(font, tooltips, (int) pos.x, (int) pos.y, stack);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nonnull ItemStack stack, @Nullable Identifier backgroundTexture) {
        Vector2f pos = this.toScreenSpace(mouseX, mouseY);
        this.parent.setComponentTooltipForNextFrame(font, tooltips, (int) pos.x, (int) pos.y, stack, backgroundTexture);
    }

    @Override
    public void setComponentTooltipFromElementsForNextFrame(@Nonnull Font font, @Nonnull List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        Vector2f pos = this.toScreenSpace(mouseX, mouseY);
        this.parent.setComponentTooltipFromElementsForNextFrame(font, elements, (int) pos.x, (int) pos.y, stack);
    }

    @Override
    public void setComponentTooltipFromElementsForNextFrame(@Nonnull Font font, @Nonnull List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int mouseY, @Nonnull ItemStack stack, @Nullable Identifier backgroundTexture) {
        Vector2f pos = this.toScreenSpace(mouseX, mouseY);
        this.parent.setComponentTooltipFromElementsForNextFrame(font, elements, (int) pos.x, (int) pos.y, stack, backgroundTexture);
    }
}
