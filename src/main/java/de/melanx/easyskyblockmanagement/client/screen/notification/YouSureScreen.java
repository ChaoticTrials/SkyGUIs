package de.melanx.easyskyblockmanagement.client.screen.notification;

import de.melanx.easyskyblockmanagement.TextHelper;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.base.NotificationScreen;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class YouSureScreen extends NotificationScreen {

    private static final Component CONFIRM = ComponentBuilder.button("confirm");
    private static final Component ABORT = ComponentBuilder.button("abort");

    public YouSureScreen(Component component, BaseScreen.OnConfirm onConfirm) {
        this(component.copy().withStyle(ChatFormatting.RED), onConfirm, BaseScreen.DEFAULT_ABORT);
    }

    public YouSureScreen(Component component, BaseScreen.OnConfirm onConfirm, BaseScreen.OnAbort onAbort) {
        super(component, TextHelper.stringLength(component) + 30, 80, onConfirm, onAbort);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.centeredX(50) - 30, this.y(45), 50, 20, CONFIRM, (button -> {
            this.onConfirm.onConfirm();
        })));
        this.addRenderableWidget(new Button(this.centeredX(50) + 30, this.y(45), 50, 20, ABORT, (button -> {
            this.onAbort.onAbort();
        })));
    }
}
