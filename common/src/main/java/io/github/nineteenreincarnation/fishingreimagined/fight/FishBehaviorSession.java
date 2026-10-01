package io.github.nineteenreincarnation.fishingreimagined.fight;

@FunctionalInterface
public interface FishBehaviorSession {
    FishIntent nextIntent(FightSnapshot snapshot);
}
