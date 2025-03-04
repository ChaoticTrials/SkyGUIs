package de.melanx.skyguis.client.screen.notification;

import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.NotificationScreen;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class InformationScreen extends NotificationScreen {

    public InformationScreen(Component component, int xSize, int ySize, BaseScreen.OnConfirm onConfirm) {
        super(component, xSize, ySize, onConfirm);
    }

    public static void open(Component component) {
        Minecraft.getInstance().pushGuiLayer(new InformationScreen(component, TextHelper.stringLength(component) + 30, 100, Minecraft.getInstance()::popGuiLayer));
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(ComponentBuilder.button("ok"), button -> this.onConfirm.onConfirm())
                .bounds(this.centeredX(60), this.y(60), 60, 20)
                .build()).setFocused(true);
    }
}
