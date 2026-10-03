package io.github.nineteenreincarnation.fishingreimagined.mixin.client;

import io.github.nineteenreincarnation.fishingreimagined.client.ClientFishingInput;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void fishingReimagined$sampleFishingInput(CallbackInfo ci) {
        ClientFishingInput.tick((Minecraft) (Object) this);
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void fishingReimagined$replaceAttackWithReel(
        CallbackInfoReturnable<Boolean> cir
    ) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (ClientFishingInput.isFightActive(minecraft)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void fishingReimagined$stopMiningWhileReeling(
        boolean down,
        CallbackInfo ci
    ) {
        Minecraft minecraft =
            (Minecraft) (Object) this;

        if (
            ClientFishingInput.isFightActive(minecraft)
                && !ClientFishingInput.isCatchHanging(minecraft)
        ) {
            ci.cancel();
        }
    }

}
