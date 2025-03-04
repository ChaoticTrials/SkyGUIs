package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class CreateTeamScreenClick extends PacketHandler<CreateTeamScreenClick.Message> {

    public static final CustomPacketPayload.Type<CreateTeamScreenClick.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().resource("create_team_screen_click"));

    public CreateTeamScreenClick() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();

        EasyNetwork network = SkyGUIs.getNetwork();
        if (SkyblockHooks.onCreateTeam(msg.name)) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.DENIED_CREATE_TEAM);
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        ConfiguredTemplate template = TemplateLoader.getConfiguredTemplate(msg.shape);
        if (template == null) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, ComponentBuilder.text("shape_does_not_exist").withStyle(ChatFormatting.RED));
            return;
        }

        SkyblockSavedData data = SkyblockSavedData.get(level);

        if (data.hasPlayerTeam(player)) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_USER_HAS_TEAM);
            return;
        }

        Team team = data.createTeam(msg.name, template);
        if (team == null) {
            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, SkyComponents.ERROR_TEAM_ALREADY_EXIST.apply(msg.name));
            return;
        }

        data.addPlayerToTeam(team, player);
        WorldUtil.teleportToIsland(player, team);
        team.setAllowVisit(msg.allowVisits);
        team.setAllowJoinRequest(msg.allowJoinRequests);
        network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, SkyComponents.SUCCESS_CREATE_TEAM.apply(team.getName()).withStyle(ChatFormatting.GREEN));
    }

    public record Message(String name, String shape, boolean allowVisits, boolean allowJoinRequests) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, CreateTeamScreenClick.Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> {
                    buffer.writeUtf(msg.name);
                    buffer.writeUtf(msg.shape);
                    buffer.writeBoolean(msg.allowVisits);
                    buffer.writeBoolean(msg.allowJoinRequests);
                }), buffer -> new CreateTeamScreenClick.Message(
                        buffer.readUtf(),
                        buffer.readUtf(),
                        buffer.readBoolean(),
                        buffer.readBoolean()
                ));

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return CreateTeamScreenClick.TYPE;
        }
    }
}
