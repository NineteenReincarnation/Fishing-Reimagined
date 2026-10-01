package io.github.nineteenreincarnation.fishingreimagined.fight;

public record FishProfile(
    double cruiseSpeed,
    double pullStrength,
    double maxStamina,
    double burstChancePerTick,
    double burstMultiplier,
    int burstMinTicks,
    int burstMaxTicks,
    double staminaDrainRate,
    double recoveryRate,
    int slackEscapeTicks,
    double tiredThreshold
) {
    public static final FishProfile PROTOTYPE = new FishProfile(
        0.022,
        1.0,
        100.0,
        0.015,
        4.0,
        8,
        20,
        0.34,
        0.08,
        50,
        0.25
    );

    public FishProfile {
        requireNonNegative(cruiseSpeed, "cruiseSpeed");
        requirePositive(pullStrength, "pullStrength");
        requirePositive(maxStamina, "maxStamina");

        if (burstChancePerTick < 0.0 || burstChancePerTick > 1.0) {
            throw new IllegalArgumentException("burstChancePerTick must be in [0, 1]");
        }
        if (burstMultiplier < 1.0) {
            throw new IllegalArgumentException("burstMultiplier must be >= 1");
        }
        if (burstMinTicks < 1 || burstMaxTicks < burstMinTicks) {
            throw new IllegalArgumentException("invalid burst duration range");
        }

        requireNonNegative(staminaDrainRate, "staminaDrainRate");
        requireNonNegative(recoveryRate, "recoveryRate");

        if (slackEscapeTicks < 1) {
            throw new IllegalArgumentException("slackEscapeTicks must be >= 1");
        }
        if (tiredThreshold < 0.0 || tiredThreshold > 1.0) {
            throw new IllegalArgumentException("tiredThreshold must be in [0, 1]");
        }
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and > 0");
        }
    }

    private static void requireNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and >= 0");
        }
    }
}
