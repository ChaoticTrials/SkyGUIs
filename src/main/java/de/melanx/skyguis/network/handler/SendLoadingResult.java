package de.melanx.skyguis.network.handler;

import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record SendLoadingResult(LoadingResult.Status status, Component reason) {

    public static class Handler implements PacketHandler<SendLoadingResult> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(SendLoadingResult msg, Supplier<NetworkEvent.Context> ctx) {
            if (Minecraft.getInstance().screen instanceof BaseScreen screen) {
                screen.setResult(new LoadingResult(msg.status, msg.reason));
            }
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<SendLoadingResult> {

        @Override
        public Class<SendLoadingResult> messageClass() {
            return SendLoadingResult.class;
        }

        @Override
        public void encode(SendLoadingResult msg, FriendlyByteBuf buffer) {
            buffer.writeEnum(msg.status);
            buffer.writeComponent(msg.reason);
        }

        @Override
        public SendLoadingResult decode(FriendlyByteBuf buffer) {
            return new SendLoadingResult(buffer.readEnum(LoadingResult.Status.class), buffer.readComponent());
        }
    }
}
