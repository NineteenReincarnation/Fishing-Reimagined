package io.github.nineteenreincarnation.fishingreimagined;

import java.util.concurrent.atomic.AtomicBoolean;

public final class FishingReimagined {
    public static final String MOD_ID = "fishing_reimagined";

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    private FishingReimagined() {
    }

    public static void initialize() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        // Shared registrations attach here as Minecraft-facing systems are added.
    }
}
