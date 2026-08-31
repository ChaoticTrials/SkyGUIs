package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class SendLoadingResult extends PacketHandler<SendLoadingResult.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().id("send_loading_result"));

    public SendLoadingResult() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        switch (msg.status) {
            case SUCCESS -> {
                Minecraft.getInstance().popGuiLayer();
                ctx.player().sendSystemMessage(msg.reason.copy().withStyle(ChatFormatting.GOLD));
            }
            case FAIL -> {
                InformationScreen.open(msg.reason);
            }
        }
    }

    public record Message(LoadingResult.Status status, Component reason) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    buffer.writeEnum(msg.status);
                    ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, msg.reason);
                }, buffer -> new SendLoadingResult.Message(buffer.readEnum(LoadingResult.Status.class), ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer))
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SendLoadingResult.TYPE;
        }
    }
}
