package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyguis.client.screen.CreateTeamScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketSerializer;

import java.util.Objects;
import java.util.function.Supplier;

public class SendTemplateToClient {

    public static void handle(Message msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof CreateTeamScreen screen) {
                screen.addStructureToCache(msg.name, msg.template);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.name);
            buffer.writeNbt(msg.template.write(new CompoundTag()));
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message(buffer.readUtf(), ConfiguredTemplate.fromTag(Objects.requireNonNull(buffer.readAnySizeNbt())));
        }
    }

    public record Message(String name, ConfiguredTemplate template) {

    }
}
