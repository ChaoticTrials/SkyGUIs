package de.melanx.easyskyblockmanagement.client.screen.notification;

import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.base.NotificationScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class InformationScreen extends NotificationScreen {

    public InformationScreen(Component component, int xSize, int ySize, BaseScreen.OnConfirm onConfirm) {
        super(component, xSize, ySize, onConfirm);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.centeredX(60), this.y(60), 60, 20, new TranslatableComponent("Confirm"), button -> {
            this.onConfirm.onConfirm();
        }));
    }
}
