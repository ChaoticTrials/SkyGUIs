package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
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

public class AnswerInvitation extends PacketHandler<AnswerInvitation.Message> {

    public static final CustomPacketPayload.Type<AnswerInvitation.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().resource("answer_invitation"));

    public AnswerInvitation() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();

        SkyblockSavedData data = SkyblockSavedData.get(player.getCommandSenderWorld());
        Team team = data.getTeam(msg.teamName);
        EasyNetwork network = SkyGUIs.getNetwork();

        switch (msg.answerType) {
            case ACCEPT -> {
                if (team == null) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_TEAM_NOT_EXIST);
                    return;
                }

                if (data.hasPlayerTeam(player)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_USER_HAS_TEAM);
                    return;
                }

                if (!data.hasInvites(player)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_NO_INVITATIONS);
                    return;
                }

                switch (SkyblockHooks.onAccept(player, team)) {
                    case DENY -> {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_ACCEPT_INVITATIONS);
                        return;
                    }
                    case DEFAULT -> {
                        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_INVITES)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_ACCEPT_INVITATIONS);
                            return;
                        }
                    }
                }

                if (!data.acceptInvite(team, player)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_ACCEPT_INVITATIONS);
                    return;
                }

                network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, SkyComponents.SUCCESS_JOINED_TEAM.apply(team.getName()));
            }

            case IGNORE -> {
                if (team == null) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_TEAM_NOT_EXIST);
                    return;
                }

                if (!data.hasInvites(player)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_NO_INVITATIONS);
                    return;
                }

                switch (SkyblockHooks.onDecline(player, team)) {
                    case DENY -> {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_DECLINE_INVITATIONS);
                        return;
                    }
                    case DEFAULT -> {
                        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_INVITES)) {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_DECLINE_INVITATIONS);
                            return;
                        }
                    }
                }

                if (!data.declineInvite(team, player)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_DECLINE_INVITATIONS);
                    return;
                }

                network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, SkyComponents.SUCCESS_DECLINED_INVITATION.apply(team.getName()));
            }
        }
    }

    public record Message(String teamName, AnswerInvitation.Type answerType) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, AnswerInvitation.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    buffer.writeUtf(msg.teamName);
                    buffer.writeEnum(msg.answerType);
                }), buffer -> new AnswerInvitation.Message(buffer.readUtf(), buffer.readEnum(AnswerInvitation.Type.class)));

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return AnswerInvitation.TYPE;
        }
    }

    public enum Type {
        ACCEPT,
        IGNORE
    }
}
