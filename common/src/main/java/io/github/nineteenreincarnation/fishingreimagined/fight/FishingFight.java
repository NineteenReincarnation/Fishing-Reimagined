package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.Objects;
import java.util.random.RandomGenerator;

public final class FishingFight {
    private static final double TRACK_MIN = 0.035;
    private static final double TRACK_MAX = 0.965;

    private static final double PLAYER_ACCEL_RIGHT = 0.0054;
    private static final double PLAYER_ACCEL_LEFT = -0.0048;
    private static final double PLAYER_DAMPING = 0.86;
    private static final double PLAYER_MAX_SPEED = 0.045;
    private static final double EDGE_BOUNCE = 0.28;

    private static final int OUTSIDE_GRACE_TICKS = 10;
    private static final double PROGRESS_GAIN = 0.0060;
    private static final double PROGRESS_LOSS = -0.0038;
    private static final double PROGRESS_RESPONSE = 0.18;
    private static final double PROGRESS_DIRECTION_RESPONSE = 0.10;

    private static final double ESCAPE_LIMIT = 70.0;

    private final FishProfile fishProfile;
    private final LineProfile lineProfile;
    private final FishBehaviorSession behavior;
    private final RandomGenerator random;
    private final double initialDistance;
    private final double catchZoneWidth;

    private long tick;
    private FightPhase phase = FightPhase.FIGHTING;

    private double stamina;
    private double distance;
    private double lineLength;

    private double fishTrackPosition = 0.50;
    private double fishVelocity;
    private double fishTargetPosition = 0.50;
    private int fishDecisionTicks;
    private FishFightMode previousMode;

    private double catchZonePosition = 0.50;
    private double catchZoneVelocity;

    private double landingProgress = 0.08;
    private double progressVelocity;
    private int outsideTicks;
    private double escapePressure;

    private double tension;
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
        this.random = Objects.requireNonNull(random, "random");

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

        double speedDifficulty = clamp(
            fishProfile.cruiseSpeed() / 0.022,
            0.65,
            1.45
        );

        double combinedDifficulty =
            (speedDifficulty + fishProfile.pullStrength()) * 0.5;

        catchZoneWidth = clamp(
            0.30 - (combinedDifficulty - 1.0) * 0.055,
            0.235,
            0.335
        );

        behavior = fishBehavior.createSession(
            fishProfile,
            random
        );

        updatePresentationState();
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

        updateCatchZone(action);
        updateFishTrack();
        updateProgress();
        updateStamina();
        updateEscapePressure();
        updatePresentationState();
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
            catchZoneVelocity,
            reelEfficiency,
            dragSlip,
            landingProgress,
            escapeRisk(),
            slackTicks,
            overTensionTicks,
            fishTrackPosition,
            catchZonePosition,
            catchZoneWidth,
            overlapRatio(),
            lastIntent
        );
    }

    private void updateCatchZone(ReelAction action) {
        double acceleration = switch (action) {
            case REEL_IN -> PLAYER_ACCEL_RIGHT;
            case PAY_OUT -> PLAYER_ACCEL_LEFT;
            case HOLD -> 0.0;
        };

        catchZoneVelocity += acceleration;
        catchZoneVelocity *= PLAYER_DAMPING;
        catchZoneVelocity = clamp(
            catchZoneVelocity,
            -PLAYER_MAX_SPEED,
            PLAYER_MAX_SPEED
        );

        catchZonePosition += catchZoneVelocity;

        double halfWidth = catchZoneWidth * 0.5;
        double minCenter = halfWidth;
        double maxCenter = 1.0 - halfWidth;

        if (catchZonePosition < minCenter) {
            catchZonePosition = minCenter;
            if (catchZoneVelocity < 0.0) {
                catchZoneVelocity =
                    -catchZoneVelocity * EDGE_BOUNCE;
            }
        } else if (catchZonePosition > maxCenter) {
            catchZonePosition = maxCenter;
            if (catchZoneVelocity > 0.0) {
                catchZoneVelocity =
                    -catchZoneVelocity * EDGE_BOUNCE;
            }
        }
    }

    private void updateFishTrack() {
        FishFightMode mode = lastIntent.mode();

        if (mode != previousMode || fishDecisionTicks <= 0) {
            chooseFishTarget(mode);
            previousMode = mode;
        }

        fishDecisionTicks--;

        double speciesSpeed = clamp(
            fishProfile.cruiseSpeed() / 0.022,
            0.65,
            1.45
        );

        double maxSpeed = switch (mode) {
            case PROBING -> 0.014;
            case PULLING -> 0.022;
            case BURST -> 0.046;
            case RECOVERING -> 0.011;
            case TIRED -> 0.009;
        } * speciesSpeed;

        double response = switch (mode) {
            case PROBING -> 0.070;
            case PULLING -> 0.095;
            case BURST -> 0.155;
            case RECOVERING -> 0.060;
            case TIRED -> 0.045;
        };

        double desiredVelocity =
            (fishTargetPosition - fishTrackPosition) * response;

        desiredVelocity += clamp(
            lastIntent.lateralTurnRadians(),
            -0.22,
            0.22
        ) * 0.012;

        desiredVelocity = clamp(
            desiredVelocity,
            -maxSpeed,
            maxSpeed
        );

        double velocityResponse =
            mode == FishFightMode.BURST ? 0.48 : 0.30;

        fishVelocity +=
            (desiredVelocity - fishVelocity) * velocityResponse;

        fishVelocity *=
            mode == FishFightMode.TIRED ? 0.90 : 0.94;

        fishVelocity = clamp(
            fishVelocity,
            -maxSpeed,
            maxSpeed
        );

        fishTrackPosition += fishVelocity;

        if (fishTrackPosition < TRACK_MIN) {
            fishTrackPosition = TRACK_MIN;
            fishVelocity = Math.abs(fishVelocity) * 0.45;
            fishTargetPosition = clamp(
                0.24 + random.nextDouble() * 0.34,
                TRACK_MIN,
                TRACK_MAX
            );
        } else if (fishTrackPosition > TRACK_MAX) {
            fishTrackPosition = TRACK_MAX;
            fishVelocity = -Math.abs(fishVelocity) * 0.45;
            fishTargetPosition = clamp(
                0.42 - random.nextDouble() * 0.24,
                TRACK_MIN,
                TRACK_MAX
            );
        }
    }

    private void chooseFishTarget(FishFightMode mode) {
        switch (mode) {
            case PROBING -> {
                fishTargetPosition = clamp(
                    fishTrackPosition + random.nextDouble(-0.22, 0.22),
                    0.10,
                    0.90
                );
                fishDecisionTicks = random.nextInt(18, 34);
            }

            case PULLING -> {
                fishTargetPosition = random.nextBoolean()
                    ? random.nextDouble(0.66, 0.90)
                    : random.nextDouble(0.10, 0.34);
                fishDecisionTicks = random.nextInt(14, 28);
            }

            case BURST -> {
                double separation =
                    fishTrackPosition - catchZonePosition;

                boolean runRight =
                    Math.abs(separation) > 0.08
                        ? separation > 0.0
                        : random.nextBoolean();

                fishTargetPosition = runRight
                    ? random.nextDouble(0.88, 0.965)
                    : random.nextDouble(0.035, 0.12);

                fishDecisionTicks = random.nextInt(8, 16);
            }

            case RECOVERING -> {
                fishTargetPosition = clamp(
                    0.50 + random.nextDouble(-0.18, 0.18),
                    0.16,
                    0.84
                );
                fishDecisionTicks = random.nextInt(20, 34);
            }

            case TIRED -> {
                fishTargetPosition = clamp(
                    fishTrackPosition + random.nextDouble(-0.10, 0.10),
                    0.16,
                    0.84
                );
                fishDecisionTicks = random.nextInt(28, 48);
            }
        }
    }

    private void updateProgress() {
        double overlap = overlapRatio();
        double targetVelocity;

        if (overlap > 0.0) {
            outsideTicks = 0;
            targetVelocity =
                PROGRESS_GAIN * (0.72 + overlap * 0.28);
        } else {
            outsideTicks++;

            if (outsideTicks <= OUTSIDE_GRACE_TICKS) {
                targetVelocity = 0.0;
            } else {
                double miss = Math.max(
                    0.0,
                    Math.abs(
                        fishTrackPosition - catchZonePosition
                    ) - catchZoneWidth * 0.5
                );

                targetVelocity =
                    PROGRESS_LOSS
                        * (
                            0.82
                                + Math.min(
                                    0.55,
                                    miss * 1.6
                                )
                        );
            }
        }

        boolean sameDirection =
            Math.signum(targetVelocity)
                == Math.signum(progressVelocity)
                || Math.abs(progressVelocity) < 1.0E-7;

        double response = sameDirection
            ? PROGRESS_RESPONSE
            : PROGRESS_DIRECTION_RESPONSE;

        progressVelocity +=
            (targetVelocity - progressVelocity) * response;

        progressVelocity = clamp(
            progressVelocity,
            PROGRESS_LOSS * 1.35,
            PROGRESS_GAIN
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

    private void updateStamina() {
        if (overlapRatio() > 0.0) {
            double drain =
                fishProfile.staminaDrainRate()
                    * (
                        0.20
                            + lastIntent.effort() * 0.30
                    );

            stamina = Math.max(
                0.0,
                stamina - drain
            );
            return;
        }

        if (!lastIntent.burst()) {
            stamina = Math.min(
                fishProfile.maxStamina(),
                stamina
                    + fishProfile.recoveryRate() * 0.18
            );
        }
    }

    private void updateEscapePressure() {
        if (
            landingProgress <= 0.001
                && outsideTicks > OUTSIDE_GRACE_TICKS + 16
        ) {
            escapePressure +=
                1.0
                    + Math.min(
                        0.7,
                        Math.abs(fishVelocity) * 12.0
                    );
        } else {
            escapePressure = Math.max(
                0.0,
                escapePressure - 3.0
            );
        }

        slackTicks = outsideTicks;
        overTensionTicks =
            (int) Math.round(escapePressure);
    }

    private void updatePresentationState() {
        double span = Math.max(
            0.0,
            initialDistance - lineProfile.catchDistance()
        );

        double runOffset =
            lastIntent.burst()
                ? 0.65
                : lastIntent.mode() == FishFightMode.PULLING
                    ? 0.20
                    : 0.0;

        double targetDistance =
            lineProfile.catchDistance()
                + span * (1.0 - landingProgress)
                + runOffset;

        distance +=
            (targetDistance - distance)
                * (lastIntent.burst() ? 0.24 : 0.15);

        double slack =
            overlapRatio() > 0.0 ? 0.16 : 0.46;

        lineLength = Math.min(
            lineProfile.maxLineLength(),
            Math.max(
                lineProfile.catchDistance(),
                distance + slack
            )
        );

        double cosmeticLoad =
            0.22
                + Math.min(
                    0.36,
                    Math.abs(fishVelocity) * 8.0
                )
                + lastIntent.effort() * 0.18
                + (overlapRatio() > 0.0 ? 0.02 : 0.10);

        tension =
            lineProfile.maxTension()
                * clamp(cosmeticLoad, 0.18, 0.84);

        reelEfficiency =
            overlapRatio() > 0.0 ? 1.0 : 0.65;

        dragSlip = 0.0;
    }

    private void updatePhase() {
        if (escapePressure >= ESCAPE_LIMIT) {
            phase = FightPhase.ESCAPED;
            return;
        }

        if (landingProgress >= 1.0) {
            landingProgress = 1.0;
            distance = lineProfile.catchDistance();
            lineLength = distance;
            phase = FightPhase.CAUGHT;
            return;
        }

        phase =
            staminaRatio() <= fishProfile.tiredThreshold()
                ? FightPhase.TIRED
                : FightPhase.FIGHTING;
    }

    private double overlapRatio() {
        double half = catchZoneWidth * 0.5;
        double markerRadius = 0.018;
        double distanceFromCenter =
            Math.abs(fishTrackPosition - catchZonePosition);

        return clamp(
            (half + markerRadius - distanceFromCenter)
                / (half + markerRadius),
            0.0,
            1.0
        );
    }

    private double escapeRisk() {
        return clamp(
            escapePressure / ESCAPE_LIMIT,
            0.0,
            1.0
        );
    }

    private double staminaRatio() {
        return fishProfile.maxStamina() <= 0.0
            ? 0.0
            : stamina / fishProfile.maxStamina();
    }

    private static double clamp(
        double value,
        double min,
        double max
    ) {
        return Math.max(min, Math.min(max, value));
    }
}
