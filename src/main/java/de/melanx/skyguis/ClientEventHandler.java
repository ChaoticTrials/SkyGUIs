package de.melanx.skyguis;

import de.melanx.skyguis.client.screen.info.AllTeamsScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event) {
        if (Minecraft.getInstance().screen == null) {
            while (Keybinds.ALL_TEAMS.consumeClick()) {
                AllTeamsScreen.open();
            }
        }
    }
}
