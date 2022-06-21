package de.melanx.skyguis.network.handler;

import com.google.common.collect.Sets;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.network.PacketSerializer;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UpdateTeam {

    public static void handle(Message msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            if (!player.hasPermissions(2)) {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, ComponentBuilder.text("missing_permissions"));
                return;
            }

            ServerLevel level = player.getLevel();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeamFromPlayer(player);
            if (team == null) {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_no_team"));
                return;
            }

            PlayerList playerList = level.getServer().getPlayerList();
            Pair<Boolean, Set<ServerPlayer>> result = SkyblockHooks.onManageRemoveFromTeam(null, team, msg.players.stream().map(playerList::getPlayer).collect(Collectors.toList()));
            if (result.getLeft()) {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.remove_players_from_team"));
                return;
            }

            if (team.getName().equalsIgnoreCase(msg.teamName)) {
                for (UUID id : msg.players) {
                    if (team.hasPlayer(id)) {
                        data.removePlayerFromTeam(id);
                        ServerPlayer toRemove = playerList.getPlayer(id);
                        if (toRemove != null) {
                            if (ConfigHandler.Inventory.dropItems) {
                                RandomUtility.dropInventories(toRemove);
                            }
                            WorldUtil.teleportToIsland(toRemove, data.getSpawn());
                        }
                    }
                }
            } else {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, ComponentBuilder.text("player_not_in_team"));
                return;
            }

            network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.remove_multiple_players", msg.players.size(), team.getName()));
        });
        ctx.setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<Message> {

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
