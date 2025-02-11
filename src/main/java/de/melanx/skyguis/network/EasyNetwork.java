package de.melanx.skyguis.network;

import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyguis.network.handler.*;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.ToggleButtons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.annotation.meta.RemoveIn;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class EasyNetwork extends NetworkX {

    public EasyNetwork(ModX mod) {
        super(mod);
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("9");
    }

    @Override
    protected void registerPackets() {
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new CreateTeamScreenClick.Serializer(), () -> CreateTeamScreenClick.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new UpdateTeam.Serializer(), () -> UpdateTeam.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new EditSpawns.Serializer(), () -> EditSpawns.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new RemoveSpawns.Serializer(), () -> RemoveSpawns.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new LeaveTeam.Serializer(), () -> LeaveTeam.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new InvitePlayers.Serializer(), () -> InvitePlayers.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new AnswerInvitation.Serializer(), () -> AnswerInvitation.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new RequestTemplateFromServer.Serializer(), () -> RequestTemplateFromServer.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new VisitTeam.Serializer(), () -> VisitTeam.Handler::new); // todo 1.21 remove and use TeleportToTeam
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new TeleportToTeam.Serializer(), () -> TeleportToTeam.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new RequestToJoinTeam.Serializer(), () -> RequestToJoinTeam.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new ToggleStateButtonClick.Serializer(), () -> ToggleStateButtonClick.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new UpdateSkyblockSavedData.Serializer(), () -> UpdateSkyblockSavedData.Handler::new);

        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new OpenGui.Serializer(), () -> OpenGui.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new SendLoadingResult.Serializer(), () -> SendLoadingResult.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new SendTemplateToClient.Serializer(), () -> SendTemplateToClient.Handler::new);
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.21")
    public void handleCreateTeam(String name, String shape) {
        this.channel.sendToServer(new CreateTeamScreenClick(name, shape, false, false));
    }

    public void handleCreateTeam(String name, String shape, boolean allowVisits, boolean allowJoinRequests) {
        this.channel.sendToServer(new CreateTeamScreenClick(name, shape, allowVisits, allowJoinRequests));
    }

    public void handleKickPlayers(String teamName, Set<UUID> players) {
        this.channel.sendToServer(new UpdateTeam(teamName, players));
    }

    public void handleInvitePlayers(String teamName, Set<UUID> players) {
        this.channel.sendToServer(new InvitePlayers(teamName, players));
    }

    public void handleInvitationAnswer(String teamName, AnswerInvitation.Type type) {
        this.channel.sendToServer(new AnswerInvitation(teamName, type));
    }

    public void handleEditSpawns(EditSpawns.Type type, BlockPos pos, Direction direction) {
        this.channel.sendToServer(new EditSpawns(type, pos, direction));
    }

    public void handleRemoveSpawns(Set<BlockPos> positions) {
        this.channel.sendToServer(new RemoveSpawns(positions));
    }

    public void leaveTeam(Player player) {
        this.leaveTeam(player.getGameProfile().getId());
    }

    public void leaveTeam(UUID player) {
        this.channel.sendToServer(new LeaveTeam(player));
    }

    public void handleLoadingResult(NetworkEvent.Context ctx, LoadingResult.Status result, Component reason) {
        this.channel.reply(new SendLoadingResult(result, reason), ctx);
    }

    public void requestTemplateFromServer(String name) {
        this.channel.sendToServer(new RequestTemplateFromServer(name));
    }

    public void sendTemplateToClient(NetworkEvent.Context ctx, String name) {
        this.channel.reply(new SendTemplateToClient(name, TemplateLoader.getConfiguredTemplate(name)), ctx);
    }

    public void visitTeam(Team team) {
        this.visitTeam(team.getId());
    }

    public void visitTeam(UUID team) {
        this.channel.sendToServer(new VisitTeam(team));
    }

    public void teleportToTeam(Team team) {
        this.teleportToTeam(team.getId());
    }

    public void teleportToTeam(UUID team) {
        this.channel.sendToServer(new TeleportToTeam(team));
    }

    public void requestToJoinTeam(Team team) {
        this.requestToJoinTeam(team.getId());
    }

    public void requestToJoinTeam(UUID team) {
        this.channel.sendToServer(new RequestToJoinTeam(team));
    }

    public void toggleState(Team team, ToggleButtons.Type type) {
        this.toggleState(team.getId(), type);
    }

    public void toggleState(UUID team, ToggleButtons.Type type) {
        this.channel.sendToServer(new ToggleStateButtonClick(team, type));
    }

    public void updateSkyblockSavedData() {
        this.channel.sendToServer(new UpdateSkyblockSavedData());
    }
}
