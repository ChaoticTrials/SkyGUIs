package de.melanx.skyguis.network.handler;

import com.google.common.collect.Sets;
import de.melanx.skyblockbuilder.config.common.InventoryConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UpdateTeam extends PacketHandler<UpdateTeam.Message> {

    public static final CustomPacketPayload.Type<UpdateTeam.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().id("update_team"));

    public UpdateTeam() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        EasyNetwork network = SkyGUIs.getNetwork();

        if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, ComponentBuilder.text("missing_permissions"));
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_USER_HAS_NO_TEAM);
            return;
        }

        PlayerList playerList = level.getServer().getPlayerList();
        Pair<Boolean, Set<ServerPlayer>> result = SkyblockHooks.onManageRemoveFromTeam(null, team, msg.players.stream().map(playerList::getPlayer).collect(Collectors.toList()));
        if (result.getLeft()) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_REMOVE_PLAYERS_FROM_TEAM);
            return;
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
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, ComponentBuilder.text("player_not_in_team"));
            return;
        }

        network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, SkyComponents.SUCCESS_REMOVE_MULTIPLE_PLAYERS.apply(msg.players.size(), team.getName()));
    }

    public record Message(String teamName, Set<UUID> players) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    buffer.writeUtf(msg.teamName);
                    buffer.writeVarInt(msg.players.size());
                    for (UUID id : msg.players) {
                        buffer.writeUUID(id);
                    }
                }), buffer -> {
                    String teamName = buffer.readUtf();
                    int size = buffer.readVarInt();
                    Set<UUID> ids = Sets.newHashSet();
                    for (int i = 0; i < size; i++) {
                        ids.add(buffer.readUUID());
                    }
                    return new Message(teamName, ids);
                });

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return UpdateTeam.TYPE;
        }
    }
}
