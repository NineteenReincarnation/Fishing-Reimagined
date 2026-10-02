package io.github.nineteenreincarnation.fishingreimagined.mixin.client;

import io.github.nineteenreincarnation.fishingreimagined.client.FishingHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fishingReimagined$renderFightHud(
        GuiGraphicsExtractor graphics,
        DeltaTracker deltaTracker,
        CallbackInfo ci
    ) {
        Hud hud = (Hud) (Object) this;
        if (!hud.isHidden()) {
            FishingHud.render(graphics, Minecraft.getInstance());
        }
    }
}
