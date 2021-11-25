package de.melanx.easyskyblockmanagement.network.handler;

import com.google.common.collect.Sets;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import de.melanx.easyskyblockmanagement.util.LoadingResult;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

// TODO check config/events
public class UpdateTeam {

    public static void handle(UpdateTeam.Message msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }

            ServerLevel level = player.getLevel();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeamFromPlayer(player);
            if (team == null) {
                EasySkyblockManagement.getNetwork().handleLoadingResult(context.get(), LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.error.user_has_no_team"));
                return;
            }

            if (team.getName().equalsIgnoreCase(msg.teamName)) {
                for (UUID id : msg.players) {
                    data.removePlayerFromTeam(id);
                }
            } else {
                EasySkyblockManagement.getNetwork().handleLoadingResult(context.get(), LoadingResult.Status.FAIL, ComponentBuilder.text("player_not_in_team"));
            }
        });
        context.get().setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<UpdateTeam.Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.teamName);
            buffer.writeVarInt(msg.players.size());
            for (UUID id : msg.players) {
                buffer.writeUUID(id);
            }
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            String teamName = buffer.readUtf();
            int size = buffer.readVarInt();
            Set<UUID> ids = Sets.newHashSet();
            for (int i = 0; i < size; i++) {
                ids.add(buffer.readUUID());
            }

            return new Message(teamName, ids);
        }
    }

    public record Message(String teamName, Set<UUID> players) {
    }
}
