package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.SkyGUIs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public class RequestTemplateFromServer {

    public static void handle(Message msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> SkyGUIs.getNetwork().sendTemplateToClient(ctx, msg.name));
        ctx.setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.name);
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message(buffer.readUtf());
        }
    }

    public record Message(String name) {

    }
}
