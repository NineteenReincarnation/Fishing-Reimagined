package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ServerFishingMode {
    private static final Map<UUID, Boolean>
        ENABLED =
            new ConcurrentHashMap<>();

    private ServerFishingMode() {
    }

    public static void update(
        ServerPlayer player,
        boolean enabled
    ) {
        if (enabled) {
            ENABLED.put(
                player.getUUID(),
                true
            );
        } else {
            ENABLED.remove(
                player.getUUID()
            );
        }
    }

    public static boolean enabledFor(
        Player player
    ) {
        return ENABLED.getOrDefault(
            player.getUUID(),
            false
        );
    }
}
