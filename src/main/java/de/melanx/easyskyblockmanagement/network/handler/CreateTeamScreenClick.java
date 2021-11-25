package de.melanx.easyskyblockmanagement.network.handler;

import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.network.EasyNetwork;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import de.melanx.easyskyblockmanagement.util.LoadingResult;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.WorldUtil;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class CreateTeamScreenClick {

    public static void handle(CreateTeamScreenClick.Message msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            EasyNetwork network = EasySkyblockManagement.getNetwork();
            if (SkyblockHooks.onCreateTeam(msg.name)) {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.denied.create_team").withStyle(ChatFormatting.RED));
                return;
            }

            ServerLevel level = player.getLevel();
            ConfiguredTemplate template = TemplateLoader.getConfiguredTemplate(msg.shape);
            if (template == null) {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, ComponentBuilder.text("shape_does_not_exist").withStyle(ChatFormatting.RED));
                return;
            }

            SkyblockSavedData data = SkyblockSavedData.get(level);

            if (data.hasPlayerTeam(player)) {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.error.user_has_team").withStyle(ChatFormatting.RED));
                return;
            }

            Team team = data.createTeam(msg.name, template.getTemplate());
            if (team == null) {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.error.team_already_exist", msg.name).withStyle(ChatFormatting.RED));
                return;
            }

            team.addPlayer(player);
            WorldUtil.teleportToIsland(player, team);
            network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, new TranslatableComponent("skyblockbuilder.command.success.create_team", team.getName()).withStyle(ChatFormatting.GREEN));
        });
        ctx.setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<CreateTeamScreenClick.Message> {

        @Override
        public Class<CreateTeamScreenClick.Message> messageClass() {
            return CreateTeamScreenClick.Message.class;
        }

        @Override
        public void encode(CreateTeamScreenClick.Message msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.name);
            buffer.writeUtf(msg.shape);
        }

        @Override
        public CreateTeamScreenClick.Message decode(FriendlyByteBuf buffer) {
            return new CreateTeamScreenClick.Message(buffer.readUtf(), buffer.readUtf());
        }
    }

    public record Message(String name, String shape) {
        // empty
    }
}
