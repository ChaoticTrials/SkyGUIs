package de.melanx.skyguis.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyguis.util.Math2;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.opengl.GL11;
import org.moddingx.libx.screen.Panel;

import javax.annotation.Nonnull;

public class RenderArea extends Panel implements ScrollbarWidgetListener {

    private final int initX;
    private final int initY;
    private final int renderWidth;
    private final int renderHeight;
    private final int scrollOffset;

    public RenderArea(Screen screen, int x, int y, int renderWidth, int renderHeight, int totalWidth, int totalHeight) {
        this(screen, x, y, renderWidth, renderHeight, totalWidth, totalHeight, 0);
    }

    public RenderArea(Screen screen, int x, int y, int renderWidth, int renderHeight, int totalWidth, int totalHeight, int scrollOffset) {
        super(screen, x, y, totalWidth, totalHeight);
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
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        double guiScale = this.screen.getMinecraft().getWindow().getGuiScale();
        GL11.glScissor((int) (this.initX * guiScale), this.screen.getMinecraft().getWindow().getHeight() - (int) ((this.initY + this.renderHeight) * guiScale), (int) (this.renderWidth * guiScale), (int) (this.renderHeight * guiScale));
        super.render(poseStack, mouseX, mouseY, partialTick);
//        this.screen.renderBackground(poseStack);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math2.isInBounds(this.initX, this.initY, this.renderWidth, this.renderHeight, mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    public <T extends GuiEventListener & Widget> T addRenderableWidget2(T widget) {
        return this.addRenderableWidget(widget);
    }

    public <T extends Widget> T addRenderableOnly2(T widget) {
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
