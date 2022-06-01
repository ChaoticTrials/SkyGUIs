package de.melanx.skyguis;

import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.network.handler.OpenGui;
import de.melanx.skyguis.tooltip.ClientSmallTextTooltip;
import de.melanx.skyguis.tooltip.SmallTextTooltip;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.RegistrationBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("skyguis")
public final class SkyGUIs extends ModXRegistration {

    private static SkyGUIs instance;
    private static EasyNetwork network;
    public final Logger logger;

    public SkyGUIs() {
        instance = this;
        network = new EasyNetwork(this);
        this.logger = LoggerFactory.getLogger(this.modid);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientRegistry.registerKeyBinding(Keybinds.ALL_TEAMS);
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        });

        MinecraftForge.EVENT_BUS.addListener(SkyGUIs::registerClientCommands);
    }

    public static SkyGUIs getInstance() {
        return instance;
    }

    public static EasyNetwork getNetwork() {
        return network;
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {

    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        MinecraftForgeClient.registerTooltipComponentFactory(SmallTextTooltip.class, ClientSmallTextTooltip::new);
    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        builder.setVersion(1);
    }

    public static void registerClientCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skyblock")
                .then(Commands.literal("gui").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    network.channel.sendTo(new OpenGui.Message(), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    return 1;
                }))
        );
    }
}
