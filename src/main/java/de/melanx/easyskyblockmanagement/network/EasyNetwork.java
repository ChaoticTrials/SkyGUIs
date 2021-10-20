package de.melanx.easyskyblockmanagement.network;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraftforge.fmllegacy.network.NetworkDirection;

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
        this.register(new CreateTeamScreenClickHandler.Serializer(), () -> CreateTeamScreenClickHandler::handle, NetworkDirection.PLAY_TO_SERVER);
    }

    public void handleCreateTeam(String name, String shape) {
        this.channel.sendToServer(new CreateTeamScreenClickHandler.Message(name, shape));
    }
}
