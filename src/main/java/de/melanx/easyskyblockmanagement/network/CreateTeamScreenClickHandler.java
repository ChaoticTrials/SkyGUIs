package de.melanx.easyskyblockmanagement.network;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.WorldUtil;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CreateTeamScreenClickHandler {

    public static void handle(CreateTeamScreenClickHandler.Message msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }

            if (SkyblockHooks.onCreateTeam(msg.name)) {
                player.sendMessage(new TranslatableComponent("skyblockbuilder.command.denied.create_team").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                return;
            }

            ServerLevel level = player.getLevel();
            ConfiguredTemplate template = TemplateLoader.getConfiguredTemplate(msg.shape);
            if (template == null) {
                player.sendMessage(new TranslatableComponent("This shape does not exist."), UUID.randomUUID()); // TODO real name
                return;
            }

            Team team = SkyblockSavedData.get(level).createTeam(msg.name, template.getTemplate());
            if (team == null) {
                player.sendMessage(new TranslatableComponent("This team already exists"), UUID.randomUUID()); // TODO real name
                return;
            }

            team.addPlayer(player);
            WorldUtil.teleportToIsland(player, team);
            // TODO add message for player and for team
        });
        context.get().setPacketHandled(true);
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
