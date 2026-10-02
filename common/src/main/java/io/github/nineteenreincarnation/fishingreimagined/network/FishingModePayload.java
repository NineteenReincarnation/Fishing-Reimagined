package io.github.nineteenreincarnation.fishingreimagined.network;

import io.github.nineteenreincarnation.fishingreimagined.FishingReimagined;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FishingModePayload(
    boolean enabled
) implements CustomPacketPayload {
    public static final Type<FishingModePayload>
        TYPE =
            new Type<>(
                Identifier.fromNamespaceAndPath(
                    FishingReimagined.MOD_ID,
                    "fishing_mode"
                )
            );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        FishingModePayload
    > STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public FishingModePayload decode(
                RegistryFriendlyByteBuf buffer
            ) {
                return new FishingModePayload(
                    buffer.readBoolean()
                );
            }

            @Override
            public void encode(
                RegistryFriendlyByteBuf buffer,
                FishingModePayload payload
            ) {
                buffer.writeBoolean(
                    payload.enabled()
                );
            }
        };

    @Override
    public Type<FishingModePayload> type() {
        return TYPE;
    }
}
