package io.github.nineteenreincarnation.fishingreimagined.fight;

public record FightSnapshot(
    long tick,
    FightPhase phase,
    double stamina,
    double maxStamina,
    double distance,
    double initialDistance,
    double lineLength,
    double tension,
    double maxTension,
    double fishVelocity,
    double lineVelocity,
    double reelEfficiency,
    double dragSlip,
    double landingProgress,
    double breakRisk,
    int slackTicks,
    int overTensionTicks,
    FishIntent fishIntent
) {
    public double staminaRatio() {
        return maxStamina <= 0.0
            ? 0.0
            : clamp01(
                stamina / maxStamina
            );
    }

    public double tensionRatio() {
        return maxTension <= 0.0
            ? 0.0
            : Math.max(
                0.0,
                tension / maxTension
            );
    }

    public boolean isTerminal() {
        return phase.isTerminal();
    }

    private static double clamp01(
        double value
    ) {
        return Math.max(
            0.0,
            Math.min(
                1.0,
                value
            )
        );
    }
}
