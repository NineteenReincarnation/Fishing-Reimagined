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
    private static float smoothedTension;
    private static float smoothedProgress;
    private static float smoothedFishVelocity;
    private static float smoothedLineVelocity;
    private static float smoothedDragSlip;

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

        smoothedTension =
            Mth.lerp(
                0.30F,
                smoothedTension,
                Mth.clamp(
                    access
                        .fishingReimagined$tensionRatio(),
                    0.0F,
                    1.35F
                )
            );

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
                0.34F,
                smoothedFishVelocity,
                access
                    .fishingReimagined$fishVelocity()
            );

        smoothedLineVelocity =
            Mth.lerp(
                0.34F,
                smoothedLineVelocity,
                access
                    .fishingReimagined$lineVelocity()
            );

        smoothedDragSlip =
            Mth.lerp(
                0.40F,
                smoothedDragSlip,
                access
                    .fishingReimagined$dragSlip()
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

        float tension =
            Mth.clamp(
                smoothedTension,
                0.0F,
                1.25F
            );

        float load =
            Mth.clamp(
                tension / 0.88F,
                0.0F,
                1.0F
            );

        float critical =
            Mth.clamp(
                (tension - 0.82F)
                    / 0.34F,
                0.0F,
                1.0F
            );

        float outwardPull =
            Mth.clamp(
                smoothedFishVelocity
                    / 0.11F,
                0.0F,
                1.0F
            );

        float actualReel =
            Mth.clamp(
                -smoothedLineVelocity
                    / 0.11F,
                0.0F,
                1.0F
            );

        float drag =
            Mth.clamp(
                smoothedDragSlip
                    / 0.16F,
                0.0F,
                1.0F
            );

        float phase =
            player.tickCount
                * (
                    0.58F
                        + actualReel * 0.72F
                );

        float pump =
            action == ReelAction.REEL_IN
                ? Mth.sin(phase)
                    * actualReel
                : 0.0F;

        float pullBack =
            load * 0.085F
                + outwardPull * 0.035F;

        poseStack.translate(
            handSign
                * (
                    -0.032F * load
                        + pump * 0.010F
                ),
            -0.045F * load
                + pump * 0.015F,
            pullBack
                - drag * 0.035F
        );

        poseStack.mulPose(
            Axis.XP.rotationDegrees(
                -13.0F * load
                    - 5.0F
                        * outwardPull
                    + 6.0F * drag
            )
        );

        poseStack.mulPose(
            Axis.ZP.rotationDegrees(
                handSign
                    * (
                        -5.5F * load
                            + pump * 14.0F
                    )
            )
        );

        if (drag > 0.02F) {
            float ratchet =
                Mth.sin(
                    player.tickCount
                        * (
                            2.2F
                                + drag * 2.8F
                        )
                )
                    * drag;

            poseStack.translate(
                handSign
                    * ratchet
                    * 0.006F,
                ratchet * 0.004F,
                -ratchet * 0.004F
            );

            poseStack.mulPose(
                Axis.YP.rotationDegrees(
                    handSign
                        * ratchet
                        * 2.4F
                )
            );
        }

        if (critical > 0.0F) {
            float tremor =
                Mth.sin(
                    player.tickCount
                        * 3.15F
                )
                    * critical;

            poseStack.translate(
                handSign
                    * tremor
                    * 0.006F,
                tremor * 0.005F,
                0.0F
            );
        }

        poseStack.translate(
            0.0F,
            smoothedProgress * 0.014F,
            0.0F
        );
    }

    public static float tension() {
        return smoothedTension;
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

        smoothedTension =
            Mth.lerp(
                0.35F,
                smoothedTension,
                0.0F
            );

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

        smoothedLineVelocity =
            Mth.lerp(
                0.35F,
                smoothedLineVelocity,
                0.0F
            );

        smoothedDragSlip =
            Mth.lerp(
                0.35F,
                smoothedDragSlip,
                0.0F
            );
    }
}
