package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TeleportToTeam extends PacketHandler<TeleportToTeam.Message> {

    public static final CustomPacketPayload.Type<TeleportToTeam.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().resource("teleport_to_team"));

    public TeleportToTeam() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        EasyNetwork network = SkyGUIs.getNetwork();
        Level level = player.level();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Team team = data.getTeam(msg.team);

        if (team == null) {
            // should never be the case
            return;
        }

        if (!PermissionManager.INSTANCE.mayBypassLimitation(player) && !PermissionsConfig.Teleports.teleportationDimensions.test(level.dimension().location())) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_TELEPORTATION_NOT_ALLOWED_DIMENSION);
            return;
        }

        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_ACROSS_DIMENSIONS) && level != data.getLevel()) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_TELEPORT_ACROSS_DIMENSIONS);
            return;
        }

        if (!PermissionManager.INSTANCE.mayBypassLimitation(player) && PermissionsConfig.Teleports.disallowTeleportationDuringFalling && player.fallDistance > 1) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_PREVENT_WHILE_FALLING);
            return;
        }

        switch (msg.teleportType) {
            case SPAWN -> {
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_TO_SPAWN)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_TELEPORT_SPAWN);
                    return;
                }
            }
            case HOME -> {
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_HOME)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_TELEPORT_HOME);
                    return;
                }
            }
            case VISIT -> {
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_TO_VISITING_ISLAND)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_TEAM_VISIT);
                    return;
                }
            }
        }

        if (!PermissionManager.INSTANCE.mayBypassLimitation(player) && !data.getOrCreateMetaInfo(player).canTeleport(msg.teleportType, level.getGameTime())) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_COOLDOWN.apply(
                    RandomUtility.formattedCooldown(PermissionsConfig.Teleports.Cooldowns.spawnCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastTeleport(SkyMeta.TeleportType.SPAWN))
                    )));
            return;
        }

        switch (msg.teleportType) {
            case SPAWN -> {
                switch (SkyblockHooks.onTeleportToSpawn(player, team)) {
                    case DENY -> {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_TELEPORT_TO_SPAWN);
                        return;
                    }
                    case DEFAULT -> {
                        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_TO_SPAWN)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_TELEPORT_SPAWN);
                            return;
                        }
                    }
                }
            }
            case HOME -> {
                switch (SkyblockHooks.onTeleportHome(player, team)) {
                    case DENY -> {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_TELEPORT_HOME);
                        return;
                    }
                    case DEFAULT -> {
                        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_HOME)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_TELEPORT_HOME);
                            return;
                        }
                    }
                }
            }
            case VISIT -> {
                switch (SkyblockHooks.onTeleportToVisit(player, team)) {
                    case DENY -> {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_VISIT_TEAM);
                        return;
                    }
                    case DEFAULT -> {
                        if (team.hasPlayer(player)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_VISIT_OWN_TEAM);
                            return;
                        }

                        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_TO_VISITING_ISLAND)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_TEAM_VISIT);
                            return;
                        }

                        if (!team.allowsVisits()) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_VISIT_TEAM);
                            return;
                        }
                    }
                }
            }
        }

        if (!PermissionManager.INSTANCE.mayBypassLimitation(player)) {
            data.getOrCreateMetaInfo(player).setLastTeleport(msg.teleportType, level.getGameTime());
        }

        WorldUtil.teleportToIsland(player, team);
        network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, this.getSuccessMessage(team, msg.teleportType));
    }

    private Component getSuccessMessage(Team team, SkyMeta.TeleportType teleportType) {
        return switch(teleportType) {
            case SPAWN -> SkyComponents.SUCCESS_TELEPORT_TO_SPAWN;
            case HOME -> SkyComponents.SUCCESS_TELEPORT_HOME;
            case VISIT -> SkyComponents.SUCCESS_VISIT_TEAM.apply(team.getName());
        };
    }

    public record Message(UUID team, SkyMeta.TeleportType teleportType) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    buffer.writeUUID(msg.team);
                    buffer.writeEnum(msg.teleportType);
                }),
                buffer -> new Message(buffer.readUUID(), buffer.readEnum(SkyMeta.TeleportType.class))
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TeleportToTeam.TYPE;
        }
    }
}
