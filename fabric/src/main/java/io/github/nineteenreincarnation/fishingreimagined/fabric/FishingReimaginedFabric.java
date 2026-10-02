package io.github.nineteenreincarnation.fishingreimagined.fabric;

import io.github.nineteenreincarnation.fishingreimagined.FishingReimagined;
import io.github.nineteenreincarnation.fishingreimagined.fight.ServerFishingInput;
import io.github.nineteenreincarnation.fishingreimagined.network.ReelInputPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FishingReimaginedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FishingReimagined.initialize();

        PayloadTypeRegistry.serverboundPlay().register(
            ReelInputPayload.TYPE,
            ReelInputPayload.STREAM_CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(
            ReelInputPayload.TYPE,
            (payload, context) -> ServerFishingInput.update(
                context.player(),
                payload.action()
            )
        );
    }
}
