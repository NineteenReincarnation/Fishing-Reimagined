package io.github.nineteenreincarnation.fishingreimagined.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

final class FishingFightTest {
    private static final FishBehavior STILL_FISH =
        (profile, random) -> snapshot ->
            new FishIntent(
                0.0,
                0.0,
                0.2,
                false,
                FishFightMode.PROBING
            );

    private static final FishBehavior BURST_FISH =
        (profile, random) -> snapshot ->
            new FishIntent(
                0.12,
                0.0,
                1.0,
                true,
                FishFightMode.BURST
            );

    @Test
    void reelingRaisesTensionAndProgress() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        FightSnapshot before = fight.snapshot();
        FightSnapshot after = fight.tick(ReelAction.REEL_IN);

        assertTrue(after.tensionRatio() > before.tensionRatio());
        assertTrue(after.landingProgress() > before.landingProgress());
    }

    @Test
    void releasingLowersTensionWithoutDeletingProgress() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        for (int i = 0; i < 10; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        double progress = fight.snapshot().landingProgress();
        double tension = fight.snapshot().tensionRatio();

        for (int i = 0; i < 5; i++) {
            fight.tick(ReelAction.PAY_OUT);
        }

        assertTrue(fight.snapshot().tensionRatio() < tension);
        assertEquals(progress, fight.snapshot().landingProgress(), 1.0E-9);
    }

    @Test
    void slackDoesNotAutomaticallyRetractOrLoseFish() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        for (int i = 0; i < 240; i++) {
            fight.tick(ReelAction.PAY_OUT);
        }

        assertFalse(fight.snapshot().isTerminal());
        assertEquals(FightPhase.FIGHTING, fight.snapshot().phase());
    }

    @Test
    void burstBuildsTensionFasterThanCalmFish() {
        FishingFight calm = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );
        FishingFight burst = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            BURST_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        calm.tick(ReelAction.REEL_IN);
        burst.tick(ReelAction.REEL_IN);

        assertTrue(
            burst.snapshot().tensionRatio()
                > calm.snapshot().tensionRatio()
        );
    }

    @Test
    void sustainedRedZoneSnapsLine() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            BURST_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        FightSnapshot result = fight.snapshot();

        for (int i = 0; i < 100 && !result.isTerminal(); i++) {
            result = fight.tick(ReelAction.REEL_IN);
        }

        assertEquals(FightPhase.LINE_BROKEN, result.phase());
        assertEquals(1.0, result.breakRisk(), 1.0E-9);
    }

    @Test
    void simpleBalanceLoopCanCatchFish() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        FightSnapshot result = fight.snapshot();

        for (int i = 0; i < 700 && !result.isTerminal(); i++) {
            ReelAction action = result.tensionRatio() > 0.72
                ? ReelAction.PAY_OUT
                : ReelAction.REEL_IN;

            result = fight.tick(action);
        }

        assertEquals(FightPhase.CAUGHT, result.phase());
        assertEquals(1.0, result.landingProgress(), 1.0E-9);
    }
}
