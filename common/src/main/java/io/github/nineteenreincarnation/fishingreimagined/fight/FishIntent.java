package io.github.nineteenreincarnation.fishingreimagined.fight;

public record FishIntent(
    double outwardVelocity,
    double lateralTurnRadians,
    double effort,
    boolean burst,
    FishFightMode mode
) {
    public static final FishIntent CALM =
        new FishIntent(
            0.0,
            0.0,
            0.0,
            false,
            FishFightMode.RECOVERING
        );

    public FishIntent(
        double outwardVelocity,
        double lateralTurnRadians,
        double effort,
        boolean burst
    ) {
        this(
            outwardVelocity,
            lateralTurnRadians,
            effort,
            burst,
            burst
                ? FishFightMode.BURST
                : FishFightMode.PROBING
        );
    }

    public FishIntent {
        if (!Double.isFinite(outwardVelocity)) {
            throw new IllegalArgumentException(
                "outwardVelocity must be finite"
            );
        }
        if (!Double.isFinite(lateralTurnRadians)) {
            throw new IllegalArgumentException(
                "lateralTurnRadians must be finite"
            );
        }
        if (!Double.isFinite(effort)
            || effort < 0.0
            || effort > 1.0) {
            throw new IllegalArgumentException(
                "effort must be finite and in [0, 1]"
            );
        }
        if (mode == null) {
            throw new IllegalArgumentException(
                "mode must not be null"
            );
        }
    }
}
