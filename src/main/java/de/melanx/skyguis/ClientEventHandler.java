package de.melanx.skyguis;

import de.melanx.skyguis.client.screen.info.AllTeamsScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event) {
        if (Minecraft.getInstance().screen == null && event.getKey() == Keybinds.ALL_TEAMS.getKey().getValue()) {
            AllTeamsScreen.open();
        }
    }
}
