package io.github.nineteenreincarnation.fishingreimagined.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

final class FishingFightTest {
    private static final FishBehavior STILL_FISH =
        (profile, random) -> snapshot -> FishIntent.CALM;

    @Test
    void reelingInCreatesLoadAndShortensTheLine() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            10.0
        );

        FightSnapshot before = fight.snapshot();
        FightSnapshot after =
            fight.tick(ReelAction.REEL_IN);

        assertTrue(
            after.lineLength() < before.lineLength()
        );
        assertTrue(
            after.tension() > before.tension()
        );
    }

    @Test
    void controlledReelingAdvancesLandingProgress() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            8.0
        );

        FightSnapshot before = fight.snapshot();
        FightSnapshot after =
            fight.tick(ReelAction.REEL_IN);

        assertTrue(
            after.landingProgress()
                > before.landingProgress()
        );
        assertTrue(after.distance() < before.distance());
    }

    @Test
    void payingOutRelievesExistingTension() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            9.6
        );

        FightSnapshot before = fight.snapshot();
        FightSnapshot after =
            fight.tick(ReelAction.PAY_OUT);

        assertTrue(
            after.tension() < before.tension()
        );
    }

    @Test
    void slackCanRemovePreviouslyEarnedProgress() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            8.0
        );

        FightSnapshot gained =
            fight.tick(ReelAction.REEL_IN);

        while (fight.snapshot().tensionRatio() >= 0.08) {
            fight.tick(ReelAction.PAY_OUT);
        }

        FightSnapshot afterSlack =
            fight.tick(ReelAction.HOLD);

        assertTrue(
            afterSlack.landingProgress()
                < gained.landingProgress()
        );
    }

    @Test
    void sustainedSlackLetsTheFishEscape() {
        FishProfile easyEscape = new FishProfile(
            0.0,
            1.0,
            100.0,
            0.0,
            1.0,
            1,
            1,
            0.0,
            0.0,
            3,
            0.25
        );

        FishingFight fight = new FishingFight(
            easyEscape,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            12.0
        );

        fight.tick(ReelAction.HOLD);
        fight.tick(ReelAction.HOLD);
        FightSnapshot result =
            fight.tick(ReelAction.HOLD);

        assertEquals(
            FightPhase.ESCAPED,
            result.phase()
        );
    }

    @Test
    void sustainedOverloadBreaksTheLine() {
        LineProfile fragileLine = new LineProfile(
            10.0,
            1.0,
            50.0,
            0.05,
            0.1,
            0.01,
            2,
            1.0,
            40.0
        );

        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            fragileLine,
            STILL_FISH,
            RandomGenerator.getDefault(),
            10.0,
            9.0
        );

        fight.tick(ReelAction.HOLD);
        FightSnapshot result =
            fight.tick(ReelAction.HOLD);

        assertEquals(
            FightPhase.LINE_BROKEN,
            result.phase()
        );
    }

    @Test
    void fishWithinLandingRangeCanBeCaught() {
        FishingFight fight = new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            STILL_FISH,
            RandomGenerator.getDefault(),
            1.4,
            1.4
        );

        FightSnapshot result =
            fight.tick(ReelAction.HOLD);

        assertEquals(
            FightPhase.CAUGHT,
            result.phase()
        );
    }
}
