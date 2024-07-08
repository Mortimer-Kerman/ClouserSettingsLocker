package net.mortimer_kerman.clouser_settingslocker;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class Payloads
{
    public record StringPayload(String strId, String value) implements CustomPayload
    {
        public static final CustomPayload.Id<StringPayload> ID = new CustomPayload.Id<>(PAYLOAD_STRING);
        public static final PacketCodec<RegistryByteBuf, StringPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, StringPayload::strId, PacketCodecs.STRING, StringPayload::value, StringPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record StringStringPayload(String strId, String value1, String value2) implements CustomPayload
    {
        public static final CustomPayload.Id<StringStringPayload> ID = new CustomPayload.Id<>(PAYLOAD_STRING_STRING);
        public static final PacketCodec<RegistryByteBuf, StringStringPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, StringStringPayload::strId, PacketCodecs.STRING, StringStringPayload::value1, PacketCodecs.STRING, StringStringPayload::value2, StringStringPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record StringBoolPayload(String strId, String valueS, boolean valueB) implements CustomPayload
    {
        public static final CustomPayload.Id<StringBoolPayload> ID = new CustomPayload.Id<>(PAYLOAD_STRING_BOOL);
        public static final PacketCodec<RegistryByteBuf, StringBoolPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, StringBoolPayload::strId, PacketCodecs.STRING, StringBoolPayload::valueS, PacketCodecs.BOOL, StringBoolPayload::valueB, StringBoolPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public static Identifier PAYLOAD_STRING = Identifier.of(ClouserSettingsLocker.MOD_ID, "string_payload");
    public static Identifier PAYLOAD_STRING_STRING = Identifier.of(ClouserSettingsLocker.MOD_ID, "string_string_payload");
    public static Identifier PAYLOAD_STRING_BOOL = Identifier.of(ClouserSettingsLocker.MOD_ID, "string_bool_payload");

    public static void RegisterPayloads() {
        PayloadTypeRegistry.playC2S().register(StringPayload.ID, StringPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StringPayload.ID, StringPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StringStringPayload.ID, StringStringPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StringStringPayload.ID, StringStringPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StringBoolPayload.ID, StringBoolPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StringBoolPayload.ID, StringBoolPayload.CODEC);
    }
}
