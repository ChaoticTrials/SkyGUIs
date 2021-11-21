package de.melanx.easyskyblockmanagement.network.handler;

import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
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
