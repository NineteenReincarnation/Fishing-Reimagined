package io.github.nineteenreincarnation.fishingreimagined.fabric;

import io.github.nineteenreincarnation.fishingreimagined.FishingReimagined;
import net.fabricmc.api.ModInitializer;

public final class FishingReimaginedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FishingReimagined.initialize();
    }
}
