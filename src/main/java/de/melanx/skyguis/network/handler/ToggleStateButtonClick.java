package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.util.ToggleButtons;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.UUID;
import java.util.function.Supplier;

public record ToggleStateButtonClick(UUID team, ToggleButtons.Type type) {

    public static class Handler implements PacketHandler<ToggleStateButtonClick> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(ToggleStateButtonClick msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            Level level = player.level();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeam(msg.team);
            if (team != null) {
                switch (msg.type) {
                    case VISITS -> team.setAllowVisit(!team.allowsVisits());
                    case JOIN_REQUEST -> team.setAllowJoinRequest(!team.allowsJoinRequests());
                }
            }

            return true;
        }
    }

    public static class Serializer implements PacketSerializer<ToggleStateButtonClick> {

        @Override
        public Class<ToggleStateButtonClick> messageClass() {
            return ToggleStateButtonClick.class;
        }

        @Override
        public void encode(ToggleStateButtonClick msg, FriendlyByteBuf buffer) {
            buffer.writeUUID(msg.team);
            buffer.writeEnum(msg.type);
        }

        @Override
        public ToggleStateButtonClick decode(FriendlyByteBuf buffer) {
            return new ToggleStateButtonClick(buffer.readUUID(), buffer.readEnum(ToggleButtons.Type.class));
        }
    }
}
