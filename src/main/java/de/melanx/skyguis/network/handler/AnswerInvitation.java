package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.ConfigHandler;
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
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public class AnswerInvitation {

    public static void handle(Message msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            SkyblockSavedData data = SkyblockSavedData.get(player.getCommandSenderWorld());
            Team team = data.getTeam(msg.teamName);
            EasyNetwork network = SkyGUIs.getNetwork();

            switch (msg.type) {
                case ACCEPT -> {
                    if (team == null) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED));
                        return;
                    }

                    if (data.hasPlayerTeam(player)) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_team").withStyle(ChatFormatting.RED));
                        return;
                    }

                    if (!data.hasInvites(player)) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.no_invitations").withStyle(ChatFormatting.RED));
                        return;
                    }

                    switch (SkyblockHooks.onAccept(player, team)) {
                        case DENY -> {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.accept_invitations").withStyle(ChatFormatting.RED));
                            return;
                        }
                        case DEFAULT -> {
                            if (!ConfigHandler.Utility.selfManage && !player.hasPermissions(2)) {
                                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.accept_invitations").withStyle(ChatFormatting.RED));
                                return;
                            }
                        }
                    }

                    if (!data.acceptInvite(team, player)) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.accept_invitations").withStyle(ChatFormatting.RED));
                        return;
                    }

                    network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.joined_team").withStyle(ChatFormatting.GOLD));
                }

                case IGNORE -> {
                    if (team == null) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED));
                        return;
                    }

                    if (!data.hasInvites(player)) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.no_invitations").withStyle(ChatFormatting.RED));
                        return;
                    }

                    switch (SkyblockHooks.onDecline(player, team)) {
                        case DENY -> {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.decline_invitations").withStyle(ChatFormatting.RED));
                            return;
                        }
                        case DEFAULT -> {
                            if (!ConfigHandler.Utility.selfManage && !player.hasPermissions(2)) {
                                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.decline_invitations").withStyle(ChatFormatting.RED));
                                return;
                            }
                        }
                    }

                    if (!data.declineInvite(team, player)) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.decline_invitations").withStyle(ChatFormatting.RED));
                        return;
                    }

                    network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.declined_invitation", team.getName()).withStyle(ChatFormatting.GOLD));
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.teamName);
            buffer.writeEnum(msg.type);
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message(buffer.readUtf(), buffer.readEnum(Type.class));
        }
    }

    public record Message(String teamName, Type type) {
    }

    public enum Type {
        ACCEPT,
        IGNORE
    }
}
