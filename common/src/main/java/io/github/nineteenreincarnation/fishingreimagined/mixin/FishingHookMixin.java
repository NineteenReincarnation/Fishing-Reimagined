package io.github.nineteenreincarnation.fishingreimagined.mixin;

import io.github.nineteenreincarnation.fishingreimagined.fight.FightPhase;
import io.github.nineteenreincarnation.fishingreimagined.fight.FightSnapshot;
import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import io.github.nineteenreincarnation.fishingreimagined.fight.ServerFishingInput;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import io.github.nineteenreincarnation.fishingreimagined.hook.HookedFish;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements FishingHookFightAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> FISHING_REIMAGINED_FIGHT_ACTIVE =
        SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Float> FISHING_REIMAGINED_TENSION =
        SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.FLOAT);

    @Unique
    private static final EntityDataAccessor<Float> FISHING_REIMAGINED_PROGRESS =
        SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.FLOAT);

    @Unique
    private static final EntityDataAccessor<Float> FISHING_REIMAGINED_STAMINA =
        SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.FLOAT);

    @Unique
    private static final EntityDataAccessor<Integer> FISHING_REIMAGINED_FISH_STATE =
        SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Integer> FISHING_REIMAGINED_FISH_KIND =
        SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);

    @Shadow
    private boolean biting;

    @Shadow
    private int nibble;

    @Unique
    private HookedFish fishingReimagined$hookedFish;

    @Unique
    private InteractionHand fishingReimagined$rodHand = InteractionHand.MAIN_HAND;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void fishingReimagined$defineFightData(
        SynchedEntityData.Builder builder,
        CallbackInfo ci
    ) {
        builder.define(FISHING_REIMAGINED_FIGHT_ACTIVE, false);
        builder.define(FISHING_REIMAGINED_TENSION, 0.0F);
        builder.define(FISHING_REIMAGINED_PROGRESS, 0.0F);
        builder.define(FISHING_REIMAGINED_STAMINA, 1.0F);
        builder.define(FISHING_REIMAGINED_FISH_STATE, 0);
        builder.define(FISHING_REIMAGINED_FISH_KIND, 0);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void fishingReimagined$tickFight(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        if (hook.level().isClientSide() || fishingReimagined$hookedFish == null) {
            return;
        }

        Player owner = hook.getPlayerOwner();
        if (!(hook.level() instanceof ServerLevel serverLevel) || owner == null) {
            fishingReimagined$finish(hook, owner);
            return;
        }

        nibble = Math.max(nibble, 2);

        ReelAction action = ServerFishingInput.actionFor(owner);
        FightSnapshot snapshot = fishingReimagined$hookedFish.tick(action);

        hook.getEntityData().set(
            FISHING_REIMAGINED_TENSION,
            (float) Math.min(2.0, snapshot.tensionRatio())
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_PROGRESS,
            (float) fishingReimagined$hookedFish.catchProgress(snapshot)
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_STAMINA,
            (float) snapshot.staminaRatio()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_STATE,
            fishingReimagined$fishState(snapshot)
        );

        fishingReimagined$moveHookAnchor(hook, owner, snapshot);

        if (snapshot.phase().isTerminal()) {
            if (snapshot.phase() == FightPhase.CAUGHT) {
                fishingReimagined$hookedFish.materialize(serverLevel, owner, hook);
            }

            fishingReimagined$damageRod(owner);
            fishingReimagined$finish(hook, owner);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void fishingReimagined$cleanupOnRemove(
        Entity.RemovalReason reason,
        CallbackInfo ci
    ) {
        FishingHook hook = (FishingHook) (Object) this;
        Player owner = hook.getPlayerOwner();
        if (owner != null) {
            ServerFishingInput.clear(owner);
        }
    }

    @Override
    public boolean fishingReimagined$isFightActive() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(FISHING_REIMAGINED_FIGHT_ACTIVE);
    }

    @Override
    public boolean fishingReimagined$isFishBiting() {
        return biting;
    }

    @Override
    public boolean fishingReimagined$startFight(Player player, InteractionHand hand) {
        FishingHook hook = (FishingHook) (Object) this;
        if (hook.level().isClientSide()
            || fishingReimagined$hookedFish != null
            || !biting
            || hook.getPlayerOwner() != player) {
            return false;
        }

        fishingReimagined$hookedFish = HookedFish.create(
            player,
            hook,
            hook.level().getRandom()
        );
        fishingReimagined$rodHand = hand;

        hook.getEntityData().set(FISHING_REIMAGINED_FIGHT_ACTIVE, true);
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_KIND,
            fishingReimagined$hookedFish.kind().networkId()
        );
        hook.getEntityData().set(FISHING_REIMAGINED_TENSION, 0.0F);
        hook.getEntityData().set(FISHING_REIMAGINED_PROGRESS, 0.0F);
        hook.getEntityData().set(FISHING_REIMAGINED_STAMINA, 1.0F);
        hook.getEntityData().set(FISHING_REIMAGINED_FISH_STATE, 0);
        return true;
    }

    @Override
    public float fishingReimagined$tensionRatio() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(FISHING_REIMAGINED_TENSION);
    }

    @Override
    public float fishingReimagined$catchProgress() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(FISHING_REIMAGINED_PROGRESS);
    }

    @Override
    public float fishingReimagined$staminaRatio() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(FISHING_REIMAGINED_STAMINA);
    }

    @Override
    public int fishingReimagined$fishState() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(FISHING_REIMAGINED_FISH_STATE);
    }

    @Override
    public int fishingReimagined$fishKindId() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(FISHING_REIMAGINED_FISH_KIND);
    }

    @Unique
    private int fishingReimagined$fishState(FightSnapshot snapshot) {
        if (snapshot.phase() == FightPhase.TIRED) {
            return 2;
        }
        return snapshot.fishIntent().burst() ? 1 : 0;
    }

    @Unique
    private void fishingReimagined$moveHookAnchor(
        FishingHook hook,
        Player owner,
        FightSnapshot snapshot
    ) {
        Vec3 desired = fishingReimagined$hookedFish.desiredPosition(owner, snapshot);
        BlockPos desiredPos = BlockPos.containing(desired.x, desired.y, desired.z);

        if (hook.level().getFluidState(desiredPos).is(FluidTags.WATER)) {
            hook.setPos(desired.x, desired.y, desired.z);
            hook.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Unique
    private void fishingReimagined$damageRod(Player owner) {
        ItemStack rod = owner.getItemInHand(fishingReimagined$rodHand);
        if (rod.is(Items.FISHING_ROD)) {
            rod.hurtAndBreak(1, owner, fishingReimagined$rodHand.asEquipmentSlot());
        }
    }

    @Unique
    private void fishingReimagined$finish(FishingHook hook, Player owner) {
        if (owner != null) {
            ServerFishingInput.clear(owner);
        }

        fishingReimagined$hookedFish = null;
        hook.getEntityData().set(FISHING_REIMAGINED_FIGHT_ACTIVE, false);
        hook.getEntityData().set(FISHING_REIMAGINED_TENSION, 0.0F);
        hook.getEntityData().set(FISHING_REIMAGINED_PROGRESS, 0.0F);
        hook.getEntityData().set(FISHING_REIMAGINED_STAMINA, 1.0F);
        hook.getEntityData().set(FISHING_REIMAGINED_FISH_STATE, 0);
        hook.discard();
    }
}
