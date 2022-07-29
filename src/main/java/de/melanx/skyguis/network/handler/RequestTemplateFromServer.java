package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.SkyGUIs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record RequestTemplateFromServer(String name) {

    public static class Handler implements PacketHandler<RequestTemplateFromServer> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(RequestTemplateFromServer msg, Supplier<NetworkEvent.Context> ctx) {
            SkyGUIs.getNetwork().sendTemplateToClient(ctx.get(), msg.name);
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<RequestTemplateFromServer> {

        @Override
        public Class<RequestTemplateFromServer> messageClass() {
            return RequestTemplateFromServer.class;
        }

        @Override
        public void encode(RequestTemplateFromServer msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.name);
        }

        @Override
        public RequestTemplateFromServer decode(FriendlyByteBuf buffer) {
            return new RequestTemplateFromServer(buffer.readUtf());
        }
    }
}
