package de.melanx.skyguis.client.widget;

import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.util.Math2;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class ScrollbarWidget implements GuiEventListener, Renderable {

    private static final int SCROLLER_HEIGHT = 15;
    private static final ResourceLocation ICONS = new ResourceLocation(SkyGUIs.getInstance().modid, "textures/gui/icons.png");

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final List<ScrollbarWidgetListener> listeners = new LinkedList<>();
    private final BaseScreen screen;
    private boolean enabled;
    private boolean focused;

    private int offset;
    private int maxOffset;

    private boolean clicked = false;


    public ScrollbarWidget(BaseScreen screen, int x, int y, int width, int height) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addListener(ScrollbarWidgetListener listener) {
        this.listeners.add(listener);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(ICONS, this.screen.getRelX() + this.x - 1, this.screen.getRelY() + this.y - 1, this.width + 2, 1, 0, 15, 14, 1, 256, 256);
        guiGraphics.blit(ICONS, this.screen.getRelX() + this.x - 1, this.screen.getRelY() + this.y, this.width + 2, this.height, 0, 17, 14, 12, 256, 256);
        guiGraphics.blit(ICONS, this.screen.getRelX() + this.x - 1, this.screen.getRelY() + this.y + this.height, this.width + 2, 1, 0, 31, 14, 1, 256, 256);
        guiGraphics.blit(ICONS, this.screen.getRelX() + this.x, this.screen.getRelY() + this.y + (int) Math.min(this.height - SCROLLER_HEIGHT, (float) this.offset / (float) this.maxOffset * (float) (this.height - SCROLLER_HEIGHT)), this.enabled ? 0 : 12, 0, 12, 15);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        mouseX -= this.screen.getRelX();
        mouseY -= this.screen.getRelY();

        if (this.clicked && Math2.isInBounds(this.x, this.y, this.width, this.height, mouseX, mouseY)) {
            this.updateOffset(mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX -= this.screen.getRelX();
        mouseY -= this.screen.getRelY();

        if (button == 0 && Math2.isInBounds(this.x, this.y, this.width, this.height, mouseX, mouseY)) {
            this.updateOffset(mouseY);

            this.clicked = true;

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.clicked) {
            this.clicked = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (this.enabled) {
            this.setOffset(this.offset + Math.max(Math.min(-(int) scrollDelta, 1), -1));

            return true;
        }

        return false;
    }

    public void updateOffset(double offset) {
        this.setOffset((int) Math.floor((float) (offset - this.y) / (float) (this.height - SCROLLER_HEIGHT) * (float) this.maxOffset));
    }

    public void setMaxOffset(int maxOffset) {
        this.maxOffset = maxOffset;

        if (this.offset > maxOffset) {
            this.offset = Math.max(0, maxOffset);
        }
    }

    public void setOffset(int offset) {
        int oldOffset = this.offset;

        if (offset >= 0 && offset <= this.maxOffset) {
            this.offset = offset;

            this.listeners.forEach(l -> l.onOffsetChanged(oldOffset, offset));
        }
    }

    public int getOffset() {
        return this.offset;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }
}
