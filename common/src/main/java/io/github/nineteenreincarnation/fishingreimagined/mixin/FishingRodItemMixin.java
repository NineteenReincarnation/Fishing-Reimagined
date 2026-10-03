package io.github.nineteenreincarnation.fishingreimagined.mixin;

import io.github.nineteenreincarnation.fishingreimagined.fight.ServerFishingMode;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin {
    @Inject(
        method = "use",
        at = @At("HEAD"),
        cancellable = true
    )
    private void fishingReimagined$handleFightInput(
        Level level,
        Player player,
        InteractionHand hand,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        FishingHook hook =
            player.fishing;

        if (!(hook
            instanceof FishingHookFightAccess access)) {
            return;
        }

        if (
            access
                .fishingReimagined$isCaughtHanging()
        ) {
            if (!level.isClientSide()) {
                access
                    .fishingReimagined$takeCaughtFish(
                        player
                    );
            }

            cir.setReturnValue(
                InteractionResult.SUCCESS
            );
            return;
        }

        if (
            access
                .fishingReimagined$isFightActive()
        ) {
            cir.setReturnValue(
                InteractionResult.SUCCESS
            );
            return;
        }

        if (
            !level.isClientSide()
                && ServerFishingMode
                    .enabledFor(player)
                && access
                    .fishingReimagined$isFishBiting()
        ) {
            access
                .fishingReimagined$startFight(
                    player,
                    hand
                );

            cir.setReturnValue(
                InteractionResult.SUCCESS
            );
        }
    }
}
