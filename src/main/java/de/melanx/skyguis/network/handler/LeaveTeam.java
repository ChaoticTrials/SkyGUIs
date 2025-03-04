package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.common.InventoryConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
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

public class LeaveTeam extends PacketHandler<LeaveTeam.Message> {

    public static final CustomPacketPayload.Type<LeaveTeam.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().resource("leave_team"));

    public LeaveTeam() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();

        EasyNetwork network = SkyGUIs.getNetwork();
        SkyblockSavedData data = SkyblockSavedData.get(player.level());
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_USER_HAS_NO_TEAM);
            return;
        }

        switch (SkyblockHooks.onLeave(player, team)) {
            case DENY -> {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_LEAVE_TEAM);
                return;
            }
            case DEFAULT -> {
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_LEAVE)) {
                    network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DISABLED_MANAGE_TEAMS);
                    return;
                }
            }
        }

        if (InventoryConfig.dropItems) {
            RandomUtility.dropInventories(player);
        }

        data.removePlayerFromTeam(msg.player());
        network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, SkyComponents.SUCCESS_LEFT_TEAM);
        RandomUtility.deleteTeamIfEmpty(data, team);
        WorldUtil.teleportToIsland(player, data.getSpawn());
    }

    public record Message(UUID player) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, LeaveTeam.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> buffer.writeUUID(msg.player)),
                buffer -> new LeaveTeam.Message(buffer.readUUID())
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return LeaveTeam.TYPE;
        }
    }
}