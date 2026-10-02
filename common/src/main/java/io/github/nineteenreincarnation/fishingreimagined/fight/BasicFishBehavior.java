package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.random.RandomGenerator;

public final class BasicFishBehavior implements FishBehavior {
    public static final BasicFishBehavior INSTANCE =
        new BasicFishBehavior();

    private BasicFishBehavior() {
    }

    @Override
    public FishBehaviorSession createSession(
        FishProfile profile,
        RandomGenerator random
    ) {
        return new Session(
            profile,
            random
        );
    }

    private static final class Session
        implements FishBehaviorSession {

        private final FishProfile profile;
        private final RandomGenerator random;

        private FishFightMode mode =
            FishFightMode.PROBING;

        private int modeTicks;
        private double turnBias;

        private Session(
            FishProfile profile,
            RandomGenerator random
        ) {
            this.profile = profile;
            this.random = random;
            enter(
                FishFightMode.PROBING
            );
        }

        @Override
        public FishIntent nextIntent(
            FightSnapshot snapshot
        ) {
            if (
                snapshot.staminaRatio()
                    <= profile.tiredThreshold()
                && mode != FishFightMode.TIRED
            ) {
                enter(
                    FishFightMode.TIRED
                );
            }

            if (--modeTicks <= 0) {
                transition(snapshot);
            }

            turnBias =
                turnBias * turnDecay()
                    + random.nextDouble(
                        -turnNoise(),
                        turnNoise()
                    );

            return switch (mode) {
                case PROBING ->
                    new FishIntent(
                        profile.cruiseSpeed() * 0.72,
                        turnBias,
                        0.30,
                        false,
                        mode
                    );

                case PULLING ->
                    new FishIntent(
                        profile.cruiseSpeed() * 1.55,
                        turnBias,
                        0.68,
                        false,
                        mode
                    );

                case BURST ->
                    new FishIntent(
                        profile.cruiseSpeed()
                            * profile.burstMultiplier()
                            * 1.12,
                        turnBias,
                        1.0,
                        true,
                        mode
                    );

                case RECOVERING ->
                    new FishIntent(
                        -profile.cruiseSpeed() * 0.28,
                        turnBias,
                        0.10,
                        false,
                        mode
                    );

                case TIRED ->
                    new FishIntent(
                        profile.cruiseSpeed() * 0.22,
                        turnBias,
                        0.16,
                        false,
                        mode
                    );
            };
        }

        private void transition(
            FightSnapshot snapshot
        ) {
            switch (mode) {
                case PROBING ->
                    enter(
                        FishFightMode.PULLING
                    );

                case PULLING -> {
                    double burstChance =
                        Math.min(
                            0.72,
                            0.28
                                + profile
                                    .burstChancePerTick()
                                    * 20.0
                        );

                    if (
                        random.nextDouble()
                            < burstChance
                        && snapshot.staminaRatio()
                            > profile.tiredThreshold()
                                + 0.08
                    ) {
                        enter(
                            FishFightMode.BURST
                        );
                    } else {
                        enter(
                            FishFightMode.RECOVERING
                        );
                    }
                }

                case BURST ->
                    enter(
                        FishFightMode.RECOVERING
                    );

                case RECOVERING ->
                    enter(
                        snapshot.staminaRatio()
                                <= profile.tiredThreshold()
                            ? FishFightMode.TIRED
                            : FishFightMode.PROBING
                    );

                case TIRED -> {
                    if (
                        snapshot.staminaRatio()
                            > profile.tiredThreshold()
                                + 0.12
                    ) {
                        enter(
                            FishFightMode.PROBING
                        );
                    } else {
                        enter(
                            FishFightMode.TIRED
                        );
                    }
                }
            }
        }

        private void enter(
            FishFightMode next
        ) {
            mode = next;

            modeTicks = switch (next) {
                case PROBING ->
                    random.nextInt(
                        18,
                        37
                    );

                case PULLING ->
                    random.nextInt(
                        18,
                        43
                    );

                case BURST ->
                    random.nextInt(
                        profile.burstMinTicks(),
                        profile.burstMaxTicks()
                            + 1
                    );

                case RECOVERING ->
                    random.nextInt(
                        16,
                        34
                    );

                case TIRED ->
                    random.nextInt(
                        28,
                        52
                    );
            };

            double biasRange =
                switch (next) {
                    case BURST -> 0.22;
                    case PULLING -> 0.12;
                    case PROBING -> 0.08;
                    case RECOVERING -> 0.05;
                    case TIRED -> 0.03;
                };

            turnBias =
                random.nextDouble(
                    -biasRange,
                    biasRange
                );
        }

        private double turnDecay() {
            return switch (mode) {
                case BURST -> 0.94;
                case PULLING -> 0.90;
                case PROBING -> 0.86;
                case RECOVERING -> 0.78;
                case TIRED -> 0.72;
            };
        }

        private double turnNoise() {
            return switch (mode) {
                case BURST -> 0.040;
                case PULLING -> 0.026;
                case PROBING -> 0.018;
                case RECOVERING -> 0.012;
                case TIRED -> 0.008;
            };
        }
    }
}
