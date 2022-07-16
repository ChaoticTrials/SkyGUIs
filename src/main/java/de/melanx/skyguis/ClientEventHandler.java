package de.melanx.skyguis;

import de.melanx.skyguis.client.screen.info.AllTeamsScreen;
import de.melanx.skyguis.tooltip.ClientSmallTextTooltip;
import de.melanx.skyguis.tooltip.SmallTextTooltip;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientEventHandler {

    public ClientEventHandler() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCustomTooltipComponents);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeys);
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.Key event) {
        if (Minecraft.getInstance().screen == null && event.getKey() == Keybinds.ALL_TEAMS.getKey().getValue()) {
            AllTeamsScreen.open();
        }
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.MouseButton event) {
        if (Minecraft.getInstance().screen == null && event.getButton() == Keybinds.ALL_TEAMS.getKey().getValue()) {
            AllTeamsScreen.open();
        }
    }

    public void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.ALL_TEAMS);
    }

    public void registerCustomTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(SmallTextTooltip.class, ClientSmallTextTooltip::new);
    }
}
