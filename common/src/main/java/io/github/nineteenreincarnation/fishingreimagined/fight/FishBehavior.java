package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.random.RandomGenerator;

@FunctionalInterface
public interface FishBehavior {
    FishBehaviorSession createSession(FishProfile profile, RandomGenerator random);
}
