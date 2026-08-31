package de.melanx.skyguis.network.handler;

import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.CreateTeamScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class SendTemplateToClient extends PacketHandler<SendTemplateToClient.Message> {

    public static final CustomPacketPayload.Type<SendTemplateToClient.Message> TYPE = new CustomPacketPayload.Type<>(SkyGUIs.getInstance().id("send_template_to_client"));

    public SendTemplateToClient() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (Minecraft.getInstance().screen instanceof CreateTeamScreen screen) {
            screen.addStructureToCache(msg.name, msg.template);
        }
    }

    public record Message(String name, ConfiguredTemplate template) implements CustomPacketPayload {

        public static final StreamCodec<FriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    buffer.writeUtf(msg.name);
                    buffer.writeNbt(ConfiguredTemplate.CODEC.encodeStart(NbtOps.INSTANCE, msg.template).getOrThrow());
                }, buffer -> new Message(buffer.readUtf(), ConfiguredTemplate.CODEC.parse(NbtOps.INSTANCE, buffer.readNbt(NbtAccounter.unlimitedHeap())).getOrThrow())
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SendTemplateToClient.TYPE;
        }
    }
}
