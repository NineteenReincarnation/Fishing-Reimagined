package io.github.nineteenreincarnation.fishingreimagined.neoforge;

import io.github.nineteenreincarnation.fishingreimagined.FishingReimagined;
import io.github.nineteenreincarnation.fishingreimagined.client.ClientFishingInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@Mod(value = FishingReimagined.MOD_ID, dist = Dist.CLIENT)
public final class FishingReimaginedNeoForgeClient {
    public FishingReimaginedNeoForgeClient() {
        ClientFishingInput.installSender(ClientPacketDistributor::sendToServer);
    }
}
