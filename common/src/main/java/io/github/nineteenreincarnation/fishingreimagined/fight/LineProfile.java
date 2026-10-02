package io.github.nineteenreincarnation.fishingreimagined.fight;

public record LineProfile(
    double maxTension,
    double slackThreshold,
    double stiffness,
    double reelRate,
    double payoutRate,
    double pullbackRate,
    int breakGraceTicks,
    double catchDistance,
    double maxLineLength
) {
    public static final LineProfile PROTOTYPE =
        new LineProfile(
            100.0,
            8.0,
            72.0,
            0.11,
            0.18,
            0.065,
            18,
            1.5,
            40.0
        );

    public LineProfile {
        requirePositive(
            maxTension,
            "maxTension"
        );
        requireNonNegative(
            slackThreshold,
            "slackThreshold"
        );

        if (slackThreshold >= maxTension) {
            throw new IllegalArgumentException(
                "slackThreshold must be below maxTension"
            );
        }

        requirePositive(
            stiffness,
            "stiffness"
        );
        requirePositive(
            reelRate,
            "reelRate"
        );
        requirePositive(
            payoutRate,
            "payoutRate"
        );
        requirePositive(
            pullbackRate,
            "pullbackRate"
        );

        if (breakGraceTicks < 1) {
            throw new IllegalArgumentException(
                "breakGraceTicks must be >= 1"
            );
        }

        requirePositive(
            catchDistance,
            "catchDistance"
        );

        if (
            !Double.isFinite(
                maxLineLength
            )
                || maxLineLength
                    <= catchDistance
        ) {
            throw new IllegalArgumentException(
                "maxLineLength must be finite and greater than catchDistance"
            );
        }
    }

    private static void requirePositive(
        double value,
        String name
    ) {
        if (
            !Double.isFinite(value)
                || value <= 0.0
        ) {
            throw new IllegalArgumentException(
                name
                    + " must be finite and > 0"
            );
        }
    }

    private static void requireNonNegative(
        double value,
        String name
    ) {
        if (
            !Double.isFinite(value)
                || value < 0.0
        ) {
            throw new IllegalArgumentException(
                name
                    + " must be finite and >= 0"
            );
        }
    }
}
