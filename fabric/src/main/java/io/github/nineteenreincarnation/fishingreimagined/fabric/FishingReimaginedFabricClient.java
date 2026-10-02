package io.github.nineteenreincarnation.fishingreimagined.fabric;

import io.github.nineteenreincarnation.fishingreimagined.client.ClientFishingInput;
import io.github.nineteenreincarnation.fishingreimagined.client.ClientSpecialFishingMode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FishingReimaginedFabricClient
    implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientFishingInput.installSender(
            ClientPlayNetworking::send
        );

        ClientSpecialFishingMode.installSender(
            ClientPlayNetworking::send
        );

        KeyMappingHelper.registerKeyMapping(
            ClientSpecialFishingMode.TOGGLE_KEY
        );
    }
}
