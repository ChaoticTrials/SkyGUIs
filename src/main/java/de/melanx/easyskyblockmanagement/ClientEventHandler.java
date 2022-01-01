package de.melanx.easyskyblockmanagement;

import de.melanx.easyskyblockmanagement.client.screen.info.AllTeamsScreen;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event) {
        if (event.getKey() == Keybinds.ALL_TEAMS.getKey().getValue()) {
            AllTeamsScreen.open();
        }
    }
}
