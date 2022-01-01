package de.melanx.skyguis.network;

import de.melanx.skyguis.network.handler.*;
import de.melanx.skyguis.util.LoadingResult;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;

public class EasyNetwork extends NetworkX {

    public EasyNetwork(ModX mod) {
        super(mod);
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("0");
    }

    @Override
    protected void registerPackets() {
        this.register(new CreateTeamScreenClick.Serializer(), () -> CreateTeamScreenClick::handle, NetworkDirection.PLAY_TO_SERVER);
        this.register(new UpdateTeam.Serializer(), () -> UpdateTeam::handle, NetworkDirection.PLAY_TO_SERVER);
        this.register(new EditSpawns.Serializer(), () -> EditSpawns::handle, NetworkDirection.PLAY_TO_SERVER);
        this.register(new InvitePlayers.Serializer(), () -> InvitePlayers::handle, NetworkDirection.PLAY_TO_SERVER);
        this.register(new AnswerInvitation.Serializer(), () -> AnswerInvitation::handle, NetworkDirection.PLAY_TO_SERVER);

        this.register(new SendLoadingResult.Serializer(), () -> SendLoadingResult::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void handleCreateTeam(String name, String shape) {
        this.channel.sendToServer(new CreateTeamScreenClick.Message(name, shape));
    }

    public void handleKickPlayers(String teamName, Set<UUID> players) {
        this.channel.sendToServer(new UpdateTeam.Message(teamName, players));
    }

    public void handleInvitePlayers(String teamName, Set<UUID> players) {
        this.channel.sendToServer(new InvitePlayers.Message(teamName, players));
    }

    public void handleInvitationAnswer(String teamName, AnswerInvitation.Type type) {
        this.channel.sendToServer(new AnswerInvitation.Message(teamName, type));
    }

    public void handleEditSpawns(EditSpawns.Type type, BlockPos pos) {
        this.channel.sendToServer(new EditSpawns.Message(type, pos));
    }

    public void handleLoadingResult(NetworkEvent.Context ctx, LoadingResult.Status result, Component reason) {
        this.channel.reply(new SendLoadingResult.Message(result, reason), ctx);
    }
}
