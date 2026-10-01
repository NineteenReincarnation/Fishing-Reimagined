package io.github.nineteenreincarnation.fishingreimagined.fight;

public enum FightPhase {
    FIGHTING,
    TIRED,
    CAUGHT,
    ESCAPED,
    LINE_BROKEN;

    public boolean isTerminal() {
        return this == CAUGHT || this == ESCAPED || this == LINE_BROKEN;
    }
}
