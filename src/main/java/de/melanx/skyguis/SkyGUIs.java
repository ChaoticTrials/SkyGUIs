package de.melanx.skyguis;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.network.handler.OpenGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModXRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SkyGUIs.MODID)
public final class SkyGUIs extends ModXRegistration {

    private static SkyGUIs instance;
    private final EasyNetwork network;
    public final Logger logger;
    public static final String MODID = "skyguis";

    public SkyGUIs(IEventBus modBus, Dist dist) {
        instance = this;
        this.network = new EasyNetwork(this);
        this.logger = LoggerFactory.getLogger(this.modid);

        if (dist.isClient()) {
            NeoForge.EVENT_BUS.register(new ClientEventHandler(modBus));
        }

        NeoForge.EVENT_BUS.addListener(SkyGUIs::registerClientCommands);
    }

    public static SkyGUIs getInstance() {
        return instance;
    }

    public static EasyNetwork getNetwork() {
        return instance.network;
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        // NO-OP
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        // NO-OP
    }

    public static void registerClientCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> guiCommand = Commands.literal("gui").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            PacketDistributor.sendToPlayer(player, new OpenGui.Message());
            return 1;
        });

        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();

        commandDispatcher.register(Commands.literal("skyblock").then(guiCommand));
        commandDispatcher.register(Commands.literal("sky").then(guiCommand));
    }
}
