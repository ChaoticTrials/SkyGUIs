package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyguis.SkyGUIs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class UpdateSkyblockSavedData extends PacketHandler<UpdateSkyblockSavedData.Message> {

    public static final CustomPacketPayload.Type<UpdateSkyblockSavedData.Message> TYPE =
            new CustomPacketPayload.Type<>(SkyGUIs.getInstance().id("update_skyblock_saved_data"));

    public UpdateSkyblockSavedData() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        SkyblockBuilder.getNetwork().updateData(ctx.player(), null);
    }

    public record Message() implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    // No data to encode
                },
                buffer -> new Message() // No data to decode
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return UpdateSkyblockSavedData.TYPE;
        }
    }
}
