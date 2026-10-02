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
    private static ReelAction action = ReelAction.HOLD;
    private static boolean active;

    private ClientFishingPose() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null) {
            reset();
            return;
        }

        FishingHook hook = minecraft.player.fishing;
        if (!(hook instanceof FishingHookFightAccess access)
            || !access.fishingReimagined$isFightActive()) {
            reset();
            return;
        }

        active = true;
        action = ClientFishingInput.currentAction();
        smoothedTension = Mth.lerp(
            0.28F,
            smoothedTension,
            Mth.clamp(access.fishingReimagined$tensionRatio(), 0.0F, 1.25F)
        );
        smoothedProgress = Mth.lerp(
            0.18F,
            smoothedProgress,
            Mth.clamp(access.fishingReimagined$catchProgress(), 0.0F, 1.0F)
        );
    }

    public static void applyFirstPersonRodTransform(
        LivingEntity entity,
        ItemStack stack,
        ItemDisplayContext context,
        PoseStack poseStack
    ) {
        if (!active
            || !(entity instanceof LocalPlayer player)
            || !stack.is(Items.FISHING_ROD)
            || (context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                && context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND)) {
            return;
        }

        float handSign =
            context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? 1.0F : -1.0F;

        float tension = Mth.clamp(smoothedTension, 0.0F, 1.15F);
        float load = Mth.clamp(tension / 0.85F, 0.0F, 1.0F);
        float critical = Mth.clamp((tension - 0.85F) / 0.30F, 0.0F, 1.0F);
        float phase = player.tickCount * 0.72F;
        float reelPulse = action == ReelAction.REEL_IN
            ? Mth.sin(phase) * 0.035F
            : 0.0F;
        float release = action == ReelAction.PAY_OUT ? 1.0F : 0.0F;

        poseStack.translate(
            handSign * (-0.025F * load),
            -0.025F * load + reelPulse * 0.35F,
            0.075F * load - 0.035F * release
        );

        poseStack.mulPose(
            Axis.XP.rotationDegrees(
                -8.0F * load + 5.0F * release
            )
        );
        poseStack.mulPose(
            Axis.ZP.rotationDegrees(
                handSign * (
                    -4.0F * load
                    + reelPulse * 95.0F
                )
            )
        );

        if (critical > 0.0F) {
            float tremor = Mth.sin(player.tickCount * 2.35F) * critical;
            poseStack.translate(
                handSign * tremor * 0.004F,
                tremor * 0.003F,
                0.0F
            );
            poseStack.mulPose(
                Axis.YP.rotationDegrees(
                    handSign * tremor * 1.8F
                )
            );
        }

        float landingLift = smoothedProgress * 0.018F;
        poseStack.translate(0.0F, landingLift, 0.0F);
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
        smoothedTension = Mth.lerp(0.35F, smoothedTension, 0.0F);
        smoothedProgress = Mth.lerp(0.35F, smoothedProgress, 0.0F);
    }
}
