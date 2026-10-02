package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.Objects;
import java.util.random.RandomGenerator;

public final class FishingFight {
    private static final double INITIAL_TENSION_RATIO = 0.42;
    private static final double SWEET_MIN = 0.34;
    private static final double SWEET_MAX = 0.70;
    private static final double SWEET_IDEAL = 0.52;
    private static final double HIGH_TENSION = 0.82;
    private static final double BREAK_TENSION = 0.95;
    private static final double SLACK_TENSION = 0.08;

    private static final double REEL_TENSION_PER_TICK = 2.55;
    private static final double RELEASE_TENSION_PER_TICK = -5.10;
    private static final double HOLD_TENSION_PER_TICK = -1.20;

    private static final double BASE_PROGRESS_PER_TICK = 0.0052;
    private static final double SOFT_PROGRESS_LOSS = 0.00030;
    private static final double SLACK_PROGRESS_LOSS = 0.0024;
    private static final double HIGH_PROGRESS_LOSS = 0.0010;

    private final FishProfile fishProfile;
    private final LineProfile lineProfile;
    private final FishBehaviorSession behavior;
    private final double initialDistance;

    private long tick;
    private FightPhase phase = FightPhase.FIGHTING;
    private double stamina;
    private double distance;
    private double lineLength;
    private double tension;
    private double landingProgress;
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

        this.initialDistance = initialDistance;
        stamina = fishProfile.maxStamina();
        distance = initialDistance;
        lineLength = initialLineLength;
        tension = lineProfile.maxTension() * INITIAL_TENSION_RATIO;
        landingProgress = initialDistance <= lineProfile.catchDistance() ? 1.0 : 0.0;
        behavior = fishBehavior.createSession(fishProfile, random);
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
        lastIntent = Objects.requireNonNull(
            behavior.nextIntent(snapshot()),
            "fish intent"
        );

        updateTension(action);
        updateFailurePressure();
        updateStamina();
        updateLandingProgress();
        updateVisualDistance();
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
            landingProgress,
            breakRisk(),
            slackTicks,
            overTensionTicks,
            lastIntent
        );
    }

    private void updateTension(ReelAction action) {
        double staminaRatio = fishProfile.maxStamina() <= 0.0
            ? 0.0
            : stamina / fishProfile.maxStamina();

        double strength =
            0.45 + 0.55 * Math.sqrt(Math.max(0.0, staminaRatio));

        double fishPull =
            fishProfile.pullStrength()
                * strength
                * (
                    0.55
                        + lastIntent.effort() * 0.90
                        + Math.max(0.0, lastIntent.outwardVelocity()) * 18.0
                );

        double playerDelta = switch (action) {
            case REEL_IN -> REEL_TENSION_PER_TICK;
            case PAY_OUT -> RELEASE_TENSION_PER_TICK;
            case HOLD -> HOLD_TENSION_PER_TICK;
        };

        double damping = tension > lineProfile.maxTension() ? -0.55 : 0.0;

        tension = clamp(
            tension + fishPull + playerDelta + damping,
            0.0,
            lineProfile.maxTension() * 1.30
        );
    }

    private void updateLandingProgress() {
        if (landingProgress >= 1.0) {
            return;
        }

        double ratio = tensionRatio();

        if (ratio >= SWEET_MIN && ratio <= SWEET_MAX) {
            double halfWidth = (SWEET_MAX - SWEET_MIN) * 0.5;
            double quality =
                1.0
                    - Math.min(
                        1.0,
                        Math.abs(ratio - SWEET_IDEAL) / halfWidth
                    );

            double staminaRatio = stamina / fishProfile.maxStamina();
            double fatigueBonus = 1.0 + (1.0 - staminaRatio) * 0.35;
            double burstFactor = lastIntent.burst() ? 0.38 : 1.0;

            landingProgress = Math.min(
                1.0,
                landingProgress
                    + BASE_PROGRESS_PER_TICK
                        * (0.58 + quality * 0.42)
                        * fatigueBonus
                        * burstFactor
            );
            return;
        }

        if (ratio < SLACK_TENSION) {
            landingProgress = Math.max(
                0.0,
                landingProgress - SLACK_PROGRESS_LOSS
            );
            return;
        }

        if (ratio > HIGH_TENSION) {
            landingProgress = Math.max(
                0.0,
                landingProgress - HIGH_PROGRESS_LOSS
            );
            return;
        }

        landingProgress = Math.max(
            0.0,
            landingProgress - SOFT_PROGRESS_LOSS
        );
    }

    private void updateStamina() {
        double ratio = tensionRatio();

        if (ratio >= SWEET_MIN && ratio <= SWEET_MAX) {
            double drain =
                fishProfile.staminaDrainRate()
                    * (0.38 + lastIntent.effort() * 0.62);

            stamina = Math.max(0.0, stamina - drain);
            return;
        }

        if (ratio < SWEET_MIN && !lastIntent.burst()) {
            stamina = Math.min(
                fishProfile.maxStamina(),
                stamina + fishProfile.recoveryRate()
            );
        }
    }

    private void updateVisualDistance() {
        double span = Math.max(
            0.0,
            initialDistance - lineProfile.catchDistance()
        );

        double target =
            lineProfile.catchDistance()
                + span * (1.0 - landingProgress);

        if (lastIntent.burst()) {
            target += Math.min(
                1.4,
                lastIntent.outwardVelocity() * 12.0
            );
        }

        distance +=
            (target - distance)
                * (lastIntent.burst() ? 0.20 : 0.12);

        distance = Math.max(
            lineProfile.catchDistance(),
            distance
        );

        double slack =
            Math.max(0.0, SWEET_IDEAL - tensionRatio()) * 2.2;

        lineLength = Math.min(
            lineProfile.maxLineLength(),
            distance + slack
        );
    }

    private void updateFailurePressure() {
        double ratio = tensionRatio();

        if (ratio < SLACK_TENSION && landingProgress < 1.0) {
            slackTicks++;
        } else {
            slackTicks = Math.max(0, slackTicks - 3);
        }

        if (ratio >= BREAK_TENSION) {
            int pressure =
                ratio >= 1.15
                    ? 3
                    : ratio >= 1.05
                        ? 2
                        : 1;
            overTensionTicks += pressure;
        } else if (ratio < HIGH_TENSION) {
            overTensionTicks = Math.max(0, overTensionTicks - 4);
        } else {
            overTensionTicks = Math.max(0, overTensionTicks - 1);
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

        if (landingProgress >= 1.0) {
            distance = lineProfile.catchDistance();
            lineLength = lineProfile.catchDistance();
            phase = FightPhase.CAUGHT;
            return;
        }

        double staminaRatio = stamina / fishProfile.maxStamina();
        phase = staminaRatio <= fishProfile.tiredThreshold()
            ? FightPhase.TIRED
            : FightPhase.FIGHTING;
    }

    private double breakRisk() {
        return Math.min(
            1.0,
            overTensionTicks / (double) lineProfile.breakGraceTicks()
        );
    }

    private double tensionRatio() {
        return lineProfile.maxTension() <= 0.0
            ? 0.0
            : tension / lineProfile.maxTension();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
