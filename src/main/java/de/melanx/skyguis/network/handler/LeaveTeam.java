package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.InventoryConfig;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.UUID;
import java.util.function.Supplier;

public record LeaveTeam(UUID player) {

    public static class Handler implements PacketHandler<LeaveTeam> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(LeaveTeam msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            SkyblockSavedData data = SkyblockSavedData.get(player.level());
            Team team = data.getTeamFromPlayer(player);

            if (team == null) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED));
                return true;
            }

            switch (SkyblockHooks.onLeave(player, team)) {
                case DENY:
                    network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.leave_team").withStyle(ChatFormatting.RED));
                    return true;
                case DEFAULT:
                    if (!PermissionsConfig.selfManage && !player.hasPermissions(2)) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.manage_teams").withStyle(ChatFormatting.RED));
                        return true;
                    }
                    break;
                case ALLOW:
                    break;
            }

            if (InventoryConfig.dropItems) {
                RandomUtility.dropInventories(player);
            }

            data.removePlayerFromTeam(msg.player);
            network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.left_team"));
            RandomUtility.deleteTeamIfEmpty(data, team);
            WorldUtil.teleportToIsland(player, data.getSpawn());
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<LeaveTeam> {

        @Override
        public Class<LeaveTeam> messageClass() {
            return LeaveTeam.class;
        }

        @Override
        public void encode(LeaveTeam msg, FriendlyByteBuf buffer) {
            buffer.writeUUID(msg.player);
        }

        @Override
        public LeaveTeam decode(FriendlyByteBuf buffer) {
            return new LeaveTeam(buffer.readUUID());
        }
    }
}
