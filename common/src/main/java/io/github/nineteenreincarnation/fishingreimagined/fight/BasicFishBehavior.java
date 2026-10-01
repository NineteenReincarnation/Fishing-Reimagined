package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.random.RandomGenerator;

public final class BasicFishBehavior implements FishBehavior {
    public static final BasicFishBehavior INSTANCE = new BasicFishBehavior();

    private BasicFishBehavior() {
    }

    @Override
    public FishBehaviorSession createSession(FishProfile profile, RandomGenerator random) {
        return new Session(profile, random);
    }

    private static final class Session implements FishBehaviorSession {
        private final FishProfile profile;
        private final RandomGenerator random;
        private int burstTicks;
        private double turnBias;

        private Session(FishProfile profile, RandomGenerator random) {
            this.profile = profile;
            this.random = random;
        }

        @Override
        public FishIntent nextIntent(FightSnapshot snapshot) {
            if (burstTicks <= 0 && random.nextDouble() < profile.burstChancePerTick()) {
                burstTicks = random.nextInt(profile.burstMinTicks(), profile.burstMaxTicks() + 1);
                turnBias = random.nextDouble(-0.16, 0.16);
            }

            if (burstTicks > 0) {
                burstTicks--;
                return new FishIntent(
                    profile.cruiseSpeed() * profile.burstMultiplier(),
                    turnBias + random.nextDouble(-0.05, 0.05),
                    1.0,
                    true
                );
            }

            turnBias = turnBias * 0.82 + random.nextDouble(-0.025, 0.025);
            return new FishIntent(profile.cruiseSpeed(), turnBias, 0.28, false);
        }
    }
}
