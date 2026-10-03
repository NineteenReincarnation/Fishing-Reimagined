package io.github.nineteenreincarnation.fishingreimagined.client;

import java.util.Locale;
import net.minecraft.client.Minecraft;

public final class ClientAnimationCompatibility {
    public enum ActiveMode {
        BUILTIN,
        RESOURCE_PACK_OVERLAY
    }

    private static Object lastResourceManager;
    private static int rescanTicks;

    private static boolean fishAnimationPack;
    private static boolean playerAnimationPack;

    private ClientAnimationCompatibility() {
    }

    public static void tick(
        Minecraft minecraft
    ) {
        Object resourceManager =
            minecraft.getResourceManager();

        if (
            resourceManager != lastResourceManager
                || rescanTicks-- <= 0
        ) {
            lastResourceManager =
                resourceManager;

            rescanTicks = 200;

            scan(minecraft);
        }
    }

    public static ActiveMode fishMode() {
        return fishAnimationPack
            ? ActiveMode.RESOURCE_PACK_OVERLAY
            : ActiveMode.BUILTIN;
    }

    public static ActiveMode playerMode() {
        return playerAnimationPack
            ? ActiveMode.RESOURCE_PACK_OVERLAY
            : ActiveMode.BUILTIN;
    }

    public static boolean fishAnimationPackDetected() {
        return fishAnimationPack;
    }

    public static boolean playerAnimationPackDetected() {
        return playerAnimationPack;
    }

    public static float fishProceduralStrength() {
        return fishAnimationPack
            ? 0.0F
            : 1.0F;
    }

    public static float fishPhysicalPitchStrength() {
        return fishAnimationPack
            ? 0.35F
            : 1.0F;
    }

    public static float rodOverlayStrength() {
        return playerAnimationPack
            ? 0.30F
            : 1.0F;
    }

    private static void scan(
        Minecraft minecraft
    ) {
        String preference =
            SpecialFishingConfig
                .animationCompatibilityMode();

        if ("builtin".equals(preference)) {
            fishAnimationPack = false;
            playerAnimationPack = false;
            return;
        }

        if (
            "resource_pack".equals(preference)
                || "overlay".equals(preference)
        ) {
            fishAnimationPack = true;
            playerAnimationPack = true;
            return;
        }

        boolean fishCem =
            hasMatchingResource(
                minecraft,
                "optifine/cem",
                true
            )
                || hasMatchingResource(
                    minecraft,
                    "emf/cem",
                    true
                );

        boolean animationJson =
            hasAnyResource(
                minecraft,
                "animations"
            )
                || hasAnyResource(
                    minecraft,
                    "player_animations"
                );

        fishAnimationPack =
            fishCem;

        playerAnimationPack =
            animationJson
                || hasPlayerCem(
                    minecraft,
                    "optifine/cem"
                )
                || hasPlayerCem(
                    minecraft,
                    "emf/cem"
                );
    }

    private static boolean hasMatchingResource(
        Minecraft minecraft,
        String prefix,
        boolean fishOnly
    ) {
        try {
            return !minecraft
                .getResourceManager()
                .listResources(
                    prefix,
                    id -> {
                        String path =
                            id.toString()
                                .toLowerCase(
                                    Locale.ROOT
                                );

                        if (
                            !(
                                path.endsWith(".jem")
                                    || path.endsWith(".jpm")
                                    || path.endsWith(".properties")
                                    || path.endsWith(".json")
                            )
                        ) {
                            return false;
                        }

                        if (!fishOnly) {
                            return true;
                        }

                        return path.contains("salmon")
                            || path.contains("cod")
                            || path.contains("puffer")
                            || path.contains("tropical")
                            || path.contains("fish");
                    }
                )
                .isEmpty();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean hasPlayerCem(
        Minecraft minecraft,
        String prefix
    ) {
        try {
            return !minecraft
                .getResourceManager()
                .listResources(
                    prefix,
                    id -> {
                        String path =
                            id.toString()
                                .toLowerCase(
                                    Locale.ROOT
                                );

                        return path.contains("player")
                            || path.contains("arm")
                            || path.contains("fishing_rod");
                    }
                )
                .isEmpty();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean hasAnyResource(
        Minecraft minecraft,
        String prefix
    ) {
        try {
            return !minecraft
                .getResourceManager()
                .listResources(
                    prefix,
                    id -> true
                )
                .isEmpty();
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
