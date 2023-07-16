package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
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

import java.util.function.Supplier;

public record AnswerInvitation(String teamName, Type type) {

    public static class Handler implements PacketHandler<AnswerInvitation> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(AnswerInvitation msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            SkyblockSavedData data = SkyblockSavedData.get(player.getCommandSenderWorld());
            Team team = data.getTeam(msg.teamName);
            EasyNetwork network = SkyGUIs.getNetwork();

            switch (msg.type) {
                case ACCEPT -> {
                    if (team == null) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED));
                        return true;
                    }

                    if (data.hasPlayerTeam(player)) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_team").withStyle(ChatFormatting.RED));
                        return true;
                    }

                    if (!data.hasInvites(player)) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.no_invitations").withStyle(ChatFormatting.RED));
                        return true;
                    }

                    switch (SkyblockHooks.onAccept(player, team)) {
                        case DENY -> {
                            network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.accept_invitations").withStyle(ChatFormatting.RED));
                            return true;
                        }
                        case DEFAULT -> {
                            if (!PermissionsConfig.selfManage && !player.hasPermissions(2)) {
                                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.accept_invitations").withStyle(ChatFormatting.RED));
                                return true;
                            }
                        }
                    }

                    if (!data.acceptInvite(team, player)) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.accept_invitations").withStyle(ChatFormatting.RED));
                        return true;
                    }

                    network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.joined_team").withStyle(ChatFormatting.GOLD));
                }

                case IGNORE -> {
                    if (team == null) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED));
                        return true;
                    }

                    if (!data.hasInvites(player)) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.no_invitations").withStyle(ChatFormatting.RED));
                        return true;
                    }

                    switch (SkyblockHooks.onDecline(player, team)) {
                        case DENY -> {
                            network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.decline_invitations").withStyle(ChatFormatting.RED));
                            return true;
                        }
                        case DEFAULT -> {
                            if (!PermissionsConfig.selfManage && !player.hasPermissions(2)) {
                                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.decline_invitations").withStyle(ChatFormatting.RED));
                                return true;
                            }
                        }
                    }

                    if (!data.declineInvite(team, player)) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.decline_invitations").withStyle(ChatFormatting.RED));
                        return true;
                    }

                    network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.declined_invitation", team.getName()).withStyle(ChatFormatting.GOLD));
                }
            }
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<AnswerInvitation> {

        @Override
        public Class<AnswerInvitation> messageClass() {
            return AnswerInvitation.class;
        }

        @Override
        public void encode(AnswerInvitation msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.teamName);
            buffer.writeEnum(msg.type);
        }

        @Override
        public AnswerInvitation decode(FriendlyByteBuf buffer) {
            return new AnswerInvitation(buffer.readUtf(), buffer.readEnum(Type.class));
        }
    }

    public enum Type {
        ACCEPT,
        IGNORE
    }
}
