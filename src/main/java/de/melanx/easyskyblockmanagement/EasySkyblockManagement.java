package de.melanx.easyskyblockmanagement;

import de.melanx.easyskyblockmanagement.network.EasyNetwork;
import de.melanx.skyblockbuilder.Registration;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.RegistrationBuilder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nonnull;

@Mod("easyskyblockmanagement")
public final class EasySkyblockManagement extends ModXRegistration {

    private static EasySkyblockManagement instance;
    private static EasyNetwork network;

    public EasySkyblockManagement() {
        super(new CreativeModeTab("easyskyblockmanagement") {
            @Nonnull
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Registration.structureSaver);
            }
        });

        instance = this;
        network = new EasyNetwork(this);
    }

    public static EasySkyblockManagement getInstance() {
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

    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        builder.setVersion(1);
    }
}
