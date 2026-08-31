package de.melanx.skyguis.client.screen.base.list;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.melanx.skyblockbuilder.client.SizeableCheckbox;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.widget.RenderArea;
import de.melanx.skyguis.client.widget.ScrollbarWidget;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.impl.config.gui.screen.widget.TextWidget;
import org.moddingx.libx.screen.Panel;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        this.renderArea = this.addWidget(new RenderArea(this.x(this.renderAreaInfo.x), this.y(this.renderAreaInfo.y), this.renderAreaInfo.width, this.entriesPerPage() * ENTRY_HEIGHT, this.xSize - 20, this.values.size() * ENTRY_HEIGHT, ENTRY_HEIGHT));

        this.fillWidgets();

        this.scrollbar.addListener(this.renderArea);
        this.updateScrollbar();
    }

    @SuppressWarnings("UnusedReturnValue")
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

    public void selectAll(boolean select) {
        for (CheckboxTextWidget widget : this.widgets) {
            widget.checkbox.selected = select;
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
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        this.renderTitle(graphics);
        this.scrollbar.extractRenderState(graphics, mouseX, mouseY, a);
        this.renderArea.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean mouseClicked(@Nonnull MouseButtonEvent event, boolean doubleClick) {
        return this.scrollbar.mouseClicked(event, doubleClick) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.scrollbar.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(@Nonnull MouseButtonEvent event) {
        return this.scrollbar.mouseReleased(event) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.scrollbar.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void updateScrollbar() {
        this.scrollbar.setEnabled(this.values.size() > this.entriesPerPage());
        this.scrollbar.setMaxOffset(this.values.size() - this.entriesPerPage());
    }

    public List<T> getValues() {
        return ImmutableList.copyOf(this.values);
    }

    public record ScrollbarInfo(int x, int y, int height) {}

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
            super(x, y, width, height);
            this.value = value;
            this.checkbox = new SizeableCheckbox(0, 0, height, false);
            this.addRenderableWidget(this.checkbox);
            TextWidget textWidget = new TextWidget(height + 5, 0, Math.min(width, TextHelper.stringLength(text)), height, text, tooltip);
            this.addRenderableWidget(textWidget);
        }

        public T getValue() {
            return this.value;
        }

        public boolean selected() {
            return this.checkbox.selected();
        }
    }
}
