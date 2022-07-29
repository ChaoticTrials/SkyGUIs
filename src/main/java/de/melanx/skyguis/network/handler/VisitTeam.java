package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.UUID;
import java.util.function.Supplier;

public record VisitTeam(UUID team) {

    public static class Handler implements PacketHandler<VisitTeam> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(VisitTeam msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            if (!ConfigHandler.Utility.Teleports.allowVisits) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.team_visit"));
                return true;
            }

            ServerLevel level = player.getLevel();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeam(msg.team);
            if (team == null) {
                // should never be the case
                return true;
            }

            if (!team.allowsVisits()) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.visit_team"));
                return true;
            }

            if (team.equals(data.getTeamFromPlayer(player))) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.visit_own_team"));
                return true;
            }

            WorldUtil.teleportToIsland(player, team);
            network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.visit_team", team.getName()));
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<VisitTeam> {

        @Override
        public Class<VisitTeam> messageClass() {
            return VisitTeam.class;
        }

        @Override
        public void encode(VisitTeam msg, FriendlyByteBuf buffer) {
            buffer.writeUUID(msg.team);
        }

        @Override
        public VisitTeam decode(FriendlyByteBuf buffer) {
            return new VisitTeam(buffer.readUUID());
        }
    }
}
