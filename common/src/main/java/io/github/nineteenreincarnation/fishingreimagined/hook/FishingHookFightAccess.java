package io.github.nineteenreincarnation.fishingreimagined.hook;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface FishingHookFightAccess {
    boolean fishingReimagined$isFightActive();

    boolean fishingReimagined$isFishBiting();

    boolean fishingReimagined$isCaughtHanging();

    boolean fishingReimagined$startFight(
        Player player,
        InteractionHand hand
    );

    boolean fishingReimagined$takeCaughtFish(
        Player player
    );

    float fishingReimagined$tensionRatio();

    float fishingReimagined$catchProgress();

    float fishingReimagined$staminaRatio();

    float fishingReimagined$breakRisk();

    float fishingReimagined$fishVelocity();

    float fishingReimagined$lineVelocity();

    float fishingReimagined$dragSlip();

    float fishingReimagined$distance();

    float fishingReimagined$fishTrackPosition();

    float fishingReimagined$catchZonePosition();

    float fishingReimagined$catchZoneWidth();

    int fishingReimagined$fishState();

    int fishingReimagined$fishKindId();
}
