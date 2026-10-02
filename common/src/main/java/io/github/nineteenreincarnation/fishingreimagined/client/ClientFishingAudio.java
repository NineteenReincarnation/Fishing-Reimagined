package io.github.nineteenreincarnation.fishingreimagined.client;

import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FishingHook;

public final class ClientFishingAudio {
    private static boolean active;
    private static int lastFishState = -1;
    private static int reelCooldown;

    private ClientFishingAudio() {
    }

    public static void tick(
        Minecraft minecraft
    ) {
        if (
            minecraft.player == null
                || minecraft.level == null
        ) {
            reset();
            return;
        }

        FishingHook hook =
            minecraft.player.fishing;

        if (!(hook
            instanceof FishingHookFightAccess access)
            || !access
                .fishingReimagined$isFightActive()) {
            reset();
            return;
        }

        ClientLevel level =
            minecraft.level;

        int fishState =
            access
                .fishingReimagined$fishState();

        if (!active) {
            active = true;
            lastFishState = fishState;
        }

        if (fishState != lastFishState) {
            if (fishState == 1) {
                level.playLocalSound(
                    hook,
                    SoundEvents
                        .FISHING_BOBBER_SPLASH,
                    SoundSource.PLAYERS,
                    0.48F,
                    0.92F
                );
            } else if (fishState == 4) {
                level.playLocalSound(
                    hook,
                    SoundEvents.FISH_SWIM,
                    SoundSource.PLAYERS,
                    0.20F,
                    0.98F
                );
            }

            lastFishState =
                fishState;
        }

        if (reelCooldown > 0) {
            reelCooldown--;
        }

        ReelAction action =
            ClientFishingInput.currentAction();

        float zoneSpeed =
            Math.abs(
                access
                    .fishingReimagined$lineVelocity()
            );

        if (
            action == ReelAction.REEL_IN
                && zoneSpeed > 0.004F
                && reelCooldown <= 0
        ) {
            float intensity =
                Math.min(
                    1.0F,
                    zoneSpeed / 0.045F
                );

            level.playLocalSound(
                hook,
                SoundEvents
                    .FISHING_BOBBER_RETRIEVE,
                SoundSource.PLAYERS,
                0.07F
                    + intensity * 0.06F,
                1.18F
                    + intensity * 0.22F
            );

            reelCooldown =
                Math.max(
                    5,
                    9
                        - Math.round(
                            intensity * 3.0F
                        )
                );
        }
    }

    private static void reset() {
        active = false;
        lastFishState = -1;
        reelCooldown = 0;
    }
}
