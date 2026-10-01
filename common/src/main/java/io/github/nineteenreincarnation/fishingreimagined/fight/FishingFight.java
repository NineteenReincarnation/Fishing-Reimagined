package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.Objects;
import java.util.random.RandomGenerator;

public final class FishingFight {
    private final FishProfile fishProfile;
    private final LineProfile lineProfile;
    private final FishBehaviorSession behavior;

    private long tick;
    private FightPhase phase = FightPhase.FIGHTING;
    private double stamina;
    private double distance;
    private double lineLength;
    private double tension;
    private int slackTicks;
    private int overTensionTicks;
    private FishIntent lastIntent = FishIntent.CALM;

    public FishingFight(
        FishProfile fishProfile,
        LineProfile lineProfile,
        FishBehavior fishBehavior,
        RandomGenerator random,
        double initialDistance,
        double initialLineLength
    ) {
        this.fishProfile = Objects.requireNonNull(fishProfile, "fishProfile");
        this.lineProfile = Objects.requireNonNull(lineProfile, "lineProfile");
        Objects.requireNonNull(fishBehavior, "fishBehavior");
        Objects.requireNonNull(random, "random");

        if (!Double.isFinite(initialDistance) || initialDistance < 0.0) {
            throw new IllegalArgumentException("initialDistance must be finite and >= 0");
        }
        if (!Double.isFinite(initialLineLength)
            || initialLineLength < 0.0
            || initialLineLength > lineProfile.maxLineLength()) {
            throw new IllegalArgumentException("initialLineLength is outside the supported line range");
        }

        stamina = fishProfile.maxStamina();
        distance = initialDistance;
        lineLength = initialLineLength;
        behavior = fishBehavior.createSession(fishProfile, random);
        recalculateTension();
    }

    public static FishingFight prototype(RandomGenerator random, double initialDistance) {
        return new FishingFight(
            FishProfile.PROTOTYPE,
            LineProfile.PROTOTYPE,
            BasicFishBehavior.INSTANCE,
            random,
            initialDistance,
            initialDistance
        );
    }

    public FightSnapshot tick(ReelAction action) {
        Objects.requireNonNull(action, "action");
        if (phase.isTerminal()) {
            return snapshot();
        }

        tick++;
        lastIntent = Objects.requireNonNull(behavior.nextIntent(snapshot()), "fish intent");

        applyReelAction(action);
        applyFishMovement();
        recalculateTension();
        applyLineConstraint();
        recalculateTension();
        updateStamina();
        updateFailurePressure();
        updatePhase();

        return snapshot();
    }

    public FightSnapshot snapshot() {
        return new FightSnapshot(
            tick,
            phase,
            stamina,
            fishProfile.maxStamina(),
            distance,
            lineLength,
            tension,
            lineProfile.maxTension(),
            slackTicks,
            overTensionTicks,
            lastIntent
        );
    }

    private void applyReelAction(ReelAction action) {
        switch (action) {
            case REEL_IN -> lineLength = Math.max(0.0, lineLength - lineProfile.reelRate());
            case PAY_OUT -> lineLength = Math.min(lineProfile.maxLineLength(), lineLength + lineProfile.payoutRate());
            case HOLD -> {
            }
        }
    }

    private void applyFishMovement() {
        double staminaRatio = stamina / fishProfile.maxStamina();
        double strengthFactor = 0.30 + 0.70 * Math.sqrt(Math.max(0.0, staminaRatio));
        double outwardMovement =
            lastIntent.outwardVelocity() * fishProfile.pullStrength() * strengthFactor;
        distance = Math.max(0.0, distance + outwardMovement);
    }

    private void applyLineConstraint() {
        if (tension <= 0.0) {
            return;
        }

        double normalizedTension = Math.min(1.0, tension / lineProfile.maxTension());
        double staminaRatio = stamina / fishProfile.maxStamina();
        double fatigueAdvantage = 0.35 + (1.0 - staminaRatio) * 0.85;
        double inwardMovement =
            normalizedTension * lineProfile.pullbackRate() * fatigueAdvantage;
        distance = Math.max(0.0, distance - inwardMovement);
    }

    private void updateStamina() {
        if (tension >= lineProfile.slackThreshold()) {
            double controlledLoad = Math.min(1.0, tension / lineProfile.maxTension());
            double drain =
                fishProfile.staminaDrainRate()
                    * controlledLoad
                    * Math.max(0.2, lastIntent.effort());
            stamina = Math.max(0.0, stamina - drain);
            return;
        }

        if (!lastIntent.burst()) {
            stamina = Math.min(fishProfile.maxStamina(), stamina + fishProfile.recoveryRate());
        }
    }

    private void updateFailurePressure() {
        if (tension < lineProfile.slackThreshold() && distance > lineProfile.catchDistance()) {
            slackTicks++;
        } else {
            slackTicks = Math.max(0, slackTicks - 2);
        }

        if (tension > lineProfile.maxTension()) {
            overTensionTicks++;
        } else {
            overTensionTicks = Math.max(0, overTensionTicks - 2);
        }
    }

    private void updatePhase() {
        if (overTensionTicks >= lineProfile.breakGraceTicks()) {
            phase = FightPhase.LINE_BROKEN;
            return;
        }
        if (slackTicks >= fishProfile.slackEscapeTicks()) {
            phase = FightPhase.ESCAPED;
            return;
        }
        if (distance <= lineProfile.catchDistance()
            && lineLength <= lineProfile.catchDistance() + lineProfile.reelRate() * 2.0
            && tension <= lineProfile.maxTension()) {
            phase = FightPhase.CAUGHT;
            return;
        }

        double staminaRatio = stamina / fishProfile.maxStamina();
        phase = staminaRatio <= fishProfile.tiredThreshold()
            ? FightPhase.TIRED
            : FightPhase.FIGHTING;
    }

    private void recalculateTension() {
        double extension = Math.max(0.0, distance - lineLength);
        tension = extension * lineProfile.stiffness();
    }
}
