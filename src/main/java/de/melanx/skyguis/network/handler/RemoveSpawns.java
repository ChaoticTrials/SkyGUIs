package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockManageTeamEvent;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class RemoveSpawns extends PacketHandler<RemoveSpawns.Message> {

    public static final CustomPacketPayload.Type<RemoveSpawns.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().resource("remove_spawns"));

    public RemoveSpawns() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();

        EasyNetwork network = SkyGUIs.getNetwork();
        ServerLevel level = (ServerLevel) player.getCommandSenderWorld();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_USER_HAS_NO_TEAM);
            return;
        }

        if (level != level.getServer().getLevel(SpawnConfig.spawnDimension)) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_WRONG_POSITION);
            return;
        }

        int failedRemovals = 0;
        for (BlockPos pos : msg.positions()) {
            SkyblockManageTeamEvent.Result result = SkyblockHooks.onRemoveSpawn(player, team, pos);
            switch (result) {
                case DENY -> failedRemovals++;
                case DEFAULT -> {
                    if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.EDIT_SPAWNS)) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_MODIFY_SPAWNS);
                        return;
                    }
                }
            }

            if (!team.removePossibleSpawn(pos)) {
                failedRemovals++;
            }
        }

        if (failedRemovals >= msg.positions().size()) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, ComponentBuilder.text("removed_spawns.failed"));
            return;
        }

        network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, ComponentBuilder.text("removed_spawns", msg.positions().size() - failedRemovals, msg.positions().size()));
    }

    public record Message(Set<BlockPos> positions) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    buffer.writeVarInt(msg.positions.size());
                    msg.positions.forEach(buffer::writeBlockPos);
                }), buffer -> {
                    Set<BlockPos> positions = new HashSet<>();
                    int size = buffer.readVarInt();
                    for (int i = 0; i < size; i++) {
                        positions.add(buffer.readBlockPos());
                    }
                    return new Message(positions);
                });

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return RemoveSpawns.TYPE;
        }
    }
}