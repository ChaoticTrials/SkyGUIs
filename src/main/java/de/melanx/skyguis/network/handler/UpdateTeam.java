package de.melanx.skyguis.network.handler;

import com.google.common.collect.Sets;
import de.melanx.skyblockbuilder.config.common.InventoryConfig;
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
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public record UpdateTeam(String teamName, Set<UUID> players) {

    public static class Handler implements PacketHandler<UpdateTeam> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(UpdateTeam msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            if (!player.hasPermissions(2)) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, ComponentBuilder.text("missing_permissions"));
                return true;
            }

            ServerLevel level = (ServerLevel) player.level();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeamFromPlayer(player);
            if (team == null) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_no_team"));
                return true;
            }

            PlayerList playerList = level.getServer().getPlayerList();
            Pair<Boolean, Set<ServerPlayer>> result = SkyblockHooks.onManageRemoveFromTeam(null, team, msg.players.stream().map(playerList::getPlayer).collect(Collectors.toList()));
            if (result.getLeft()) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.remove_players_from_team"));
                return true;
            }

            if (team.getName().equalsIgnoreCase(msg.teamName)) {
                for (UUID id : msg.players) {
                    if (team.hasPlayer(id)) {
                        data.removePlayerFromTeam(id);
                        ServerPlayer toRemove = playerList.getPlayer(id);
                        if (toRemove != null) {
                            if (InventoryConfig.dropItems) {
                                RandomUtility.dropInventories(toRemove);
                            }
                            WorldUtil.teleportToIsland(toRemove, data.getSpawn());
                        }
                    }
                }
            } else {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, ComponentBuilder.text("player_not_in_team"));
                return true;
            }

            network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.remove_multiple_players", msg.players.size(), team.getName()));
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<UpdateTeam> {

        @Override
        public Class<UpdateTeam> messageClass() {
            return UpdateTeam.class;
        }

        @Override
        public void encode(UpdateTeam msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.teamName);
            buffer.writeVarInt(msg.players.size());
            for (UUID id : msg.players) {
                buffer.writeUUID(id);
            }
        }

        @Override
        public UpdateTeam decode(FriendlyByteBuf buffer) {
            String teamName = buffer.readUtf();
            int size = buffer.readVarInt();
            Set<UUID> ids = Sets.newHashSet();
            for (int i = 0; i < size; i++) {
                ids.add(buffer.readUUID());
            }

            return new UpdateTeam(teamName, ids);
        }
    }
}
