package de.melanx.skyguis.network;

import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.network.handler.*;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.ToggleButtons;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class EasyNetwork extends NetworkX {

    public EasyNetwork(ModX mod) {
        super(mod);

        // send to server
        this.register(new AnswerInvitation());
        this.register(new CreateTeamScreenClick());
        this.register(new EditSpawns());
        this.register(new InvitePlayers());
        this.register(new LeaveTeam());
        this.register(new RemoveSpawns());
        this.register(new RequestTemplateFromServer());
        this.register(new RequestToJoinTeam());
        this.register(new TeleportToTeam());
        this.register(new ToggleStateButtonClick());
        this.register(new UpdateSkyblockSavedData());
        this.register(new UpdateTeam());

        // send to client
        this.register(new OpenGui());
        this.register(new SendLoadingResult());
        this.register(new SendTemplateToClient());
    }

    @Override
    protected String getVersion() {
        return "11";
    }

    public void handleCreateTeam(String name, String shape, Optional<Integer> paletteIndex, boolean allowVisits, boolean allowJoinRequests) {
        ClientPacketDistributor.sendToServer(new CreateTeamScreenClick.Message(name, shape, paletteIndex, allowVisits, allowJoinRequests));
    }

    public void handleKickPlayers(String teamName, Set<UUID> players) {
        ClientPacketDistributor.sendToServer(new UpdateTeam.Message(teamName, players));
    }

    public void handleInvitePlayers(String teamName, Set<UUID> players) {
        ClientPacketDistributor.sendToServer(new InvitePlayers.Message(teamName, players));
    }

    public void handleInvitationAnswer(String teamName, AnswerInvitation.Type type) {
        ClientPacketDistributor.sendToServer(new AnswerInvitation.Message(teamName, type));
    }

    public void handleEditSpawns(EditSpawns.Type type, BlockPos pos, WorldUtil.SpawnDirection direction) {
        ClientPacketDistributor.sendToServer(new EditSpawns.Message(type, pos, direction));
    }

    public void handleRemoveSpawns(Set<BlockPos> positions) {
        ClientPacketDistributor.sendToServer(new RemoveSpawns.Message(positions));
    }

    public void leaveTeam(Player player) {
        this.leaveTeam(player.getGameProfile().id());
    }

    public void leaveTeam(UUID player) {
        ClientPacketDistributor.sendToServer(new LeaveTeam.Message(player));
    }

    public void handleLoadingResult(IPayloadContext ctx, LoadingResult.Status result, Component reason) {
        PacketDistributor.sendToPlayer((ServerPlayer) ctx.player(), new SendLoadingResult.Message(result, reason));
    }

    public void requestTemplateFromServer(String name) {
        ClientPacketDistributor.sendToServer(new RequestTemplateFromServer.Message(name));
    }

    public void sendTemplateToClient(IPayloadContext ctx, String name) {
        PacketDistributor.sendToPlayer((ServerPlayer) ctx.player(), new SendTemplateToClient.Message(name, TemplateLoader.getConfiguredTemplate(name)));
    }

    public void teleportToSpawn() {
        this.teleportToTeam(SkyblockSavedData.SPAWN_ID, SkyMeta.TeleportType.SPAWN);
    }

    public void teleportToTeam(Team team, SkyMeta.TeleportType teleportType) {
        this.teleportToTeam(team.id(), teleportType);
    }

    public void teleportToTeam(UUID team, SkyMeta.TeleportType teleportType) {
        ClientPacketDistributor.sendToServer(new TeleportToTeam.Message(team, teleportType));
    }

    public void requestToJoinTeam(Team team) {
        this.requestToJoinTeam(team.id());
    }

    public void requestToJoinTeam(UUID team) {
        ClientPacketDistributor.sendToServer(new RequestToJoinTeam.Message(team));
    }

    public void toggleState(Team team, ToggleButtons.Type type) {
        this.toggleState(team.id(), type);
    }

    public void toggleState(UUID team, ToggleButtons.Type type) {
        ClientPacketDistributor.sendToServer(new ToggleStateButtonClick.Message(team, type));
    }

    public void updateSkyblockSavedData() {
        ClientPacketDistributor.sendToServer(new UpdateSkyblockSavedData.Message());
    }
}
