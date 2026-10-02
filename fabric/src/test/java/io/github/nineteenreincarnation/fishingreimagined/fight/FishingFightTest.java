package io.github.nineteenreincarnation.fishingreimagined.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

final class FishingFightTest {
    private static final FishBehavior CALM_FISH =
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
    void holdingUseRaisesTension() {
        FishingFight fight =
            create(CALM_FISH);

        double before =
            fight.snapshot().tensionRatio();

        for (int i = 0; i < 4; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        assertTrue(
            fight.snapshot().tensionRatio()
                > before
        );
    }

    @Test
    void releasingUseLowersTension() {
        FishingFight fight =
            create(CALM_FISH);

        for (int i = 0; i < 14; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        double before =
            fight.snapshot().tensionRatio();

        for (int i = 0; i < 7; i++) {
            fight.tick(ReelAction.PAY_OUT);
        }

        assertTrue(
            fight.snapshot().tensionRatio()
                < before
        );
    }

    @Test
    void idealRhythmBuildsProgress() {
        FishingFight fight =
            create(CALM_FISH);

        double before =
            fight.snapshot().landingProgress();

        for (int i = 0; i < 20; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        assertTrue(
            fight.snapshot().landingProgress()
                > before
        );
    }

    @Test
    void progressHasPositiveMomentumAfterRelease() {
        FishingFight fight =
            create(CALM_FISH);

        for (int i = 0; i < 12; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        double before =
            fight.snapshot().landingProgress();

        fight.tick(ReelAction.PAY_OUT);

        assertTrue(
            fight.snapshot().landingProgress()
                >= before
        );
    }

    @Test
    void prolongedLowTensionPullsProgressBack() {
        FishingFight fight =
            create(CALM_FISH);

        for (int i = 0; i < 30; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        double earned =
            fight.snapshot().landingProgress();

        for (int i = 0; i < 35; i++) {
            fight.tick(ReelAction.PAY_OUT);
        }

        assertTrue(
            fight.snapshot().landingProgress()
                < earned
        );

        assertFalse(
            fight.snapshot().isTerminal()
        );
    }

    @Test
    void shortRedVisitDoesNotSnapLine() {
        FishingFight fight =
            create(BURST_FISH);

        FightSnapshot snapshot =
            fight.snapshot();

        for (int i = 0; i < 30; i++) {
            snapshot =
                fight.tick(ReelAction.REEL_IN);

            if (snapshot.breakRisk() > 0.0) {
                break;
            }
        }

        assertTrue(
            snapshot.breakRisk() > 0.0
        );

        double risk =
            snapshot.breakRisk();

        for (int i = 0; i < 8; i++) {
            snapshot =
                fight.tick(ReelAction.PAY_OUT);
        }

        assertFalse(
            snapshot.isTerminal()
        );

        assertTrue(
            snapshot.breakRisk() < risk
        );
    }

    @Test
    void sustainedRedZoneSnapsLine() {
        FishingFight fight =
            create(BURST_FISH);

        FightSnapshot result =
            fight.snapshot();

        for (
            int i = 0;
            i < 160
                && !result.isTerminal();
            i++
        ) {
            result =
                fight.tick(
                    ReelAction.REEL_IN
                );
        }

        assertEquals(
            FightPhase.LINE_BROKEN,
            result.phase()
        );

        assertEquals(
            1.0,
            result.breakRisk(),
            1.0E-9
        );
    }

    @Test
    void rhythmLoopCanCatchFish() {
        FishingFight fight =
            create(CALM_FISH);

        FightSnapshot result =
            fight.snapshot();

        for (
            int i = 0;
            i < 1000
                && !result.isTerminal();
            i++
        ) {
            ReelAction action;

            if (result.tensionRatio() > 0.72) {
                action =
                    ReelAction.PAY_OUT;
            } else if (
                result.tensionRatio() < 0.40
            ) {
                action =
                    ReelAction.REEL_IN;
            } else {
                action =
                    i % 9 < 6
                        ? ReelAction.REEL_IN
                        : ReelAction.PAY_OUT;
            }

            result =
                fight.tick(action);
        }

        assertEquals(
            FightPhase.CAUGHT,
            result.phase()
        );

        assertEquals(
            1.0,
            result.landingProgress(),
            1.0E-9
        );
    }

    private static FishingFight create(
        FishBehavior behavior
    ) {
        return new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            behavior,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );
    }
}
