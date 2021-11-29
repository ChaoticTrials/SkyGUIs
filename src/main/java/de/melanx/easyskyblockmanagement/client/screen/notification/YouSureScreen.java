package de.melanx.easyskyblockmanagement.client.screen.notification;

import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.base.NotificationScreen;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import de.melanx.easyskyblockmanagement.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class YouSureScreen extends NotificationScreen {

    private static final Component CONFIRM = ComponentBuilder.button("confirm");
    private static final Component ABORT = ComponentBuilder.button("abort");

    public YouSureScreen(Component title, BaseScreen.OnConfirm onConfirm) {
        this(title.copy().withStyle(ChatFormatting.RED), onConfirm, BaseScreen.DEFAULT_ABORT);
    }

    public YouSureScreen(Component title, BaseScreen.OnConfirm onConfirm, BaseScreen.OnAbort onAbort) {
        super(title, TextHelper.stringLength(title) + 30, 80, onConfirm, onAbort);
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
