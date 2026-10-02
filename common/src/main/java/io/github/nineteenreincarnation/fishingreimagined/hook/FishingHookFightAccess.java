package io.github.nineteenreincarnation.fishingreimagined.hook;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface FishingHookFightAccess {
    boolean fishingReimagined$isFightActive();

    boolean fishingReimagined$isFishBiting();

    boolean fishingReimagined$startFight(Player player, InteractionHand hand);

    float fishingReimagined$tensionRatio();

    float fishingReimagined$catchProgress();

    float fishingReimagined$staminaRatio();

    float fishingReimagined$breakRisk();

    int fishingReimagined$fishState();

    int fishingReimagined$fishKindId();
}
