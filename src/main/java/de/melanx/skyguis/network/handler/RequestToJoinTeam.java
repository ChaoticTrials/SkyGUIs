package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RequestToJoinTeam extends PacketHandler<RequestToJoinTeam.Message> {

    public static final CustomPacketPayload.Type<RequestToJoinTeam.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().id("request_to_join_team"));

    public RequestToJoinTeam() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();

        EasyNetwork network = SkyGUIs.getNetwork();
        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_INVITES)) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_JOIN_REQUEST);
            return;
        }

        SkyblockSavedData data = SkyblockSavedData.get(player.level());
        Team team = data.getTeam(msg.team);
        if (team == null) {
            // should never be the case
            return;
        }

        if (data.hasPlayerTeam(player)) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_USER_HAS_TEAM);
            return;
        }

        if (!team.allowsJoinRequests()) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_TEAM_JOIN_REQUEST);
            return;
        }

        team.sendJoinRequest(player);
        network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, SkyComponents.SUCCESS_JOIN_REQUEST.apply(team.getName()));
    }

    public record Message(UUID team) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, RequestToJoinTeam.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> buffer.writeUUID(msg.team)),
                buffer -> new RequestToJoinTeam.Message(buffer.readUUID())
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return RequestToJoinTeam.TYPE;
        }
    }
}
