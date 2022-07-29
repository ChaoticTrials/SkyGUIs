package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyguis.client.screen.CreateTeamScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.Objects;
import java.util.function.Supplier;

public record SendTemplateToClient(String name, ConfiguredTemplate template) {

    public static class Handler implements PacketHandler<SendTemplateToClient> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(SendTemplateToClient msg, Supplier<NetworkEvent.Context> ctx) {
            if (Minecraft.getInstance().screen instanceof CreateTeamScreen screen) {
                screen.addStructureToCache(msg.name, msg.template);
            }
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<SendTemplateToClient> {

        @Override
        public Class<SendTemplateToClient> messageClass() {
            return SendTemplateToClient.class;
        }

        @Override
        public void encode(SendTemplateToClient msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.name);
            buffer.writeNbt(msg.template.write(new CompoundTag()));
        }

        @Override
        public SendTemplateToClient decode(FriendlyByteBuf buffer) {
            return new SendTemplateToClient(buffer.readUtf(), ConfiguredTemplate.fromTag(Objects.requireNonNull(buffer.readAnySizeNbt())));
        }
    }
}
