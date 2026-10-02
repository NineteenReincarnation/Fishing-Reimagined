package io.github.nineteenreincarnation.fishingreimagined.fabric;

import io.github.nineteenreincarnation.fishingreimagined.client.ClientFishingInput;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FishingReimaginedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientFishingInput.installSender(ClientPlayNetworking::send);
    }
}
