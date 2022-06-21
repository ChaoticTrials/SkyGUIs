package de.melanx.skyguis.client.screen.base.list;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.widget.RenderArea;
import de.melanx.skyguis.client.widget.ScrollbarWidget;
import de.melanx.skyguis.client.widget.sizable.SizeableCheckbox;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.impl.config.gui.screen.widget.TextWidget;
import org.moddingx.libx.screen.Panel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class ListScreen<T> extends BaseScreen {

    protected static final Component SELECT_ALL = ComponentBuilder.text("select_all");
    protected static final Component UNSELECT_ALL = ComponentBuilder.text("unselect_all");
    protected static final int ENTRY_HEIGHT = 14;

    private final ListScreen.ScrollbarInfo scrollbarInfo;
    private final ListScreen.RenderAreaInfo renderAreaInfo;
    private final Set<CheckboxTextWidget> widgets = new HashSet<>();
    protected final List<T> values;
    protected RenderArea renderArea;
    protected ScrollbarWidget scrollbar;

    // While rendering the scrollable view, tooltips must be delayed
    // Because scissors is enabled, and they need to be rendered with
    // absolute coordinates as they should not be cut by the screen border.
    private final List<Pair<PoseStack.Pose, Runnable>> capturedTooltips = new LinkedList<>();
    private boolean isCapturingTooltips = false;

    public ListScreen(Component title, Set<T> values, int xSize, int ySize, ListScreen.ScrollbarInfo scrollbarInfo, ListScreen.RenderAreaInfo renderAreaInfo) {
        this(title, values.stream().toList(), xSize, ySize, scrollbarInfo, renderAreaInfo);
    }

    public ListScreen(Component title, List<T> values, int xSize, int ySize, ListScreen.ScrollbarInfo scrollbarInfo, ListScreen.RenderAreaInfo renderAreaInfo) {
        super(title, xSize, ySize);
        this.values = values;
        this.scrollbarInfo = scrollbarInfo;
        this.renderAreaInfo = renderAreaInfo;
    }

    @Override
    protected void init() {
        this.widgets.clear();
        this.scrollbar = new ScrollbarWidget(this, this.scrollbarInfo.x, this.scrollbarInfo.y, 12, this.scrollbarInfo.height);
        this.renderArea = this.addWidget(new RenderArea(this, this.x(this.renderAreaInfo.x), this.y(this.renderAreaInfo.y), this.renderAreaInfo.width, this.entriesPerPage() * ENTRY_HEIGHT, this.xSize - 20, this.values.size() * ENTRY_HEIGHT, ENTRY_HEIGHT) {
            @Override
            public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                ListScreen.this.isCapturingTooltips = true;
                super.render(poseStack, mouseX, mouseY, partialTick);
                ListScreen.this.isCapturingTooltips = false;
                ListScreen.this.capturedTooltips.forEach(pair -> {
                    int x = mouseX - this.getInitX();
                    int y = mouseY - this.getInitY();
                    if (x > 0 && x < this.getRenderWidth()
                            && y > 0 && y < this.getRenderHeight()) {
                        poseStack.pushPose();
                        poseStack.setIdentity();
                        poseStack.mulPoseMatrix(pair.getLeft().pose());
                        pair.getRight().run();
                        poseStack.popPose();
                    }
                });
                ListScreen.this.capturedTooltips.clear();
            }
        });

        this.fillWidgets();

        this.scrollbar.addListener(this.renderArea);
        this.updateScrollbar();
    }

    protected CheckboxTextWidget addCheckboxWidget(CheckboxTextWidget widget) {
        this.widgets.add(widget);
        return widget;
    }

    protected abstract int entriesPerPage();

    protected abstract void fillWidgets();

    public boolean allSelected() {
        for (CheckboxTextWidget widget : this.widgets) {
            if (!widget.selected()) {
                return false;
            }
        }

        return true;
    }

    public void selectAll() {
        for (CheckboxTextWidget widget : this.widgets) {
            widget.checkbox.selected = true;
        }
    }

    public void unselectAll() {
        for (CheckboxTextWidget widget : this.widgets) {
            widget.checkbox.selected = false;
        }
    }

    public Set<T> getSelectedValues() {
        Set<T> ids = new HashSet<>();
        for (CheckboxTextWidget widget : this.widgets) {
            if (widget.selected()) {
                ids.add(widget.getValue());
            }
        }

        return ids;
    }

    @Override
    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render_(poseStack, mouseX, mouseY, partialTick);
        this.renderTitle(poseStack);
        this.scrollbar.render(poseStack);
        this.renderArea.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.scrollbar.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.scrollbar.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.scrollbar.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.scrollbar.mouseScrolled(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void updateScrollbar() {
        this.scrollbar.setEnabled(this.values.size() > this.entriesPerPage());
        this.scrollbar.setMaxOffset(this.values.size() - this.entriesPerPage());
    }

    public List<T> getValues() {
        return ImmutableList.copyOf(this.values);
    }

    public record ScrollbarInfo(int x, int y, int height) {
    }

    public record RenderAreaInfo(int x, int y, int width, int height) {

        public RenderAreaInfo(int x, int y, int width) {
            this(x, y, width, 0);
        }
    }

    protected class CheckboxTextWidget extends Panel {

        protected final T value;
        protected final Checkbox checkbox;

        public CheckboxTextWidget(T value, Screen screen, int x, int y, int width, int height, Component text) {
            this(value, screen, x, y, width, height, Lists.newArrayList(), text);
        }

        public CheckboxTextWidget(T value, Screen screen, int x, int y, int width, int height, List<Component> tooltip, Component text) {
            super(screen, x, y, width, height);
            this.value = value;
            this.checkbox = new SizeableCheckbox(0, 0, height, false);
            this.addRenderableWidget(this.checkbox);
            TextWidget textWidget = new TextWidget(screen, height + 5, 0, Math.min(width, TextHelper.stringLength(text)), height, text, tooltip);
            this.addRenderableWidget(textWidget);
        }

        public T getValue() {
            return this.value;
        }

        public boolean selected() {
            return this.checkbox.selected();
        }
    }

    private void captureTooltip(PoseStack.Pose pose, Runnable action) {
        this.capturedTooltips.add(Pair.of(pose, action));
    }

    @Override
    protected void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull ItemStack stack, int x, int y) {
        if (this.isCapturingTooltips) {
            // Not inside lambda as the value may change
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, stack, x, y));
        } else {
            super.renderTooltip(poseStack, stack, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> special, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, special, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, special, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull Component tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedCharSequence> tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> tooltipComponent, int x, int y, @Nonnull ItemStack stack) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, stack));
        } else {
            super.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, stack);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> tooltipComponent, int x, int y, @Nullable Font font) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, font));
        } else {
            super.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, font);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> tooltipComponent, int x, int y, @Nullable Font font, @Nonnull ItemStack stack) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, font, stack));
        } else {
            super.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, font, stack);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedText> tooltip, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, stack));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, stack);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedText> tooltip, int mouseX, int mouseY, @Nullable Font font) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, font));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, font);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedText> tooltip, int mouseX, int mouseY, @Nullable Font font, @Nonnull ItemStack stack) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, font, stack));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, font, stack);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedCharSequence> tooltip, int x, int y, @Nonnull Font font) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, x, y, font));
        } else {
            super.renderTooltip(poseStack, tooltip, x, y, font);
        }
    }
}
