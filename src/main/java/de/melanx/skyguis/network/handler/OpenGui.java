package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.client.screen.info.AllTeamsScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record OpenGui() {

    public static class Handler implements PacketHandler<OpenGui> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(OpenGui msg, Supplier<NetworkEvent.Context> ctx) {
            AllTeamsScreen.open();
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<OpenGui> {

        @Override
        public Class<OpenGui> messageClass() {
            return OpenGui.class;
        }

        @Override
        public void encode(OpenGui msg, FriendlyByteBuf buffer) {

        }

        @Override
        public OpenGui decode(FriendlyByteBuf buffer) {
            return new OpenGui();
        }
    }
}
