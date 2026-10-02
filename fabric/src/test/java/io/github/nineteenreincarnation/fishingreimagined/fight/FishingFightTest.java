package io.github.nineteenreincarnation.fishingreimagined.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

final class FishingFightTest {
    private static final FishBehavior STILL_FISH =
        (profile, random) ->
            snapshot -> FishIntent.CALM;

    private static FishBehavior hardPull(
        double outwardVelocity
    ) {
        return (profile, random) ->
            snapshot ->
                new FishIntent(
                    outwardVelocity,
                    0.0,
                    1.0,
                    true,
                    FishFightMode.BURST
                );
    }

    @Test
    void hookSetStartsWithPhysicalPreload() {
        FishingFight fight =
            new FishingFight(
                FishProfile.PROTOTYPE,
                LineProfile.PROTOTYPE,
                STILL_FISH,
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        FightSnapshot snapshot =
            fight.snapshot();

        assertTrue(
            snapshot.tensionRatio()
                > 0.25
        );

        assertTrue(
            snapshot.lineLength()
                < snapshot.distance()
        );
    }

    @Test
    void reelingActuallyShortensSpoolLine() {
        FishingFight fight =
            new FishingFight(
                FishProfile.PROTOTYPE,
                LineProfile.PROTOTYPE,
                STILL_FISH,
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        double before =
            fight.snapshot().lineLength();

        for (int i = 0; i < 6; i++) {
            fight.tick(
                ReelAction.REEL_IN
            );
        }

        FightSnapshot after =
            fight.snapshot();

        assertTrue(
            after.lineLength()
                < before
        );

        assertTrue(
            after.lineVelocity()
                < 0.0
        );
    }

    @Test
    void payingOutRelievesLoadWithInertia() {
        FishingFight fight =
            new FishingFight(
                FishProfile.PROTOTYPE,
                LineProfile.PROTOTYPE,
                STILL_FISH,
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        for (int i = 0; i < 10; i++) {
            fight.tick(
                ReelAction.REEL_IN
            );
        }

        double loaded =
            fight.snapshot()
                .tensionRatio();

        for (int i = 0; i < 5; i++) {
            fight.tick(
                ReelAction.PAY_OUT
            );
        }

        FightSnapshot relieved =
            fight.snapshot();

        assertTrue(
            relieved.lineVelocity()
                > 0.0
        );

        assertTrue(
            relieved.tensionRatio()
                < loaded
        );
    }

    @Test
    void strongFishCanTriggerAutomaticDrag() {
        FishProfile strong =
            new FishProfile(
                0.030,
                2.0,
                120.0,
                0.05,
                4.5,
                10,
                24,
                0.8,
                0.05,
                60,
                0.22
            );

        FishingFight fight =
            new FishingFight(
                strong,
                LineProfile.PROTOTYPE,
                hardPull(0.15),
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        boolean sawDrag = false;

        for (
            int i = 0;
            i < 40
                && !fight
                    .snapshot()
                    .isTerminal();
            i++
        ) {
            FightSnapshot snapshot =
                fight.tick(
                    ReelAction.REEL_IN
                );

            if (
                snapshot.dragSlip()
                    > 0.005
            ) {
                sawDrag = true;
                break;
            }
        }

        assertTrue(sawDrag);
    }

    @Test
    void fishMomentumPersistsWhileLinePaysOut() {
        FishingFight fight =
            new FishingFight(
                FishProfile.PROTOTYPE,
                LineProfile.PROTOTYPE,
                hardPull(0.12),
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        for (int i = 0; i < 6; i++) {
            fight.tick(
                ReelAction.REEL_IN
            );
        }

        FightSnapshot released =
            fight.tick(
                ReelAction.PAY_OUT
            );

        assertTrue(
            released.fishVelocity()
                > 0.0
        );

        assertTrue(
            released.lineVelocity()
                > 0.0
        );
    }

    @Test
    void releasingLineCanRecoverSnapRisk() {
        FishProfile strong =
            new FishProfile(
                0.030,
                1.8,
                120.0,
                0.05,
                4.5,
                10,
                24,
                0.8,
                0.05,
                60,
                0.22
            );

        FishingFight fight =
            new FishingFight(
                strong,
                LineProfile.PROTOTYPE,
                hardPull(0.15),
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        double risk = 0.0;

        for (int i = 0; i < 30; i++) {
            FightSnapshot snapshot =
                fight.tick(
                    ReelAction.REEL_IN
                );

            risk = snapshot.breakRisk();

            if (
                risk > 0.10
                    && !snapshot.isTerminal()
            ) {
                break;
            }
        }

        assertTrue(risk > 0.0);

        for (
            int i = 0;
            i < 8
                && !fight
                    .snapshot()
                    .isTerminal();
            i++
        ) {
            fight.tick(
                ReelAction.PAY_OUT
            );
        }

        assertTrue(
            fight.snapshot().breakRisk()
                < risk
        );
    }

    @Test
    void sustainedOverloadStillSnapsTheLine() {
        FishProfile brute =
            new FishProfile(
                0.040,
                2.2,
                150.0,
                0.10,
                5.0,
                12,
                28,
                0.7,
                0.04,
                60,
                0.20
            );

        FishingFight fight =
            new FishingFight(
                brute,
                LineProfile.PROTOTYPE,
                hardPull(0.19),
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        FightSnapshot result =
            fight.snapshot();

        for (
            int i = 0;
            i < 80
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
            result.breakRisk()
        );
    }

    @Test
    void sustainedSlackLetsFishEscape() {
        FishingFight fight =
            new FishingFight(
                FishProfile.PROTOTYPE,
                LineProfile.PROTOTYPE,
                STILL_FISH,
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        FightSnapshot result =
            fight.snapshot();

        for (
            int i = 0;
            i < 100
                && !result.isTerminal();
            i++
        ) {
            result =
                fight.tick(
                    ReelAction.PAY_OUT
                );
        }

        assertEquals(
            FightPhase.ESCAPED,
            result.phase()
        );
    }

    @Test
    void controlledReelingBringsFishPhysicallyCloser() {
        FishingFight fight =
            new FishingFight(
                FishProfile.PROTOTYPE,
                LineProfile.PROTOTYPE,
                STILL_FISH,
                RandomGenerator.getDefault(),
                10.0,
                10.0
            );

        FightSnapshot result =
            fight.snapshot();

        for (
            int i = 0;
            i < 420
                && !result.isTerminal();
            i++
        ) {
            ReelAction action =
                result.tensionRatio()
                        > 0.68
                    ? ReelAction.PAY_OUT
                    : ReelAction.REEL_IN;

            result =
                fight.tick(action);
        }

        assertEquals(
            FightPhase.CAUGHT,
            result.phase()
        );

        assertEquals(
            1.0,
            result.landingProgress()
        );

        assertTrue(
            result.distance()
                <= LineProfile.PROTOTYPE
                    .catchDistance()
                    + 0.12
        );
    }
}
