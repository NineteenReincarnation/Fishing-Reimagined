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
    private static int lastTensionBand = -1;

    private static int reelCooldown;
    private static int strainCooldown;
    private static int dragCooldown;

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

        float tension =
            access
                .fishingReimagined$tensionRatio();

        float lineVelocity =
            access
                .fishingReimagined$lineVelocity();

        float dragSlip =
            access
                .fishingReimagined$dragSlip();

        int fishState =
            access
                .fishingReimagined$fishState();

        int tensionBand =
            tensionBand(tension);

        if (!active) {
            active = true;
            lastFishState = fishState;
            lastTensionBand = tensionBand;
        }

        if (fishState != lastFishState) {
            if (fishState == 1) {
                level.playLocalSound(
                    hook,
                    SoundEvents
                        .FISHING_BOBBER_SPLASH,
                    SoundSource.PLAYERS,
                    0.60F,
                    0.86F
                );
            } else if (fishState == 4) {
                level.playLocalSound(
                    hook,
                    SoundEvents.FISH_SWIM,
                    SoundSource.PLAYERS,
                    0.24F,
                    0.92F
                );
            } else if (
                fishState == 2
                    || fishState == 5
            ) {
                level.playLocalSound(
                    hook,
                    SoundEvents.FISH_SWIM,
                    SoundSource.PLAYERS,
                    0.20F,
                    0.72F
                );
            }

            lastFishState = fishState;
        }

        if (
            tensionBand
                != lastTensionBand
        ) {
            if (tensionBand == 2) {
                level.playLocalSound(
                    hook,
                    SoundEvents
                        .TRIPWIRE_CLICK_ON,
                    SoundSource.PLAYERS,
                    0.26F,
                    1.18F
                );
            } else if (
                tensionBand == 3
            ) {
                level.playLocalSound(
                    hook,
                    SoundEvents
                        .TRIPWIRE_ATTACH,
                    SoundSource.PLAYERS,
                    0.38F,
                    1.42F
                );
            } else if (
                tensionBand == 0
                    && lastTensionBand > 0
            ) {
                level.playLocalSound(
                    hook,
                    SoundEvents
                        .TRIPWIRE_CLICK_OFF,
                    SoundSource.PLAYERS,
                    0.20F,
                    0.78F
                );
            }

            lastTensionBand =
                tensionBand;
        }

        if (reelCooldown > 0) {
            reelCooldown--;
        }

        if (strainCooldown > 0) {
            strainCooldown--;
        }

        if (dragCooldown > 0) {
            dragCooldown--;
        }

        ReelAction action =
            ClientFishingInput
                .currentAction();

        float actualReel =
            Math.max(
                0.0F,
                -lineVelocity
            );

        if (
            action == ReelAction.REEL_IN
                && actualReel > 0.012F
                && reelCooldown <= 0
        ) {
            float intensity =
                Math.min(
                    1.0F,
                    actualReel / 0.11F
                );

            level.playLocalSound(
                hook,
                SoundEvents
                    .FISHING_BOBBER_RETRIEVE,
                SoundSource.PLAYERS,
                0.08F
                    + intensity * 0.08F,
                1.10F
                    + intensity * 0.28F
            );

            reelCooldown =
                9
                    - Math.round(
                        intensity * 4.0F
                    );
        }

        if (
            dragSlip > 0.008F
                && dragCooldown <= 0
        ) {
            float drag =
                Math.min(
                    1.0F,
                    dragSlip / 0.16F
                );

            level.playLocalSound(
                hook,
                SoundEvents
                    .TRIPWIRE_CLICK_ON,
                SoundSource.PLAYERS,
                0.18F
                    + drag * 0.18F,
                0.72F
                    + drag * 0.30F
            );

            dragCooldown =
                Math.max(
                    3,
                    9
                        - Math.round(
                            drag * 5.0F
                        )
                );
        }

        if (
            tensionBand >= 2
                && strainCooldown <= 0
        ) {
            level.playLocalSound(
                hook,
                SoundEvents
                    .TRIPWIRE_CLICK_ON,
                SoundSource.PLAYERS,
                tensionBand == 3
                    ? 0.19F
                    : 0.11F,
                tensionBand == 3
                    ? 0.62F
                    : 0.82F
            );

            strainCooldown =
                tensionBand == 3
                    ? 8
                    : 15;
        }
    }

    private static int tensionBand(
        float tension
    ) {
        if (tension < 0.08F) {
            return 0;
        }

        if (tension < 0.78F) {
            return 1;
        }

        if (tension < 0.96F) {
            return 2;
        }

        return 3;
    }

    private static void reset() {
        active = false;
        lastFishState = -1;
        lastTensionBand = -1;
        reelCooldown = 0;
        strainCooldown = 0;
        dragCooldown = 0;
    }
}
