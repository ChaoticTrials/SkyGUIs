package de.melanx.skyguis.client.screen.notification;

import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.NotificationScreen;
import de.melanx.skyguis.client.widget.LoadingCircle;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class YouSureScreen extends NotificationScreen {

    private static final Component CONFIRM = ComponentBuilder.button("confirm");
    private static final Component ABORT = ComponentBuilder.button("abort");
    private final BaseScreen parent;

    public YouSureScreen(BaseScreen parent, Component title, BaseScreen.OnConfirm onConfirm) {
        this(parent, title.copy().withStyle(ChatFormatting.RED), onConfirm, BaseScreen.DEFAULT_ABORT);
    }

    public YouSureScreen(BaseScreen parent, Component title, BaseScreen.OnConfirm onConfirm, BaseScreen.OnAbort onAbort) {
        super(title, TextHelper.stringLength(title) + 30, 80, onConfirm, onAbort);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.centeredX(50) - 30, this.y(45), 50, 20, CONFIRM, (button -> {
            this.onConfirm.onConfirm();
            LoadingCircle loadingCircle = this.parent.getLoadingCircle();
            if (loadingCircle != null) {
                loadingCircle.setActive(true);
            }
            this.parent.onClose();
        })));
        this.addRenderableWidget(new Button(this.centeredX(50) + 30, this.y(45), 50, 20, ABORT, (button -> {
            this.onAbort.onAbort();
            this.onClose();
        })));
    }
}
