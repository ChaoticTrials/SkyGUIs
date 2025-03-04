package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockManageTeamEvent;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class EditSpawns extends PacketHandler<EditSpawns.Message> {

    public static final CustomPacketPayload.Type<EditSpawns.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().resource("edit_spawns"));

    public EditSpawns() {
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

        switch (msg.editSpawnsType) {
            // handle adding spawns
            case ADD -> {
                Direction vanillaDirection = switch (msg.direction) {
                    case NORTH -> Direction.NORTH;
                    case SOUTH -> Direction.SOUTH;
                    case EAST -> Direction.EAST;
                    case WEST -> Direction.WEST;
                };
                SkyblockManageTeamEvent.Result result = SkyblockHooks.onAddSpawn(player, team, msg.pos, vanillaDirection).getLeft();
                switch (result) {
                    case DENY -> {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_CREATE_SPAWN);
                        return;
                    }
                    case DEFAULT -> {
                        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.EDIT_SPAWNS)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_MODIFY_SPAWNS);
                            return;
                        }

                        Vec3i templateSize = TemplateData.get(level).getConfiguredTemplate().getTemplate().getSize();
                        BlockPos center = team.getIsland().getCenter().mutable();
                        center.offset(templateSize.getX() / 2, templateSize.getY() / 2, templateSize.getZ() / 2);
                        if (!msg.pos.closerThan(center, PermissionsConfig.Spawns.range)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_POSITION_TOO_FAR_AWAY);
                            return;
                        }
                    }
                }

                team.addPossibleSpawn(msg.pos, msg.direction);
                network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, SkyComponents.SUCCESS_SPAWN_ADDED.apply(
                        msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()
                ));
            }

            // handle removing spawns
            case REMOVE -> {
                if (level != level.getServer().getLevel(SpawnConfig.spawmDimension)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_WRONG_POSITION);
                    return;
                }

                SkyblockManageTeamEvent.Result result = SkyblockHooks.onRemoveSpawn(player, team, msg.pos);
                switch (result) {
                    case DENY -> {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_MODIFY_SPAWNS0);
                        return;
                    }
                    case DEFAULT -> {
                        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.EDIT_SPAWNS)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_MODIFY_SPAWNS);
                            return;
                        }
                    }
                }

                if (!team.removePossibleSpawn(msg.pos)) {
                    MutableComponent answer = SkyComponents.ERROR_REMOVE_SPAWN0;
                    if (team.getPossibleSpawns().size() <= 1) {
                        answer.append(" ").append(SkyComponents.ERROR_REMOVE_SPAWN1);
                    }
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, answer);
                    return;
                }

                network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, SkyComponents.SUCCESS_SPAWN_REMOVED.apply(
                        msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()
                ));
            }

            case RESET -> {
                switch (SkyblockHooks.onResetSpawns(player, team)) {
                    case DENY -> {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_MODIFY_SPAWNS0);
                        return;
                    }
                    case DEFAULT -> {
                        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.EDIT_SPAWNS)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_MODIFY_SPAWNS);
                            return;
                        }
                    }
                }

                team.setPossibleSpawns(team.getDefaultPossibleSpawns());
            }
        }
    }

    public record Message(EditSpawns.Type editSpawnsType, BlockPos pos, WorldUtil.SpawnDirection direction) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, EditSpawns.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    buffer.writeEnum(msg.editSpawnsType);
                    buffer.writeBlockPos(msg.pos);
                    buffer.writeEnum(msg.direction);
                }), buffer -> new EditSpawns.Message(
                        buffer.readEnum(EditSpawns.Type.class),
                        buffer.readBlockPos(),
                        buffer.readEnum(WorldUtil.SpawnDirection.class)
                ));

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return EditSpawns.TYPE;
        }
    }

    public enum Type {
        ADD,
        REMOVE,
        RESET
    }
}
