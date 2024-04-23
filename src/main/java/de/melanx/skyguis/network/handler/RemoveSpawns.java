package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public record RemoveSpawns(Set<BlockPos> positions) {

    public static class Handler implements PacketHandler<RemoveSpawns> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(RemoveSpawns msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            ServerLevel level = (ServerLevel) player.getCommandSenderWorld();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeamFromPlayer(player);

            if (team == null) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED));
                return true;
            }

            if (level != level.getServer().getLevel(SpawnConfig.dimension)) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.wrong_position"));
                return true;
            }

            int failedRemovals = 0;
            for (BlockPos pos : msg.positions) {
                Event.Result result = SkyblockHooks.onRemoveSpawn(player, team, pos);
                switch (result) {
                    case DENY -> failedRemovals++;
                    case DEFAULT -> {
                        if ((!PermissionsConfig.selfManage || !PermissionsConfig.Spawns.modifySpawns) && !player.hasPermissions(2)) {
                            network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.modify_spawns"));
                            return true;
                        }
                    }
                }

                if (!team.removePossibleSpawn(pos)) {
                    failedRemovals++;
                }
            }

            if (failedRemovals >= msg.positions.size()) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.literal("removed_spawns.failed"));
            }

            network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, ComponentBuilder.text("removed_spawns", msg.positions.size() - failedRemovals, msg.positions.size()));
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<RemoveSpawns> {

        @Override
        public Class<RemoveSpawns> messageClass() {
            return RemoveSpawns.class;
        }

        @Override
        public void encode(RemoveSpawns msg, FriendlyByteBuf buffer) {
            buffer.writeVarInt(msg.positions.size());
            msg.positions.forEach(buffer::writeBlockPos);
        }

        @Override
        public RemoveSpawns decode(FriendlyByteBuf buffer) {
            Set<BlockPos> positions = new HashSet<>();
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                positions.add(buffer.readBlockPos());
            }

            return new RemoveSpawns(positions);
        }
    }
}
