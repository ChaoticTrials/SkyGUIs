package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.client.screen.info.AllTeamsScreen;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenGui {

    public static void handle(Message msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(AllTeamsScreen::open);
        ctx.setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {

        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message();
        }
    }

    public record Message() {
    }
}
