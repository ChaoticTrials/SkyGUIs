package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.info.AllTeamsScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class OpenGui extends PacketHandler<OpenGui.Message> {

    public static final CustomPacketPayload.Type<OpenGui.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().id("open_gui"));

    public OpenGui() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        AllTeamsScreen.open();
    }

    public record Message() implements CustomPacketPayload {

        public static final StreamCodec<FriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, value) -> {
                },
                buffer -> new Message()
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return OpenGui.TYPE;
        }
    }
}
