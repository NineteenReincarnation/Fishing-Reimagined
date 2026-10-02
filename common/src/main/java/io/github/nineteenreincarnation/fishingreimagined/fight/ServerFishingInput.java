package io.github.nineteenreincarnation.fishingreimagined.fight;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ServerFishingInput {
    private static final Map<UUID, ReelAction> ACTIONS = new ConcurrentHashMap<>();

    private ServerFishingInput() {
    }

    public static void update(ServerPlayer player, ReelAction action) {
        if (action == ReelAction.HOLD) {
            ACTIONS.remove(player.getUUID());
        } else {
            ACTIONS.put(player.getUUID(), action);
        }
    }

    public static ReelAction actionFor(Player player) {
        return ACTIONS.getOrDefault(player.getUUID(), ReelAction.HOLD);
    }

    public static void clear(Player player) {
        ACTIONS.remove(player.getUUID());
    }
}
