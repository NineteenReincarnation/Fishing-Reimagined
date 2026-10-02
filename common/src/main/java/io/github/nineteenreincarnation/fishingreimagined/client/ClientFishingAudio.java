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

    private ClientFishingAudio() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null
            || minecraft.level == null) {
            reset();
            return;
        }

        FishingHook hook = minecraft.player.fishing;
        if (!(hook instanceof FishingHookFightAccess access)
            || !access.fishingReimagined$isFightActive()) {
            reset();
            return;
        }

        ClientLevel level = minecraft.level;
        float tension =
            access.fishingReimagined$tensionRatio();
        int fishState =
            access.fishingReimagined$fishState();
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
                    SoundEvents.FISHING_BOBBER_SPLASH,
                    SoundSource.PLAYERS,
                    0.48F,
                    0.92F
                );
            } else if (fishState == 2) {
                level.playLocalSound(
                    hook,
                    SoundEvents.FISH_SWIM,
                    SoundSource.PLAYERS,
                    0.28F,
                    0.72F
                );
            }
            lastFishState = fishState;
        }

        if (tensionBand != lastTensionBand) {
            if (tensionBand == 2) {
                level.playLocalSound(
                    hook,
                    SoundEvents.TRIPWIRE_CLICK_ON,
                    SoundSource.PLAYERS,
                    0.22F,
                    1.35F
                );
            } else if (tensionBand == 3) {
                level.playLocalSound(
                    hook,
                    SoundEvents.TRIPWIRE_ATTACH,
                    SoundSource.PLAYERS,
                    0.30F,
                    1.55F
                );
            } else if (tensionBand == 0
                && lastTensionBand > 0) {
                level.playLocalSound(
                    hook,
                    SoundEvents.TRIPWIRE_CLICK_OFF,
                    SoundSource.PLAYERS,
                    0.18F,
                    0.82F
                );
            }
            lastTensionBand = tensionBand;
        }

        if (reelCooldown > 0) {
            reelCooldown--;
        }
        if (strainCooldown > 0) {
            strainCooldown--;
        }

        ReelAction action =
            ClientFishingInput.currentAction();

        if (action == ReelAction.REEL_IN
            && tensionBand >= 1
            && tensionBand <= 2
            && reelCooldown <= 0) {
            level.playLocalSound(
                hook,
                SoundEvents.FISHING_BOBBER_RETRIEVE,
                SoundSource.PLAYERS,
                0.11F,
                1.28F
            );
            reelCooldown = 7;
        }

        if (tensionBand >= 2
            && strainCooldown <= 0) {
            level.playLocalSound(
                hook,
                SoundEvents.TRIPWIRE_CLICK_ON,
                SoundSource.PLAYERS,
                tensionBand == 3 ? 0.17F : 0.10F,
                tensionBand == 3 ? 0.68F : 0.86F
            );
            strainCooldown =
                tensionBand == 3 ? 8 : 14;
        }
    }

    private static int tensionBand(float tension) {
        if (tension < 0.18F) {
            return 0;
        }
        if (tension < 0.70F) {
            return 1;
        }
        if (tension < 0.95F) {
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
    }
}
