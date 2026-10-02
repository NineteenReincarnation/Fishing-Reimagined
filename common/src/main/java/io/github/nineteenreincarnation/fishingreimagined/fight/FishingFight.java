package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.Objects;
import java.util.random.RandomGenerator;

public final class FishingFight {
    private static final double INITIAL_TENSION = 0.34;

    private static final double REEL_TENSION_GAIN = 0.0215;
    private static final double RELEASE_TENSION_LOSS = 0.0290;
    private static final double HOLD_TENSION_LOSS = 0.0140;
    private static final double TENSION_RESPONSE = 0.38;

    private static final double VERY_LOW_TENSION = 0.12;
    private static final double LOW_TENSION = 0.28;
    private static final double IDEAL_TENSION_END = 0.68;
    private static final double HIGH_TENSION_END = 0.90;
    private static final double BREAK_TENSION = 0.90;

    private static final double PROGRESS_GAIN_IDEAL = 0.0048;
    private static final double PROGRESS_GAIN_IDEAL_RELEASE = 0.0020;
    private static final double PROGRESS_GAIN_HIGH = 0.0021;
    private static final double PROGRESS_GAIN_HIGH_RELEASE = 0.0007;
    private static final double PROGRESS_LOSS_LOW = -0.0016;
    private static final double PROGRESS_LOSS_VERY_LOW = -0.0038;
    private static final double PROGRESS_LOSS_RED = -0.0013;

    private static final double PROGRESS_RESPONSE_SAME_DIRECTION = 0.24;
    private static final double PROGRESS_RESPONSE_DIRECTION_CHANGE = 0.13;

    private static final double SNAP_LIMIT = 32.0;

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
    private double tensionVelocity;

    private double landingProgress;
    private double progressVelocity;

    private double fishVelocity;
    private double lineVelocity;
    private double reelEfficiency = 1.0;
    private double dragSlip;

    private double snapPressure;
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
            throw new IllegalArgumentException(
                "initialDistance must be finite and >= 0"
            );
        }

        if (!Double.isFinite(initialLineLength)
            || initialLineLength < 0.0
            || initialLineLength > lineProfile.maxLineLength()) {
            throw new IllegalArgumentException(
                "initialLineLength is outside the supported line range"
            );
        }

        this.initialDistance = initialDistance;
        distance = initialDistance;
        lineLength = initialLineLength;
        stamina = fishProfile.maxStamina();
        tension = lineProfile.maxTension() * INITIAL_TENSION;

        behavior = fishBehavior.createSession(
            fishProfile,
            random
        );
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
        double fishPressure = switch (lastIntent.mode()) {
            case PROBING -> 0.0015;
            case PULLING -> 0.0065;
            case BURST -> 0.0175;
            case RECOVERING -> -0.0030;
            case TIRED -> 0.0005;
        };

        fishPressure *= 0.82 + fishProfile.pullStrength() * 0.22;

        double playerPressure = switch (action) {
            case REEL_IN -> REEL_TENSION_GAIN;
            case PAY_OUT -> -RELEASE_TENSION_LOSS;
            case HOLD -> -HOLD_TENSION_LOSS;
        };

        double targetVelocity =
            fishPressure + playerPressure;

        tensionVelocity +=
            (targetVelocity - tensionVelocity)
                * TENSION_RESPONSE;

        double ratio = tensionRatio() + tensionVelocity;

        tension = lineProfile.maxTension()
            * clamp(ratio, 0.0, 1.22);

        reelEfficiency = action == ReelAction.REEL_IN
            ? clamp(
                1.0 - Math.max(0.0, tensionRatio() - 0.76) * 1.15,
                0.55,
                1.0
            )
            : 1.0;

        dragSlip = 0.0;
    }

    private void updateProgress(ReelAction action) {
        double ratio = tensionRatio();
        double targetVelocity;

        if (ratio < VERY_LOW_TENSION) {
            targetVelocity = PROGRESS_LOSS_VERY_LOW;
        } else if (ratio < LOW_TENSION) {
            targetVelocity = PROGRESS_LOSS_LOW;
        } else if (ratio <= IDEAL_TENSION_END) {
            targetVelocity = action == ReelAction.REEL_IN
                ? PROGRESS_GAIN_IDEAL
                : PROGRESS_GAIN_IDEAL_RELEASE;
        } else if (ratio < HIGH_TENSION_END) {
            targetVelocity = action == ReelAction.REEL_IN
                ? PROGRESS_GAIN_HIGH
                : PROGRESS_GAIN_HIGH_RELEASE;
        } else {
            targetVelocity = PROGRESS_LOSS_RED;
        }

        boolean sameDirection =
            Math.signum(targetVelocity)
                == Math.signum(progressVelocity)
                || Math.abs(progressVelocity) < 1.0E-7;

        double response = sameDirection
            ? PROGRESS_RESPONSE_SAME_DIRECTION
            : PROGRESS_RESPONSE_DIRECTION_CHANGE;

        progressVelocity +=
            (targetVelocity - progressVelocity)
                * response;

        progressVelocity = clamp(
            progressVelocity,
            PROGRESS_LOSS_VERY_LOW,
            PROGRESS_GAIN_IDEAL
        );

        landingProgress = clamp(
            landingProgress + progressVelocity,
            0.0,
            1.0
        );

        if (landingProgress <= 0.0 && progressVelocity < 0.0) {
            progressVelocity = 0.0;
        }

        if (landingProgress >= 1.0 && progressVelocity > 0.0) {
            progressVelocity = 0.0;
        }
    }

    private void updateStamina(ReelAction action) {
        double ratio = tensionRatio();

        if (action == ReelAction.REEL_IN
            && ratio >= LOW_TENSION
            && ratio < HIGH_TENSION_END) {
            double drain =
                fishProfile.staminaDrainRate()
                    * (0.18 + lastIntent.effort() * 0.38);

            stamina = Math.max(
                0.0,
                stamina - drain
            );
            return;
        }

        if (action == ReelAction.PAY_OUT
            && !lastIntent.burst()) {
            stamina = Math.min(
                fishProfile.maxStamina(),
                stamina + fishProfile.recoveryRate() * 0.22
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

        double baseDistance =
            lineProfile.catchDistance()
                + span * (1.0 - landingProgress);

        double runOffset = switch (lastIntent.mode()) {
            case BURST -> 1.00 + lastIntent.effort() * 0.55;
            case PULLING -> 0.30;
            case PROBING -> 0.10;
            case RECOVERING -> -0.06;
            case TIRED -> 0.0;
        };

        double targetDistance = Math.max(
            lineProfile.catchDistance(),
            baseDistance + runOffset
        );

        double response =
            lastIntent.burst() ? 0.28 : 0.16;

        distance +=
            (targetDistance - distance)
                * response;

        fishVelocity =
            distance - oldDistance;

        double slack = Math.max(
            0.0,
            0.64 - tensionRatio()
        ) * 0.90;

        lineLength = Math.min(
            lineProfile.maxLineLength(),
            Math.max(
                lineProfile.catchDistance(),
                distance + slack
            )
        );

        lineVelocity =
            lineLength - oldLineLength;

        if (action == ReelAction.REEL_IN
            && lineVelocity > 0.0) {
            lineVelocity *= 0.25;
        }
    }

    private void updateFailurePressure() {
        double ratio = tensionRatio();

        if (ratio < VERY_LOW_TENSION) {
            slackTicks++;
        } else {
            slackTicks =
                Math.max(
                    0,
                    slackTicks - 3
                );
        }

        if (ratio >= BREAK_TENSION) {
            double overload = ratio - BREAK_TENSION;

            snapPressure +=
                0.70
                    + overload * 3.5;
        } else if (ratio < 0.82) {
            snapPressure = Math.max(
                0.0,
                snapPressure - 2.4
            );
        } else {
            snapPressure = Math.max(
                0.0,
                snapPressure - 0.8
            );
        }

        overTensionTicks =
            (int) Math.round(snapPressure);
    }

    private void updatePhase() {
        if (snapPressure >= SNAP_LIMIT) {
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

        phase =
            staminaRatio()
                    <= fishProfile.tiredThreshold()
                ? FightPhase.TIRED
                : FightPhase.FIGHTING;
    }

    private double breakRisk() {
        return clamp(
            snapPressure / SNAP_LIMIT,
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

    private static double clamp(
        double value,
        double min,
        double max
    ) {
        return Math.max(
            min,
            Math.min(max, value)
        );
    }
}
