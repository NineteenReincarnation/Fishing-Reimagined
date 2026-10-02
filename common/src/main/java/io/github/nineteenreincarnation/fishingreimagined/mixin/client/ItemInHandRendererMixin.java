package io.github.nineteenreincarnation.fishingreimagined.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nineteenreincarnation.fishingreimagined.client.ClientFishingPose;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private void fishingReimagined$applyRodFightPose(
        LivingEntity entity,
        ItemStack stack,
        ItemDisplayContext context,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        CallbackInfo ci
    ) {
        ClientFishingPose.applyFirstPersonRodTransform(
            entity,
            stack,
            context,
            poseStack
        );
    }
}
