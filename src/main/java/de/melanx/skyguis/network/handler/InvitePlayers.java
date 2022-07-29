package de.melanx.skyguis.network.handler;

import com.google.common.collect.Sets;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public record InvitePlayers(String teamName, Set<UUID> players) {

    public static class Handler implements PacketHandler<InvitePlayers> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(InvitePlayers msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            SkyblockSavedData data = SkyblockSavedData.get(player.getCommandSenderWorld());
            Team team = data.getTeam(msg.teamName);

            EasyNetwork network = SkyGUIs.getNetwork();
            if (team == null) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.team_not_exist"));
                return true;
            }

            //noinspection ConstantConditions
            PlayerList playerList = player.getServer().getPlayerList();
            int i = 0;
            for (UUID id : msg.players) {
                if (data.hasInvites(id) && data.hasInviteFrom(team, id)) {
                    continue;
                }

                data.addInvite(team, player, id);
                ServerPlayer toInvite = playerList.getPlayer(id);
                if (toInvite != null) {
                    MutableComponent invite = Component.translatable("skyblockbuilder.command.info.invited_to_team0", player.getDisplayName().getString(), team.getName()).withStyle(ChatFormatting.GOLD);
                    invite.append(Component.literal("/skyblock accept \"" + team.getName() + "\"").setStyle(Style.EMPTY
                            .withHoverEvent(InviteCommand.COPY_TEXT)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skyblock accept \"" + team.getName() + "\""))
                            .applyFormat(ChatFormatting.UNDERLINE).applyFormat(ChatFormatting.GOLD)));
                    invite.append(Component.translatable("skyblockbuilder.command.info.invited_to_team1").withStyle(ChatFormatting.GOLD));
                    toInvite.sendSystemMessage(invite);
                }
                i++;
            }

            network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, ComponentBuilder.text("invite_multiple_users", i));
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<InvitePlayers> {

        @Override
        public Class<InvitePlayers> messageClass() {
            return InvitePlayers.class;
        }

        @Override
        public void encode(InvitePlayers msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.teamName);
            buffer.writeVarInt(msg.players.size());
            for (UUID id : msg.players) {
                buffer.writeUUID(id);
            }
        }

        @Override
        public InvitePlayers decode(FriendlyByteBuf buffer) {
            String teamName = buffer.readUtf();
            int size = buffer.readVarInt();
            Set<UUID> ids = Sets.newHashSet();
            for (int i = 0; i < size; i++) {
                ids.add(buffer.readUUID());
            }

            return new InvitePlayers(teamName, ids);
        }
    }
}
