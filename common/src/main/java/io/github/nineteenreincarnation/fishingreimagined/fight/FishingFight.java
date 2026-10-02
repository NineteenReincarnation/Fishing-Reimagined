package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.Objects;
import java.util.random.RandomGenerator;

public final class FishingFight {
    private static final int SUBSTEPS = 4;
    private static final double DT =
        1.0 / SUBSTEPS;

    private static final double INITIAL_LOAD = 0.38;

    private static final double DRAG_START_RATIO = 0.78;
    private static final double DRAG_FULL_RATIO = 1.08;
    private static final double MAX_DRAG_PAYOUT = 0.16;

    private static final double HIGH_TENSION_RATIO = 0.82;
    private static final double BREAK_TENSION_RATIO = 0.96;

    private static final double WATER_DRAG = 0.18;
    private static final double REEL_RESPONSE = 0.46;
    private static final double LINE_DAMPING_SCALE = 3.8;
    private static final double MAX_OUTWARD_SPEED = 0.19;
    private static final double MAX_INWARD_SPEED = -0.10;

    private final FishProfile fishProfile;
    private final LineProfile lineProfile;
    private final FishBehaviorSession behavior;

    private final double initialDistance;
    private final double initialLineLength;
    private final double fishMass;

    private long tick;
    private FightPhase phase =
        FightPhase.FIGHTING;

    private double stamina;
    private double distance;
    private double lineLength;

    private double fishVelocity;
    private double lineVelocity;

    private double tension;
    private double reelEfficiency = 1.0;
    private double dragSlip;

    private double landingProgress;
    private double breakHeat;

    private int slackTicks;
    private int overTensionTicks;

    private FishIntent lastIntent =
        FishIntent.CALM;

    public FishingFight(
        FishProfile fishProfile,
        LineProfile lineProfile,
        FishBehavior fishBehavior,
        RandomGenerator random,
        double initialDistance,
        double initialLineLength
    ) {
        this.fishProfile =
            Objects.requireNonNull(
                fishProfile,
                "fishProfile"
            );

        this.lineProfile =
            Objects.requireNonNull(
                lineProfile,
                "lineProfile"
            );

        Objects.requireNonNull(
            fishBehavior,
            "fishBehavior"
        );

        Objects.requireNonNull(
            random,
            "random"
        );

        if (
            !Double.isFinite(initialDistance)
                || initialDistance < 0.0
        ) {
            throw new IllegalArgumentException(
                "initialDistance must be finite and >= 0"
            );
        }

        if (
            !Double.isFinite(initialLineLength)
                || initialLineLength < 0.0
                || initialLineLength
                    > lineProfile.maxLineLength()
        ) {
            throw new IllegalArgumentException(
                "initialLineLength is outside the supported line range"
            );
        }

        this.initialDistance =
            initialDistance;

        double preloadExtension =
            lineProfile.maxTension()
                * INITIAL_LOAD
                / lineProfile.stiffness();

        this.initialLineLength =
            Math.max(
                lineProfile.catchDistance(),
                initialLineLength
                    - preloadExtension
            );

        this.fishMass =
            0.85
                + fishProfile.pullStrength()
                    * 0.55;

        stamina =
            fishProfile.maxStamina();

        distance =
            initialDistance;

        lineLength =
            this.initialLineLength;

        behavior =
            fishBehavior.createSession(
                fishProfile,
                random
            );

        recalculateTension();
        updateLandingProgress();
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

    public FightSnapshot tick(
        ReelAction action
    ) {
        Objects.requireNonNull(
            action,
            "action"
        );

        if (phase.isTerminal()) {
            return snapshot();
        }

        tick++;

        lastIntent =
            Objects.requireNonNull(
                behavior.nextIntent(
                    snapshot()
                ),
                "fish intent"
            );

        double averageLoad = 0.0;

        for (
            int substep = 0;
            substep < SUBSTEPS;
            substep++
        ) {
            updateSpool(action);
            updateFishMotion();

            averageLoad +=
                tensionRatio();
        }

        averageLoad /= SUBSTEPS;

        updateStamina(
            averageLoad
        );

        updateLandingProgress();
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

    private void updateSpool(
        ReelAction action
    ) {
        double load =
            tensionRatio();

        double targetVelocity =
            switch (action) {
                case REEL_IN ->
                    -lineProfile.reelRate()
                        * reelEfficiencyFor(load);

                case PAY_OUT ->
                    lineProfile.payoutRate();

                case HOLD -> 0.0;
            };

        dragSlip =
            automaticDragSlip(load);

        targetVelocity +=
            dragSlip;

        lineVelocity +=
            (targetVelocity
                - lineVelocity)
                * (
                    1.0
                        - Math.pow(
                            1.0 - REEL_RESPONSE,
                            DT
                        )
                );

        lineLength +=
            lineVelocity * DT;

        lineLength =
            clamp(
                lineLength,
                lineProfile.catchDistance()
                    * 0.75,
                lineProfile.maxLineLength()
            );

        reelEfficiency =
            reelEfficiencyFor(load);
    }

    private void updateFishMotion() {
        recalculateTension();

        double desiredVelocity =
            lastIntent.outwardVelocity();

        double response =
            0.34
                + lastIntent.effort()
                    * 0.32;

        double driveAcceleration =
            (
                desiredVelocity
                    - fishVelocity
            ) * response;

        double tensionAcceleration =
            tensionRatio()
                * lineProfile.pullbackRate()
                / fishMass;

        double acceleration =
            driveAcceleration
                - tensionAcceleration
                - fishVelocity
                    * WATER_DRAG;

        fishVelocity +=
            acceleration * DT;

        fishVelocity =
            clamp(
                fishVelocity,
                MAX_INWARD_SPEED,
                MAX_OUTWARD_SPEED
            );

        distance +=
            fishVelocity * DT;

        distance =
            Math.max(
                lineProfile.catchDistance()
                    * 0.72,
                distance
            );

        recalculateTension();
    }

    private void recalculateTension() {
        double extension =
            Math.max(
                0.0,
                distance - lineLength
            );

        double relativeOutwardSpeed =
            Math.max(
                0.0,
                fishVelocity - lineVelocity
            );

        double springForce =
            extension
                * lineProfile.stiffness();

        double damping =
            Math.sqrt(
                lineProfile.stiffness()
            )
                * LINE_DAMPING_SCALE;

        double dampingForce =
            relativeOutwardSpeed
                * damping;

        tension =
            clamp(
                springForce
                    + dampingForce,
                0.0,
                lineProfile.maxTension()
                    * 1.35
            );
    }

    private double reelEfficiencyFor(
        double load
    ) {
        if (load <= 0.0) {
            return 1.0;
        }

        double normalized =
            Math.min(
                1.0,
                load / DRAG_START_RATIO
            );

        return Math.max(
            0.10,
            1.0
                - 0.84
                    * Math.pow(
                        normalized,
                        1.65
                    )
        );
    }

    private double automaticDragSlip(
        double load
    ) {
        if (load <= DRAG_START_RATIO) {
            return 0.0;
        }

        double normalized =
            clamp(
                (
                    load
                        - DRAG_START_RATIO
                )
                    / (
                        DRAG_FULL_RATIO
                            - DRAG_START_RATIO
                    ),
                0.0,
                1.0
            );

        double smooth =
            normalized
                * normalized
                * (
                    3.0
                        - 2.0
                            * normalized
                );

        return MAX_DRAG_PAYOUT
            * smooth;
    }

    private void updateStamina(
        double averageLoad
    ) {
        if (
            averageLoad >= 0.24
                && lastIntent.effort()
                    > 0.05
        ) {
            double work =
                Math.min(
                    1.2,
                    averageLoad
                )
                    * (
                        0.30
                            + lastIntent.effort()
                                * 0.70
                    );

            stamina =
                Math.max(
                    0.0,
                    stamina
                        - fishProfile
                            .staminaDrainRate()
                            * work
                            * 0.55
                );

            return;
        }

        if (
            averageLoad < 0.12
                && !lastIntent.burst()
        ) {
            stamina =
                Math.min(
                    fishProfile.maxStamina(),
                    stamina
                        + fishProfile
                            .recoveryRate()
                        * 0.75
                );
        }
    }

    private void updateLandingProgress() {
        double span =
            Math.max(
                0.001,
                initialDistance
                    - lineProfile
                        .catchDistance()
            );

        landingProgress =
            clamp(
                (
                    initialDistance
                        - distance
                ) / span,
                0.0,
                1.0
            );
    }

    private void updateFailurePressure() {
        double load =
            tensionRatio();

        if (
            load
                < lineProfile
                    .slackThreshold()
                    / lineProfile
                        .maxTension()
                && distance
                    > lineProfile
                        .catchDistance()
                        + 0.25
        ) {
            slackTicks++;
        } else {
            slackTicks =
                Math.max(
                    0,
                    slackTicks - 2
                );
        }

        if (load >= BREAK_TENSION_RATIO) {
            double excess =
                load
                    - BREAK_TENSION_RATIO;

            double velocityPenalty =
                Math.max(
                    0.0,
                    fishVelocity
                        - lineVelocity
                ) * 7.0;

            breakHeat +=
                0.75
                    + excess * 4.0
                    + velocityPenalty;
        } else if (load < HIGH_TENSION_RATIO) {
            breakHeat =
                Math.max(
                    0.0,
                    breakHeat - 2.6
                );
        } else {
            breakHeat =
                Math.max(
                    0.0,
                    breakHeat - 0.65
                );
        }

        overTensionTicks =
            (int) Math.round(
                breakHeat
            );
    }

    private void updatePhase() {
        if (
            breakHeat
                >= lineProfile
                    .breakGraceTicks()
        ) {
            phase =
                FightPhase.LINE_BROKEN;
            return;
        }

        if (
            slackTicks
                >= fishProfile
                    .slackEscapeTicks()
        ) {
            phase =
                FightPhase.ESCAPED;
            return;
        }

        boolean closeEnough =
            distance
                <= lineProfile
                    .catchDistance()
                    + 0.12;

        boolean lineRecovered =
            lineLength
                <= lineProfile
                    .catchDistance()
                    + 0.42;

        boolean notCritical =
            tensionRatio() < 0.96;

        if (
            closeEnough
                && lineRecovered
                && notCritical
        ) {
            landingProgress = 1.0;
            phase =
                FightPhase.CAUGHT;
            return;
        }

        phase =
            staminaRatio()
                    <= fishProfile
                        .tiredThreshold()
                ? FightPhase.TIRED
                : FightPhase.FIGHTING;
    }

    private double breakRisk() {
        return clamp(
            breakHeat
                / lineProfile
                    .breakGraceTicks(),
            0.0,
            1.0
        );
    }

    private double staminaRatio() {
        return fishProfile.maxStamina()
                <= 0.0
            ? 0.0
            : stamina
                / fishProfile
                    .maxStamina();
    }

    private double tensionRatio() {
        return lineProfile.maxTension()
                <= 0.0
            ? 0.0
            : tension
                / lineProfile
                    .maxTension();
    }

    private static double clamp(
        double value,
        double min,
        double max
    ) {
        return Math.max(
            min,
            Math.min(
                max,
                value
            )
        );
    }
}
