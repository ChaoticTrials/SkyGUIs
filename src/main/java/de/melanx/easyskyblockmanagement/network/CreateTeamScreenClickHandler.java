package de.melanx.easyskyblockmanagement.network;

import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
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

public class CreateTeamScreenClickHandler {

    public static void handle(CreateTeamScreenClickHandler.Message msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            if (SkyblockHooks.onCreateTeam(msg.name)) {
                EasySkyblockManagement.getNetwork().handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.denied.create_team").withStyle(ChatFormatting.RED)); // TODO real name
                return;
            }

            ServerLevel level = player.getLevel();
            ConfiguredTemplate template = TemplateLoader.getConfiguredTemplate(msg.shape);
            if (template == null) {
                EasySkyblockManagement.getNetwork().handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("This shape does not exist.")); // TODO real name
                return;
            }

            SkyblockSavedData data = SkyblockSavedData.get(level);

            if (data.hasPlayerTeam(player)) {
                EasySkyblockManagement.getNetwork().handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("You already have a team!!!11!").withStyle(ChatFormatting.RED)); // TODO real name
                return;
            }

            Team team = data.createTeam(msg.name, template.getTemplate());
            if (team == null) {
                EasySkyblockManagement.getNetwork().handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("This team already exists.")); // TODO real name
                return;
            }

            team.addPlayer(player);
            WorldUtil.teleportToIsland(player, team);
            // TODO add message for player and for team
            EasySkyblockManagement.getNetwork().handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.success.create_team", team.getName()));
        });
        ctx.setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<CreateTeamScreenClickHandler.Message> {

        @Override
        public Class<CreateTeamScreenClickHandler.Message> messageClass() {
            return CreateTeamScreenClickHandler.Message.class;
        }

        @Override
        public void encode(CreateTeamScreenClickHandler.Message msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.name);
            buffer.writeUtf(msg.shape);
        }

        @Override
        public CreateTeamScreenClickHandler.Message decode(FriendlyByteBuf buffer) {
            return new CreateTeamScreenClickHandler.Message(buffer.readUtf(), buffer.readUtf());
        }
    }

    public record Message(String name, String shape) {
        // empty
    }
}
