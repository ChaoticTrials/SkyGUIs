package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.UUID;
import java.util.function.Supplier;

public record RequestToJoinTeam(UUID team) {

    public static class Handler implements PacketHandler<RequestToJoinTeam> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(RequestToJoinTeam msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            if (!PermissionsConfig.selfManage && !player.hasPermissions(2)) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.join_request"));
                return true;
            }

            ServerLevel level = (ServerLevel) player.level();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeam(msg.team);
            if (team == null) {
                // should never be the case
                return true;
            }

            if (data.hasPlayerTeam(player)) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_team"));
                return true;
            }

            if (!team.allowsJoinRequests()) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.team_join_request"));
                return true;
            }

            team.sendJoinRequest(player);
            network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.join_request", team.getName()));
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<RequestToJoinTeam> {

        @Override
        public Class<RequestToJoinTeam> messageClass() {
            return RequestToJoinTeam.class;
        }

        @Override
        public void encode(RequestToJoinTeam msg, FriendlyByteBuf buffer) {
            buffer.writeUUID(msg.team);
        }

        @Override
        public RequestToJoinTeam decode(FriendlyByteBuf buffer) {
            return new RequestToJoinTeam(buffer.readUUID());
        }
    }
}
