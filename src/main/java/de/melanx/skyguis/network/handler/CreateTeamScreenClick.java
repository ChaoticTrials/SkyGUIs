package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record CreateTeamScreenClick(String name, String shape) {

    public static class Handler implements PacketHandler<CreateTeamScreenClick> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(CreateTeamScreenClick msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            if (SkyblockHooks.onCreateTeam(msg.name)) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.create_team").withStyle(ChatFormatting.RED));
                return true;
            }

            ServerLevel level = player.getLevel();
            ConfiguredTemplate template = TemplateLoader.getConfiguredTemplate(msg.shape);
            if (template == null) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, ComponentBuilder.text("shape_does_not_exist").withStyle(ChatFormatting.RED));
                return true;
            }

            SkyblockSavedData data = SkyblockSavedData.get(level);

            if (data.hasPlayerTeam(player)) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_team").withStyle(ChatFormatting.RED));
                return true;
            }

            Team team = data.createTeam(msg.name, template);
            if (team == null) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.team_already_exist", msg.name).withStyle(ChatFormatting.RED));
                return true;
            }

            team.addPlayer(player);
            WorldUtil.teleportToIsland(player, team);
            network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.create_team", team.getName()).withStyle(ChatFormatting.GREEN));
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<CreateTeamScreenClick> {

        @Override
        public Class<CreateTeamScreenClick> messageClass() {
            return CreateTeamScreenClick.class;
        }

        @Override
        public void encode(CreateTeamScreenClick msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.name);
            buffer.writeUtf(msg.shape);
        }

        @Override
        public CreateTeamScreenClick decode(FriendlyByteBuf buffer) {
            return new CreateTeamScreenClick(buffer.readUtf(), buffer.readUtf());
        }
    }
}
