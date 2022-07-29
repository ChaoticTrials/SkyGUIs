package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.network.EasyNetwork;
import de.melanx.skyguis.util.LoadingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record EditSpawns(Type type, BlockPos pos) {

    public static class Handler implements PacketHandler<EditSpawns> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(EditSpawns msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            EasyNetwork network = SkyGUIs.getNetwork();
            ServerLevel level = (ServerLevel) player.getCommandSenderWorld();
            SkyblockSavedData data = SkyblockSavedData.get(level);
            Team team = data.getTeamFromPlayer(player);
            if (team == null) {
                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED));
                return true;
            }

            switch (msg.type) {
                // handle adding spawns
                case ADD -> {
                    Event.Result result = SkyblockHooks.onAddSpawn(player, team, msg.pos).getLeft();
                    switch (result) {
                        case DENY -> {
                            network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.create_spawn"));
                            return true;
                        }
                        case DEFAULT -> {
                            if ((!ConfigHandler.Utility.selfManage || !ConfigHandler.Utility.Spawns.modifySpawns) && !player.hasPermissions(2)) {
                                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.modify_spawns"));
                                return true;
                            }

                            Vec3i templateSize = TemplateData.get(level).getConfiguredTemplate().getTemplate().getSize();
                            BlockPos center = team.getIsland().getCenter().mutable();
                            center.offset(templateSize.getX() / 2, templateSize.getY() / 2, templateSize.getZ() / 2);
                            if (!msg.pos.closerThan(center, ConfigHandler.Utility.Spawns.range)) {
                                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.position_too_far_away"));
                                return true;
                            }
                        }
                    }

                    team.addPossibleSpawn(msg.pos); // TODO send fail if not added
                    network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.spawn_added", msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()));
                    return true;
                }

                // handle removing spawns
                case REMOVE -> {
                    if (level != level.getServer().getLevel(ConfigHandler.Spawn.dimension)) {
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.error.wrong_position"));
                        return true;
                    }

                    Event.Result result = SkyblockHooks.onRemoveSpawn(player, team, msg.pos);
                    switch (result) {
                        case DENY -> {
                            network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.denied.modify_spawns0"));
                            return true;
                        }
                        case DEFAULT -> {
                            if ((!ConfigHandler.Utility.selfManage || !ConfigHandler.Utility.Spawns.modifySpawns) && !player.hasPermissions(2)) {
                                network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, Component.translatable("skyblockbuilder.command.disabled.modify_spawns"));
                                return true;
                            }
                        }
                    }

                    if (!team.removePossibleSpawn(msg.pos)) {
                        MutableComponent answer = Component.translatable("skyblockbuilder.command.error.remove_spawn0");
                        if (team.getPossibleSpawns().size() <= 1) {
                            answer.append(" ").append(Component.translatable("skyblockbuilder.command.error.remove_spawn1"));
                        }
                        network.handleLoadingResult(ctx.get(), LoadingResult.Status.FAIL, answer);
                        return true;
                    }

                    network.handleLoadingResult(ctx.get(), LoadingResult.Status.SUCCESS, Component.translatable("skyblockbuilder.command.success.spawn_removed", msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()));
                    return true;
                }

                // TODO case RESET
            }
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<EditSpawns> {

        @Override
        public Class<EditSpawns> messageClass() {
            return EditSpawns.class;
        }

        @Override
        public void encode(EditSpawns msg, FriendlyByteBuf buffer) {
            buffer.writeEnum(msg.type);
            buffer.writeBlockPos(msg.pos);
        }

        @Override
        public EditSpawns decode(FriendlyByteBuf buffer) {
            return new EditSpawns(buffer.readEnum(Type.class), buffer.readBlockPos());
        }
    }

    public enum Type {
        ADD,
        REMOVE,
        RESET
    }
}
