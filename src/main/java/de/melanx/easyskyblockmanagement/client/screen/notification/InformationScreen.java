package de.melanx.easyskyblockmanagement.client.screen.notification;

import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.base.NotificationScreen;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class InformationScreen extends NotificationScreen {

    public InformationScreen(Component component, int xSize, int ySize, BaseScreen.OnConfirm onConfirm) {
        super(component, xSize, ySize, onConfirm);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.centeredX(60), this.y(60), 60, 20, ComponentBuilder.button("ok"), button -> {
            this.onConfirm.onConfirm();
        }));
    }
}
