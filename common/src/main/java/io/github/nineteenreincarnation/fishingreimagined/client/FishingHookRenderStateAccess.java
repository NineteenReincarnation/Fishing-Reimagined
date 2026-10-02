package io.github.nineteenreincarnation.fishingreimagined.client;

public interface FishingHookRenderStateAccess {
    void fishingReimagined$setFightActive(boolean active);

    boolean fishingReimagined$isFightActive();

    void fishingReimagined$setTensionRatio(float tensionRatio);

    float fishingReimagined$tensionRatio();

    void fishingReimagined$setHookId(int hookId);

    int fishingReimagined$hookId();
}
