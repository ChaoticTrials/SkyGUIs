package de.melanx.skyguis;

import de.melanx.skyblockbuilder.Registration;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.tooltip.ClientSmallTextTooltip;
import de.melanx.skyguis.tooltip.SmallTextTooltip;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.RegistrationBuilder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nonnull;

@Mod("skyguis")
public final class SkyGUIs extends ModXRegistration {

    private static SkyGUIs instance;
    private static EasyNetwork network;

    public SkyGUIs() {
        super(new CreativeModeTab("skyguis") {
            @Nonnull
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Registration.structureSaver);
            }
        });

        instance = this;
        network = new EasyNetwork(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientRegistry.registerKeyBinding(Keybinds.ALL_TEAMS);
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        });
        MinecraftForge.EVENT_BUS.register(new EventHandler());
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
}
