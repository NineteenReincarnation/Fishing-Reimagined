package io.github.nineteenreincarnation.fishingreimagined.client;

import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import io.github.nineteenreincarnation.fishingreimagined.network.ReelInputPayload;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.projectile.FishingHook;

public final class ClientFishingInput {
    private static Consumer<ReelInputPayload> sender = payload -> {
    };
    private static ReelAction lastSent = ReelAction.HOLD;

    private ClientFishingInput() {
    }

    public static void installSender(Consumer<ReelInputPayload> packetSender) {
        sender = Objects.requireNonNull(packetSender, "packetSender");
    }

    public static void tick(Minecraft minecraft) {
        ClientSpecialFishingMode.tick(minecraft);
        ClientHookedFishVisuals.tick(minecraft);

        ReelAction action = ReelAction.HOLD;
        if (
            isFightActive(minecraft)
                && !isCatchHanging(minecraft)
        ) {
            action = minecraft.options.keyUse.isDown()
                ? ReelAction.REEL_IN
                : ReelAction.PAY_OUT;
        }

        sendIfChanged(action);
        ClientFishingAudio.tick(minecraft);
    }

    public static boolean isFightActive(Minecraft minecraft) {
        if (minecraft.player == null) {
            return false;
        }

        FishingHook hook = minecraft.player.fishing;
        return hook instanceof FishingHookFightAccess access
            && access.fishingReimagined$isFightActive();
    }

    public static boolean isCatchHanging(
        Minecraft minecraft
    ) {
        if (minecraft.player == null) {
            return false;
        }

        FishingHook hook =
            minecraft.player.fishing;

        return hook
            instanceof FishingHookFightAccess access
            && access
                .fishingReimagined$isCaughtHanging();
    }

    public static ReelAction currentAction() {
        return lastSent;
    }

    private static void sendIfChanged(ReelAction action) {
        if (action == lastSent) {
            return;
        }

        lastSent = action;
        sender.accept(new ReelInputPayload(action));
    }
}
