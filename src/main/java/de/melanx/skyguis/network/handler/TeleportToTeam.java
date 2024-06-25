package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.UUID;
import java.util.function.Supplier;

public record TeleportToTeam(UUID team) {

    public static class Handler implements PacketHandler<TeleportToTeam> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(TeleportToTeam msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            Level level = player.level();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeam(msg.team);
            if (team == null) {
                // should never be the case
                return true;
            }

            if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.teleportationDimensions.test(player.level().dimension().location())) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.teleportation_not_allowed_dimension"));
                return true;
            }

            if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.crossDimensionTeleportation && level != data.getLevel()) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.teleport_across_dimensions"));
                return true;
            }

            if (!player.hasPermissions(2) && PermissionsConfig.Teleports.preventWhileFalling && player.fallDistance > 1) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.prevent_while_falling"));
                return true;
            }

            if (team.isSpawn()) {
                if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.spawn) {
                    network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyguis.command.disabled.teleport_spawn")); // todo 1.21 move this to skyblock builder
                    return true;
                }

                if (!player.hasPermissions(2) && !data.getOrCreateMetaInfo(player).canTeleportSpawn(level.getGameTime())) {
                    network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.cooldown",
                            RandomUtility.formattedCooldown(PermissionsConfig.Teleports.spawnCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastSpawnTeleport()))));
                    return true;
                }
            }

            if (!team.isSpawn()) {
                if (!team.hasPlayer(player)) {
                    // todo 1.21 handle visit
                    return true;
                }

                if (!player.hasPermissions(2) && !data.getOrCreateMetaInfo(player).canTeleportHome(level.getGameTime())) {
                    network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.cooldown",
                            RandomUtility.formattedCooldown(PermissionsConfig.Teleports.spawnCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastSpawnTeleport()))));
                    return true;
                }

                switch (SkyblockHooks.onHome(player, team)) {
                    case DENY:
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.teleport_home"));
                        return true;
                    case DEFAULT:
                        if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.home) {
                            network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.teleport_home"));
                            return true;
                        }
                        break;
                    case ALLOW:
                        break;
                }
            }

            if (!player.hasPermissions(2)) {
                if (team.isSpawn()) {
                    // todo 1.21 use the enum, see SkyblockBuilder#SkyMeta for more information
                    data.getOrCreateMetaInfo(player).setLastSpawnTeleport(level.getGameTime());
                } else {
                    data.getOrCreateMetaInfo(player).setLastHomeTeleport(level.getGameTime());
                }
            }

            WorldUtil.teleportToIsland(player, team);
            network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.empty());
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<TeleportToTeam> {

        @Override
        public Class<TeleportToTeam> messageClass() {
            return TeleportToTeam.class;
        }

        @Override
        public void encode(TeleportToTeam msg, FriendlyByteBuf buffer) {
            buffer.writeUUID(msg.team);
        }

        @Override
        public TeleportToTeam decode(FriendlyByteBuf buffer) {
            return new TeleportToTeam(buffer.readUUID());
        }
    }

}
