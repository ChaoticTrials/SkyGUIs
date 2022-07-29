package de.melanx.skyguis;

import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.network.handler.OpenGui;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.RegistrationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("skyguis")
public final class SkyGUIs extends ModXRegistration {

    private static SkyGUIs instance;
    private final EasyNetwork network;
    public final Logger logger;

    public SkyGUIs() {
        instance = this;
        this.network = new EasyNetwork(this);
        this.logger = LoggerFactory.getLogger(this.modid);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        });

        MinecraftForge.EVENT_BUS.addListener(SkyGUIs::registerClientCommands);
    }

    public static SkyGUIs getInstance() {
        return instance;
    }

    public static EasyNetwork getNetwork() {
        return instance.network;
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {

    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {

    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {

    }

    public static void registerClientCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skyblock")
                .then(Commands.literal("gui").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    SkyGUIs.getNetwork().channel.sendTo(new OpenGui(), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    return 1;
                }))
        );
    }
}
