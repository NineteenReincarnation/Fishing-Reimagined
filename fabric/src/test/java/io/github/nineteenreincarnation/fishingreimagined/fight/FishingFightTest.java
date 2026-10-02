package io.github.nineteenreincarnation.fishingreimagined.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

final class FishingFightTest {
    private static final FishBehavior GENTLE_FISH =
        (profile, random) -> snapshot ->
            new FishIntent(
                0.0,
                0.0,
                0.15,
                false,
                FishFightMode.TIRED
            );

    @Test
    void catchStartsAtZeroProgress() {
        FishingFight fight =
            create();

        assertEquals(
            0.0,
            fight.snapshot()
                .landingProgress(),
            1.0E-9
        );
    }

    @Test
    void holdingUseMovesCatchZoneRight() {
        FishingFight fight = create();

        double before =
            fight.snapshot().catchZonePosition();

        for (int i = 0; i < 8; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        assertTrue(
            fight.snapshot().catchZonePosition()
                > before
        );

        assertTrue(
            fight.snapshot().lineVelocity()
                > 0.0
        );
    }

    @Test
    void releasingMovesCatchZoneLeft() {
        FishingFight fight = create();

        for (int i = 0; i < 12; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        for (int i = 0; i < 18; i++) {
            fight.tick(ReelAction.PAY_OUT);
        }

        assertTrue(
            fight.snapshot().lineVelocity()
                < 0.0
        );
    }

    @Test
    void catchZoneHasControlInertia() {
        FishingFight fight = create();

        for (int i = 0; i < 10; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        double before =
            fight.snapshot().catchZonePosition();

        FightSnapshot afterRelease =
            fight.tick(ReelAction.PAY_OUT);

        assertTrue(
            afterRelease.catchZonePosition()
                >= before
        );
    }

    @Test
    void keepingFishInZoneBuildsProgress() {
        FishingFight fight = create();

        double before =
            fight.snapshot().landingProgress();

        for (int i = 0; i < 10; i++) {
            FightSnapshot snapshot =
                fight.snapshot();

            ReelAction action =
                snapshot.fishTrackPosition()
                        > snapshot.catchZonePosition()
                    ? ReelAction.REEL_IN
                    : ReelAction.PAY_OUT;

            fight.tick(action);
        }

        assertTrue(
            fight.snapshot().landingProgress()
                > before
        );
    }

    @Test
    void briefMissDoesNotInstantlyFail() {
        FishingFight fight = create();

        for (int i = 0; i < 24; i++) {
            fight.tick(ReelAction.REEL_IN);
        }

        assertFalse(
            fight.snapshot().isTerminal()
        );
    }

    @Test
    void trackingFeedbackLoopCanCatchFish() {
        FishingFight fight = create();
        FightSnapshot result =
            fight.snapshot();

        for (
            int i = 0;
            i < 1800 && !result.isTerminal();
            i++
        ) {
            double error =
                result.fishTrackPosition()
                    - result.catchZonePosition();

            double zoneVelocity =
                result.lineVelocity();

            ReelAction action =
                error - zoneVelocity * 4.0 > 0.0
                    ? ReelAction.REEL_IN
                    : ReelAction.PAY_OUT;

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

    private static FishingFight create() {
        return new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            GENTLE_FISH,
            new Random(7L),
            10.0,
            10.0
        );
    }
}
