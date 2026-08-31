package de.melanx.skyguis.client.widget;

import de.melanx.skyguis.util.Math2;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import org.moddingx.libx.screen.Panel;

import javax.annotation.Nonnull;

public class RenderArea extends Panel implements ScrollbarWidgetListener {

    private final int initX;
    private final int initY;
    private final int renderWidth;
    private final int renderHeight;
    private final int scrollOffset;

    public RenderArea(int x, int y, int renderWidth, int renderHeight, int totalWidth, int totalHeight) {
        this(x, y, renderWidth, renderHeight, totalWidth, totalHeight, 0);
    }

    public RenderArea(int x, int y, int renderWidth, int renderHeight, int totalWidth, int totalHeight, int scrollOffset) {
        super(x, y, totalWidth, totalHeight);
        this.initX = x;
        this.initY = y;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
        this.scrollOffset = scrollOffset;
    }

    public void translate(int x, int y) {
        this.setPos(this.x + x, this.y + y);
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    protected void extractChildren(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int x = this.initX - this.getX();
        int y = this.initY - this.getY();

        graphics.enableScissor(x, y, x + this.renderWidth, y + this.renderHeight);
        super.extractChildren(new ScreenSpaceGuiGraphicsExtractor(graphics), mouseX, mouseY, partialTicks);
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (Math2.isInBounds(this.initX, this.initY, this.renderWidth, this.renderHeight, event.x(), event.y())) {
            return super.mouseClicked(event, isDoubleClick);
        }

        return false;
    }

    public <T extends GuiEventListener & Renderable> T addRenderableWidget2(T widget) {
        return this.addRenderableWidget(widget);
    }

    public <T extends Renderable> T addRenderableOnly2(T widget) {
        return this.addRenderableOnly(widget);
    }

    public <T extends GuiEventListener> T addWidget2(T widget) {
        return this.addWidget(widget);
    }

    public int getInitX() {
        return this.initX;
    }

    public int getInitY() {
        return this.initY;
    }

    public int getRenderWidth() {
        return this.renderWidth;
    }

    public int getRenderHeight() {
        return this.renderHeight;
    }

    @Override
    public void onOffsetChanged(int oldOffset, int newOffset) {
        this.translate(0, (oldOffset - newOffset) * this.scrollOffset);
    }
}
