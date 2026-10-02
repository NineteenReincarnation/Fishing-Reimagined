package io.github.nineteenreincarnation.fishingreimagined.neoforge;

import io.github.nineteenreincarnation.fishingreimagined.FishingReimagined;
import io.github.nineteenreincarnation.fishingreimagined.client.ClientFishingInput;
import io.github.nineteenreincarnation.fishingreimagined.client.ClientSpecialFishingMode;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@Mod(
    value = FishingReimagined.MOD_ID,
    dist = Dist.CLIENT
)
public final class FishingReimaginedNeoForgeClient {
    public FishingReimaginedNeoForgeClient(
        IEventBus modEventBus
    ) {
        ClientFishingInput.installSender(
            ClientPacketDistributor::sendToServer
        );

        ClientSpecialFishingMode.installSender(
            ClientPacketDistributor::sendToServer
        );

        modEventBus.addListener(
            this::registerKeyMappings
        );
    }

    private void registerKeyMappings(
        RegisterKeyMappingsEvent event
    ) {
        event.register(
            ClientSpecialFishingMode.TOGGLE_KEY
        );
    }
}
