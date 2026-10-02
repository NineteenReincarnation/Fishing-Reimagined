package io.github.nineteenreincarnation.fishingreimagined.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ClientFishingPose {
    private static float smoothedProgress;
    private static float smoothedFishVelocity;
    private static float smoothedZoneVelocity;
    private static float smoothedMiss;

    private static ReelAction action =
        ReelAction.HOLD;

    private static boolean active;

    private ClientFishingPose() {
    }

    public static void tick(
        Minecraft minecraft
    ) {
        if (minecraft.player == null) {
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

        active = true;
        action =
            ClientFishingInput.currentAction();

        smoothedProgress =
            Mth.lerp(
                0.18F,
                smoothedProgress,
                Mth.clamp(
                    access
                        .fishingReimagined$catchProgress(),
                    0.0F,
                    1.0F
                )
            );

        smoothedFishVelocity =
            Mth.lerp(
                0.32F,
                smoothedFishVelocity,
                access
                    .fishingReimagined$fishVelocity()
            );

        smoothedZoneVelocity =
            Mth.lerp(
                0.30F,
                smoothedZoneVelocity,
                access
                    .fishingReimagined$lineVelocity()
            );

        float separation =
            Math.abs(
                access
                    .fishingReimagined$fishTrackPosition()
                    - access
                        .fishingReimagined$catchZonePosition()
            );

        float halfWidth =
            access
                .fishingReimagined$catchZoneWidth()
                * 0.5F;

        float miss =
            Math.max(
                0.0F,
                separation - halfWidth
            );

        smoothedMiss =
            Mth.lerp(
                0.25F,
                smoothedMiss,
                miss
            );
    }

    public static void applyFirstPersonRodTransform(
        LivingEntity entity,
        ItemStack stack,
        ItemDisplayContext context,
        PoseStack poseStack
    ) {
        if (!active
            || !(entity
                instanceof LocalPlayer player)
            || !stack.is(Items.FISHING_ROD)
            || (
                context
                    != ItemDisplayContext
                        .FIRST_PERSON_RIGHT_HAND
                && context
                    != ItemDisplayContext
                        .FIRST_PERSON_LEFT_HAND
            )) {
            return;
        }

        float handSign =
            context
                    == ItemDisplayContext
                        .FIRST_PERSON_RIGHT_HAND
                ? 1.0F
                : -1.0F;

        float fishEnergy =
            Mth.clamp(
                Math.abs(smoothedFishVelocity)
                    / 0.040F,
                0.0F,
                1.0F
            );

        float missLoad =
            Mth.clamp(
                smoothedMiss / 0.22F,
                0.0F,
                1.0F
            );

        float zoneMotion =
            Mth.clamp(
                Math.abs(smoothedZoneVelocity)
                    / 0.045F,
                0.0F,
                1.0F
            );

        float load =
            Mth.clamp(
                fishEnergy * 0.65F
                    + missLoad * 0.35F,
                0.0F,
                1.0F
            );

        float phase =
            player.tickCount
                * (
                    0.70F
                        + zoneMotion * 0.65F
                );

        float controlPulse =
            Mth.sin(phase)
                * zoneMotion;

        float direction =
            smoothedZoneVelocity >= 0.0F
                ? 1.0F
                : -1.0F;

        poseStack.translate(
            handSign
                * controlPulse
                * 0.010F,
            -load * 0.026F
                + controlPulse * 0.008F,
            load * 0.060F
        );

        poseStack.mulPose(
            Axis.XP.rotationDegrees(
                -8.0F * load
                    + direction
                        * controlPulse
                        * 2.5F
            )
        );

        poseStack.mulPose(
            Axis.ZP.rotationDegrees(
                handSign
                    * (
                        -3.0F * load
                            + controlPulse
                                * 9.0F
                    )
            )
        );

        if (fishEnergy > 0.65F) {
            float tremor =
                Mth.sin(
                    player.tickCount
                        * 2.8F
                )
                    * (
                        fishEnergy
                            - 0.65F
                    );

            poseStack.translate(
                handSign
                    * tremor
                    * 0.004F,
                tremor * 0.003F,
                0.0F
            );
        }

        poseStack.translate(
            0.0F,
            smoothedProgress * 0.012F,
            0.0F
        );
    }

    public static ReelAction action() {
        return action;
    }

    public static boolean active() {
        return active;
    }

    private static void reset() {
        active = false;
        action = ReelAction.HOLD;

        smoothedProgress =
            Mth.lerp(
                0.35F,
                smoothedProgress,
                0.0F
            );

        smoothedFishVelocity =
            Mth.lerp(
                0.35F,
                smoothedFishVelocity,
                0.0F
            );

        smoothedZoneVelocity =
            Mth.lerp(
                0.35F,
                smoothedZoneVelocity,
                0.0F
            );

        smoothedMiss =
            Mth.lerp(
                0.35F,
                smoothedMiss,
                0.0F
            );
    }
}
