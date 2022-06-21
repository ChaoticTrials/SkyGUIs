package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public class SendLoadingResult {

    public static void handle(Message msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof BaseScreen screen) {
                screen.setResult(new LoadingResult(msg.status, msg.reason));
            }
        });
        context.get().setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeEnum(msg.status);
            buffer.writeComponent(msg.reason);
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message(buffer.readEnum(LoadingResult.Status.class), buffer.readComponent());
        }
    }

    public record Message(LoadingResult.Status status, Component reason) {
    }
}
