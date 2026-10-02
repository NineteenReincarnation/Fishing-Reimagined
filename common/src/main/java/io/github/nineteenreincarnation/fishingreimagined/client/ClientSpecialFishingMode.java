package io.github.nineteenreincarnation.fishingreimagined.client;

import io.github.nineteenreincarnation.fishingreimagined.network.FishingModePayload;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class ClientSpecialFishingMode {
    public static final KeyMapping TOGGLE_KEY =
        new KeyMapping(
            "key.fishing_reimagined.toggle_special_fishing",
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PERIOD,
            KeyMapping.Category.MISC
        );

    private static Consumer<FishingModePayload> sender =
        payload -> {
        };

    private static UUID syncedPlayer;

    private ClientSpecialFishingMode() {
    }

    public static void installSender(
        Consumer<FishingModePayload> packetSender
    ) {
        sender =
            Objects.requireNonNull(
                packetSender,
                "packetSender"
            );
    }

    public static void tick(
        Minecraft minecraft
    ) {
        if (minecraft.player == null) {
            syncedPlayer = null;
            return;
        }

        UUID playerId =
            minecraft.player.getUUID();

        if (!playerId.equals(syncedPlayer)) {
            syncedPlayer = playerId;
            sync();
        }

        while (TOGGLE_KEY.consumeClick()) {
            boolean enabled =
                SpecialFishingConfig
                    .toggleSpecialFishing();

            sync();

            minecraft.player
                .displayClientMessage(
                    Component.translatable(
                        enabled
                            ? "message.fishing_reimagined.mode_enabled"
                            : "message.fishing_reimagined.mode_disabled"
                    ),
                    true
                );
        }
    }

    public static boolean enabled() {
        return SpecialFishingConfig
            .specialFishingEnabled();
    }

    private static void sync() {
        sender.accept(
            new FishingModePayload(
                SpecialFishingConfig
                    .specialFishingEnabled()
            )
        );
    }
}
