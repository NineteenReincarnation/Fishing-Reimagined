package io.github.nineteenreincarnation.fishingreimagined.network;

import io.github.nineteenreincarnation.fishingreimagined.FishingReimagined;
import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ReelInputPayload(byte actionId) implements CustomPacketPayload {
    public static final Type<ReelInputPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(FishingReimagined.MOD_ID, "reel_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReelInputPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public ReelInputPayload decode(RegistryFriendlyByteBuf buffer) {
                return new ReelInputPayload(buffer.readByte());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ReelInputPayload payload) {
                buffer.writeByte(payload.actionId());
            }
        };

    public ReelInputPayload(ReelAction action) {
        this((byte) action.ordinal());
    }

    public ReelAction action() {
        ReelAction[] actions = ReelAction.values();
        int index = Byte.toUnsignedInt(actionId);
        return index < actions.length ? actions[index] : ReelAction.HOLD;
    }

    @Override
    public Type<ReelInputPayload> type() {
        return TYPE;
    }
}
