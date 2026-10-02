package io.github.nineteenreincarnation.fishingreimagined.mixin.client;

import io.github.nineteenreincarnation.fishingreimagined.client.FishingHookRenderStateAccess;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FishingHookRenderState.class)
public abstract class FishingHookRenderStateMixin
    implements FishingHookRenderStateAccess {

    @Unique
    private boolean fishingReimagined$fightActive;

    @Unique
    private float fishingReimagined$tensionRatio;

    @Unique
    private int fishingReimagined$hookId;

    @Override
    public void fishingReimagined$setFightActive(boolean active) {
        fishingReimagined$fightActive = active;
    }

    @Override
    public boolean fishingReimagined$isFightActive() {
        return fishingReimagined$fightActive;
    }

    @Override
    public void fishingReimagined$setTensionRatio(float tensionRatio) {
        fishingReimagined$tensionRatio = tensionRatio;
    }

    @Override
    public float fishingReimagined$tensionRatio() {
        return fishingReimagined$tensionRatio;
    }

    @Override
    public void fishingReimagined$setHookId(int hookId) {
        fishingReimagined$hookId = hookId;
    }

    @Override
    public int fishingReimagined$hookId() {
        return fishingReimagined$hookId;
    }
}
