package de.melanx.skyguis.client.screen.notification;

import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.NotificationScreen;
import de.melanx.skyguis.client.widget.LoadingCircle;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Collection;
import java.util.List;

public class YouSureScreen extends NotificationScreen {

    private static final Component CONFIRM = ComponentBuilder.button("confirm");
    private static final Component ABORT = ComponentBuilder.button("abort");
    private final BaseScreen parent;
    private final List<Component> components;

    public YouSureScreen(BaseScreen parent, Component title, BaseScreen.OnConfirm onConfirm) {
        this(parent, title.copy().withStyle(ChatFormatting.RED), onConfirm, BaseScreen.DEFAULT_ABORT);
    }

    public YouSureScreen(BaseScreen parent, List<Component> components, BaseScreen.OnConfirm onConfirm) {
        this(parent, components.stream().map(component -> (Component) component.copy().withStyle(ChatFormatting.RED)).toList(), onConfirm, BaseScreen.DEFAULT_ABORT);
    }

    public YouSureScreen(BaseScreen parent, Component title, BaseScreen.OnConfirm onConfirm, BaseScreen.OnAbort onAbort) {
        this(parent, List.of(title), onConfirm, onAbort);
    }

    public YouSureScreen(BaseScreen parent, List<Component> components, BaseScreen.OnConfirm onConfirm, BaseScreen.OnAbort onAbort) {
        super(components.get(0), YouSureScreen.longestComponent(components) + 30, 80 + ((components.size() - 1) * 16), onConfirm, onAbort);
        this.parent = parent;
        this.components = components;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CONFIRM, (button -> {
                    this.onConfirm.onConfirm();
                    LoadingCircle loadingCircle = this.parent.getLoadingCircle();
                    if (loadingCircle != null) {
                        loadingCircle.setActive(true);
                    }
                    this.parent.onClose();
                }))
                .bounds(this.centeredX(50) - 30, this.y(this.ySize - 35), 50, 20)
                .build());
        this.addRenderableWidget(Button.builder(ABORT, (button -> {
                    this.onAbort.onAbort();
                    this.onClose();
                }))
                .bounds(this.centeredX(50) + 30, this.y(this.ySize - 35), 50, 20)
                .build());
    }

    @Override
    public void render_(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        RenderHelper.renderGuiBackground(guiGraphics, this.relX, this.relY, this.xSize, this.ySize, BaseScreen.GENERIC, 128, 64, 4, 125, 4, 60);

        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        for (int i = 0; i < this.components.size(); i++) {
            Component component = this.components.get(i);
            guiGraphics.drawString(this.font, component, this.centeredX(this.font.width(component.getVisualOrderText())), this.y((i + 1) * 16), Color.DARK_GRAY.getRGB(), false);
        }
    }

    private static int longestComponent(Collection<Component> components) {
        int longest = 0;
        for (Component component : components) {
            longest = Math.max(longest, TextHelper.stringLength(component));
        }

        return longest;
    }
}
