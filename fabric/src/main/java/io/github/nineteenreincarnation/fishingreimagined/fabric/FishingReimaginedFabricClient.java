package io.github.nineteenreincarnation.fishingreimagined.fabric;

import io.github.nineteenreincarnation.fishingreimagined.client.ClientFishingInput;
import io.github.nineteenreincarnation.fishingreimagined.client.ClientSpecialFishingMode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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

        KeyBindingHelper.registerKeyMapping(
            ClientSpecialFishingMode.TOGGLE_KEY
        );
    }
}
