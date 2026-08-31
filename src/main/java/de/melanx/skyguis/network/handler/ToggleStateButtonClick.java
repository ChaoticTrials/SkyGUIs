package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.util.ToggleButtons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ToggleStateButtonClick extends PacketHandler<ToggleStateButtonClick.Message> {

    public static final CustomPacketPayload.Type<ToggleStateButtonClick.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().id("toggle_state_button_click"));

    public ToggleStateButtonClick() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();

        SkyblockSavedData data = SkyblockSavedData.get(player.level());
        Team team = data.getTeam(msg.team);
        if (team != null) {
            switch (msg.toggleButtonsType) {
                case VISITS -> team.setAllowVisit(!team.allowsVisits());
                case JOIN_REQUEST -> team.setAllowJoinRequest(!team.allowsJoinRequests());
            }
        }
    }

    public record Message(UUID team, ToggleButtons.Type toggleButtonsType) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleStateButtonClick.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    buffer.writeUUID(msg.team);
                    buffer.writeEnum(msg.toggleButtonsType);
                }), buffer -> new ToggleStateButtonClick.Message(buffer.readUUID(), buffer.readEnum(ToggleButtons.Type.class)));

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ToggleStateButtonClick.TYPE;
        }
    }
}
