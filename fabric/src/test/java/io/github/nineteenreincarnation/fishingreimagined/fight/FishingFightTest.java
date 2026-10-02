package io.github.nineteenreincarnation.fishingreimagined.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

final class FishingFightTest {
    private static final FishBehavior STILL_FISH =
        (profile, random) -> snapshot -> FishIntent.CALM;

    @Test
    void holdingReelRaisesTension() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        double before = fight.snapshot().tensionRatio();
        double after = fight.tick(ReelAction.REEL_IN).tensionRatio();

        assertTrue(after > before);
    }

    @Test
    void releasingLineLowersTension() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        fight.tick(ReelAction.REEL_IN);
        double before = fight.snapshot().tensionRatio();
        double after = fight.tick(ReelAction.PAY_OUT).tensionRatio();

        assertTrue(after < before);
    }

    @Test
    void sweetSpotBuildsCatchProgress() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        double before = fight.snapshot().landingProgress();
        FightSnapshot result = fight.tick(ReelAction.HOLD);

        assertTrue(result.landingProgress() > before);
    }

    @Test
    void tooMuchSlackLosesProgress() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        for (int i = 0; i < 8; i++) {
            fight.tick(ReelAction.HOLD);
        }

        double gained = fight.snapshot().landingProgress();

        while (fight.snapshot().tensionRatio() >= 0.08) {
            fight.tick(ReelAction.PAY_OUT);
        }

        FightSnapshot result = fight.tick(ReelAction.PAY_OUT);
        assertTrue(result.landingProgress() < gained);
    }

    @Test
    void sustainedRedZoneBreaksLine() {
        FishBehavior hardPull =
            (profile, random) ->
                snapshot ->
                    new FishIntent(0.12, 0.0, 1.0, true);

        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            hardPull,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        FightSnapshot result = fight.snapshot();

        for (int i = 0; i < 80 && !result.isTerminal(); i++) {
            result = fight.tick(ReelAction.REEL_IN);
        }

        assertEquals(FightPhase.LINE_BROKEN, result.phase());
        assertEquals(1.0, result.breakRisk());
    }

    @Test
    void releasingFromDangerRecoversBreakRisk() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        for (int i = 0; i < 18; i++) {
            FightSnapshot snapshot = fight.tick(ReelAction.REEL_IN);
            if (snapshot.breakRisk() > 0.0) {
                break;
            }
        }

        double risk = fight.snapshot().breakRisk();
        assertTrue(risk > 0.0);

        for (int i = 0; i < 8; i++) {
            fight.tick(ReelAction.PAY_OUT);
        }

        assertTrue(fight.snapshot().breakRisk() < risk);
    }

    @Test
    void completingProgressCatchesFish() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        FightSnapshot result = fight.snapshot();

        for (int i = 0; i < 500 && !result.isTerminal(); i++) {
            ReelAction action =
                result.tensionRatio() > 0.58
                    ? ReelAction.PAY_OUT
                    : ReelAction.REEL_IN;

            result = fight.tick(action);
        }

        assertEquals(FightPhase.CAUGHT, result.phase());
        assertEquals(1.0, result.landingProgress());
    }
}
