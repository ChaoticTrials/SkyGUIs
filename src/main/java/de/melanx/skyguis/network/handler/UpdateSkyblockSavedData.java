package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record UpdateSkyblockSavedData() {

    public static class Handler implements PacketHandler<UpdateSkyblockSavedData> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(UpdateSkyblockSavedData msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            SkyblockBuilder.getNetwork().updateData(player, SkyblockSavedData.get(player.level()));
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<UpdateSkyblockSavedData> {

        @Override
        public Class<UpdateSkyblockSavedData> messageClass() {
            return UpdateSkyblockSavedData.class;
        }

        @Override
        public void encode(UpdateSkyblockSavedData msg, FriendlyByteBuf buffer) {

        }

        @Override
        public UpdateSkyblockSavedData decode(FriendlyByteBuf buffer) {
            return new UpdateSkyblockSavedData();
        }
    }
}
