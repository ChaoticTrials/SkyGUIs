package de.melanx.skyguis;

import de.melanx.skyguis.client.screen.info.AllTeamsScreen;
import de.melanx.skyguis.tooltip.ClientSmallTextTooltip;
import de.melanx.skyguis.tooltip.SmallTextTooltip;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class ClientEventHandler {

    public ClientEventHandler(IEventBus modBus) {
        modBus.addListener(this::registerCustomTooltipComponents);
        modBus.addListener(this::registerKeys);
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.Key event) {
        if (Minecraft.getInstance().screen == null) {
            while (Keybinds.ALL_TEAMS.consumeClick()) {
                AllTeamsScreen.open();
            }
        }
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().screen == null) {
            while (Keybinds.ALL_TEAMS.consumeClick()) {
                AllTeamsScreen.open();
            }
        }
    }

    public void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.ALL_TEAMS);
    }

    public void registerCustomTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(SmallTextTooltip.class, ClientSmallTextTooltip::new);
    }
}
