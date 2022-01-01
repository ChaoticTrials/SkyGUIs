package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EditSpawns {

    public static void handle(Message msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            ServerLevel level = (ServerLevel) player.getCommandSenderWorld();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeamFromPlayer(player);
            if (team == null) {
                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED));
                return;
            }

            switch (msg.type) {
                // handle adding spawns
                case ADD -> {
                    Event.Result result = SkyblockHooks.onAddSpawn(player, team, msg.pos).getLeft();
                    switch (result) {
                        case DENY -> {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.denied.create_spawn"));
                            return;
                        }
                        case DEFAULT -> {
                            if ((!ConfigHandler.Utility.selfManage || !ConfigHandler.Utility.Spawns.modifySpawns) && !player.hasPermissions(2)) {
                                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.disabled.modify_spawns"));
                                return;
                            }

                            Vec3i templateSize = TemplateData.get(level).getTemplate().getSize();
                            BlockPos center = team.getIsland().getCenter().mutable();
                            center.offset(templateSize.getX() / 2, templateSize.getY() / 2, templateSize.getZ() / 2);
                            if (!msg.pos.closerThan(center, ConfigHandler.Utility.Spawns.range)) {
                                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.error.position_too_far_away"));
                                return;
                            }
                        }
                    }

                    team.addPossibleSpawn(msg.pos); // TODO send fail if not added
                    network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, new TranslatableComponent("skyblockbuilder.command.success.spawn_added", msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()));
                    return;
                }

                // handle removing spawns
                case REMOVE -> {
                    if (level != level.getServer().getLevel(ConfigHandler.Spawn.dimension.getResourceKey())) {
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.error.wrong_position"));
                        return;
                    }

                    Event.Result result = SkyblockHooks.onRemoveSpawn(player, team, msg.pos);
                    switch (result) {
                        case DENY -> {
                            network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.denied.modify_spawns0"));
                            return;
                        }
                        case DEFAULT -> {
                            if ((!ConfigHandler.Utility.selfManage || !ConfigHandler.Utility.Spawns.modifySpawns) && !player.hasPermissions(2)) {
                                network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, new TranslatableComponent("skyblockbuilder.command.disabled.modify_spawns"));
                                return;
                            }
                        }
                    }

                    if (!team.removePossibleSpawn(msg.pos)) {
                        TranslatableComponent answer = new TranslatableComponent("skyblockbuilder.command.error.remove_spawn0");
                        if (team.getPossibleSpawns().size() <= 1) {
                            answer.append(" ").append(new TranslatableComponent("skyblockbuilder.command.error.remove_spawn1"));
                        }
                        network.handleLoadingResult(ctx, LoadingResult.Status.FAIL, answer);
                        return;
                    }

                    network.handleLoadingResult(ctx, LoadingResult.Status.SUCCESS, new TranslatableComponent("skyblockbuilder.command.success.spawn_removed", msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()));
                    return;
                }

                // handle resetting spawns
//                case RESET -> {
//                    Event.Result result = SkyblockHooks.onResetSpawns(player, team);
//                    switch (result) {
//                        case ALLOW -> ;
//                        case DENY -> ;
//                        case DEFAULT -> ;
//                    }
//                }
            }
            ctx.setPacketHandled(true);
        });
    }

    public static class Serializer implements PacketSerializer<Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeEnum(msg.type);
            buffer.writeBlockPos(msg.pos);
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message(buffer.readEnum(Type.class), buffer.readBlockPos());
        }
    }

    public record Message(Type type, BlockPos pos) {
    }

    public enum Type {
        ADD,
        REMOVE,
        RESET
    }
}
