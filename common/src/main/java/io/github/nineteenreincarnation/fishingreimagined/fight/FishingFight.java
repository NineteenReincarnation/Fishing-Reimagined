package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.Objects;
import java.util.random.RandomGenerator;

public final class FishingFight {
    private static final double INITIAL_TENSION = 0.38;

    private static final double REEL_TENSION_GAIN = 0.028;
    private static final double RELEASE_TENSION_LOSS = 0.040;
    private static final double HOLD_TENSION_LOSS = 0.012;

    private static final double PROGRESS_MIN_TENSION = 0.12;
    private static final double PROGRESS_FULL_START = 0.24;
    private static final double PROGRESS_FULL_END = 0.74;
    private static final double PROGRESS_MAX_TENSION = 0.92;
    private static final double BREAK_TENSION = 1.00;

    private static final double BASE_PROGRESS_PER_TICK = 0.0046;

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

    private double fishVelocity;
    private double lineVelocity;
    private double reelEfficiency = 1.0;
    private double dragSlip;

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
        this.distance = initialDistance;
        this.lineLength = initialLineLength;
        this.stamina = fishProfile.maxStamina();
        this.tension = lineProfile.maxTension() * INITIAL_TENSION;

        behavior = fishBehavior.createSession(fishProfile, random);
    }

    public static FishingFight prototype(
        RandomGenerator random,
        double initialDistance
    ) {
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
        updateProgress(action);
        updateStamina(action);
        updateWorldPresentation(action);
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
            initialDistance,
            lineLength,
            tension,
            lineProfile.maxTension(),
            fishVelocity,
            lineVelocity,
            reelEfficiency,
            dragSlip,
            landingProgress,
            breakRisk(),
            slackTicks,
            overTensionTicks,
            lastIntent
        );
    }

    private void updateTension(ReelAction action) {
        double ratio = tensionRatio();

        double fishPressure = switch (lastIntent.mode()) {
            case PROBING -> 0.0025;
            case PULLING -> 0.0105;
            case BURST -> 0.0280;
            case RECOVERING -> -0.0045;
            case TIRED -> 0.0010;
        };

        fishPressure *= 0.75 + fishProfile.pullStrength() * 0.30;
        fishPressure += Math.max(0.0, lastIntent.outwardVelocity()) * 0.08;

        double playerPressure = switch (action) {
            case REEL_IN -> REEL_TENSION_GAIN;
            case PAY_OUT -> -RELEASE_TENSION_LOSS;
            case HOLD -> -HOLD_TENSION_LOSS;
        };

        double next = ratio + fishPressure + playerPressure;

        tension = lineProfile.maxTension()
            * clamp(next, 0.0, 1.25);

        reelEfficiency = action == ReelAction.REEL_IN
            ? clamp(1.0 - Math.max(0.0, ratio - 0.72) * 1.6, 0.35, 1.0)
            : 1.0;

        dragSlip = 0.0;
    }

    private void updateProgress(ReelAction action) {
        if (action != ReelAction.REEL_IN) {
            return;
        }

        double ratio = tensionRatio();
        if (ratio < PROGRESS_MIN_TENSION || ratio > PROGRESS_MAX_TENSION) {
            return;
        }

        double quality;
        if (ratio < PROGRESS_FULL_START) {
            quality = (ratio - PROGRESS_MIN_TENSION)
                / (PROGRESS_FULL_START - PROGRESS_MIN_TENSION);
        } else if (ratio <= PROGRESS_FULL_END) {
            quality = 1.0;
        } else {
            quality = (PROGRESS_MAX_TENSION - ratio)
                / (PROGRESS_MAX_TENSION - PROGRESS_FULL_END);
        }

        quality = clamp(quality, 0.0, 1.0);

        double stateFactor = switch (lastIntent.mode()) {
            case BURST -> 0.45;
            case PULLING -> 0.78;
            case PROBING -> 1.0;
            case RECOVERING -> 1.20;
            case TIRED -> 1.28;
        };

        landingProgress = Math.min(
            1.0,
            landingProgress
                + BASE_PROGRESS_PER_TICK
                * (0.55 + quality * 0.45)
                * stateFactor
        );
    }

    private void updateStamina(ReelAction action) {
        double ratio = tensionRatio();

        if (action == ReelAction.REEL_IN
            && ratio >= PROGRESS_MIN_TENSION
            && ratio <= PROGRESS_MAX_TENSION) {
            double drain = fishProfile.staminaDrainRate()
                * (0.24 + lastIntent.effort() * 0.56);

            stamina = Math.max(0.0, stamina - drain);
            return;
        }

        if (action == ReelAction.PAY_OUT
            && !lastIntent.burst()) {
            stamina = Math.min(
                fishProfile.maxStamina(),
                stamina + fishProfile.recoveryRate() * 0.35
            );
        }
    }

    private void updateWorldPresentation(ReelAction action) {
        double oldDistance = distance;
        double oldLineLength = lineLength;

        double span = Math.max(
            0.0,
            initialDistance - lineProfile.catchDistance()
        );

        double baseDistance = lineProfile.catchDistance()
            + span * (1.0 - landingProgress);

        double runOffset = switch (lastIntent.mode()) {
            case BURST -> 1.15 + lastIntent.effort() * 0.55;
            case PULLING -> 0.35;
            case PROBING -> 0.12;
            case RECOVERING -> -0.08;
            case TIRED -> 0.0;
        };

        double targetDistance = Math.max(
            lineProfile.catchDistance(),
            baseDistance + runOffset
        );

        double response = lastIntent.burst() ? 0.28 : 0.16;
        distance += (targetDistance - distance) * response;

        fishVelocity = distance - oldDistance;

        double slack = Math.max(
            0.0,
            0.68 - tensionRatio()
        ) * 0.90;

        lineLength = Math.min(
            lineProfile.maxLineLength(),
            Math.max(
                lineProfile.catchDistance(),
                distance + slack
            )
        );

        lineVelocity = lineLength - oldLineLength;

        if (action == ReelAction.REEL_IN && lineVelocity > 0.0) {
            lineVelocity *= 0.25;
        }
    }

    private void updateFailurePressure() {
        double ratio = tensionRatio();

        if (ratio < 0.08) {
            slackTicks++;
        } else {
            slackTicks = Math.max(0, slackTicks - 3);
        }

        if (ratio >= BREAK_TENSION) {
            overTensionTicks += lastIntent.burst() ? 2 : 1;
        } else if (ratio < 0.92) {
            overTensionTicks = Math.max(0, overTensionTicks - 3);
        } else {
            overTensionTicks = Math.max(0, overTensionTicks - 1);
        }
    }

    private void updatePhase() {
        if (overTensionTicks >= lineProfile.breakGraceTicks()) {
            phase = FightPhase.LINE_BROKEN;
            return;
        }

        if (landingProgress >= 1.0) {
            landingProgress = 1.0;
            distance = lineProfile.catchDistance();
            lineLength = lineProfile.catchDistance();
            phase = FightPhase.CAUGHT;
            return;
        }

        phase = staminaRatio() <= fishProfile.tiredThreshold()
            ? FightPhase.TIRED
            : FightPhase.FIGHTING;
    }

    private double breakRisk() {
        return clamp(
            overTensionTicks / (double) lineProfile.breakGraceTicks(),
            0.0,
            1.0
        );
    }

    private double staminaRatio() {
        return fishProfile.maxStamina() <= 0.0
            ? 0.0
            : stamina / fishProfile.maxStamina();
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
