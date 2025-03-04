package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.SkyGUIs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class RequestTemplateFromServer extends PacketHandler<RequestTemplateFromServer.Message> {

    public static final CustomPacketPayload.Type<RequestTemplateFromServer.Message> TYPE =
            new CustomPacketPayload.Type<>(SkyGUIs.getInstance().resource("request_template_from_server"));

    public RequestTemplateFromServer() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        SkyGUIs.getNetwork().sendTemplateToClient(ctx, msg.name());
    }

    public record Message(String name) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> buffer.writeUtf(msg.name)),
                buffer -> new Message(buffer.readUtf())
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return RequestTemplateFromServer.TYPE;
        }
    }
}