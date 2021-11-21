package de.melanx.easyskyblockmanagement.network;

import de.melanx.easyskyblockmanagement.network.handler.CreateTeamScreenClick;
import de.melanx.easyskyblockmanagement.network.handler.SendLoadingResult;
import de.melanx.easyskyblockmanagement.network.handler.UpdateTeam;
import de.melanx.easyskyblockmanagement.util.LoadingResult;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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

        this.register(new SendLoadingResult.Serializer(), () -> SendLoadingResult::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void handleCreateTeam(String name, String shape) {
        this.channel.sendToServer(new CreateTeamScreenClick.Message(name, shape));
    }

    public void handleKickPlayers(String teamName, Set<UUID> players) {
        this.channel.sendToServer(new UpdateTeam.Message(teamName, players));
    }

    public void handleLoadingResult(NetworkEvent.Context ctx, LoadingResult.Status result, Component reason) {
        this.channel.reply(new SendLoadingResult.Message(result, reason), ctx);
    }
}
