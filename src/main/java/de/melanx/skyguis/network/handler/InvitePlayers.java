package de.melanx.skyguis.network.handler;

import com.google.common.collect.Sets;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

public class InvitePlayers extends PacketHandler<InvitePlayers.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().id("invite_players"));

    public InvitePlayers() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        SkyblockSavedData data = SkyblockSavedData.get(player.level());
        Team team = data.getTeam(msg.teamName);

        EasyNetwork network = SkyGUIs.getNetwork();
        if (team == null) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_TEAM_NOT_EXIST);
            return;
        }

        //noinspection ConstantConditions
        PlayerList playerList = player.level().getServer().getPlayerList();
        int i = 0;
        for (UUID id : msg.players) {
            if (data.hasInvites(id) && data.hasInviteFrom(team, id)) {
                continue;
            }

            data.addInvite(team, player, id);
            ServerPlayer toInvite = playerList.getPlayer(id);
            if (toInvite != null) {
                MutableComponent invite = SkyComponents.INFO_INVITED_TO_TEAM0.apply(player.getDisplayName().getString(), team.getName()).withStyle(ChatFormatting.GOLD);
                invite.append(Component.literal("/skyblock accept \"" + team.getName() + "\"").setStyle(Style.EMPTY
                        .withHoverEvent(InviteCommand.COPY_TEXT)
                        .withClickEvent(new ClickEvent.SuggestCommand("/skyblock accept \"" + team.getName() + "\""))
                        .applyFormat(ChatFormatting.UNDERLINE).applyFormat(ChatFormatting.GOLD)));
                invite.append(SkyComponents.INFO_INVITED_TO_TEAM1.withStyle(ChatFormatting.GOLD));
                toInvite.sendSystemMessage(invite);
            }
            i++;
        }

        network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, ComponentBuilder.text("invite_multiple_users", i));
    }

    public record Message(String teamName, Set<UUID> players) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, InvitePlayers.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    buffer.writeUtf(msg.teamName);
                    buffer.writeVarInt(msg.players.size());
                    for (UUID id : msg.players) {
                        buffer.writeUUID(id);
                    }
                }), buffer -> {
                    String teamName = buffer.readUtf();
                    int size = buffer.readVarInt();
                    Set<UUID> ids = Sets.newHashSet();
                    for (int i = 0; i < size; i++) {
                        ids.add(buffer.readUUID());
                    }

                    return new InvitePlayers.Message(teamName, ids);
                }
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return InvitePlayers.TYPE;
        }
    }
}
