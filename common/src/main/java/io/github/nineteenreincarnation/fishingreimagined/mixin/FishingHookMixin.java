package io.github.nineteenreincarnation.fishingreimagined.mixin;

import io.github.nineteenreincarnation.fishingreimagined.fight.FightPhase;
import io.github.nineteenreincarnation.fishingreimagined.fight.FightSnapshot;
import io.github.nineteenreincarnation.fishingreimagined.fight.FishFightMode;
import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import io.github.nineteenreincarnation.fishingreimagined.fight.ServerFishingInput;
import io.github.nineteenreincarnation.fishingreimagined.fight.ServerFishingMode;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import io.github.nineteenreincarnation.fishingreimagined.hook.HookedFish;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
public abstract class FishingHookMixin
    implements FishingHookFightAccess {

    @Unique
    private static final EntityDataAccessor<Boolean>
        FISHING_REIMAGINED_FIGHT_ACTIVE =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.BOOLEAN
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_TENSION =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_PROGRESS =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_STAMINA =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_BREAK_RISK =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_FISH_VELOCITY =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_LINE_VELOCITY =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_DRAG_SLIP =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_DISTANCE =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_FISH_TRACK_POSITION =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_CATCH_ZONE_POSITION =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Float>
        FISHING_REIMAGINED_CATCH_ZONE_WIDTH =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.FLOAT
            );

    @Unique
    private static final EntityDataAccessor<Integer>
        FISHING_REIMAGINED_FISH_STATE =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.INT
            );

    @Unique
    private static final EntityDataAccessor<Integer>
        FISHING_REIMAGINED_FISH_KIND =
            SynchedEntityData.defineId(
                FishingHook.class,
                EntityDataSerializers.INT
            );

    @Shadow
    private boolean biting;

    @Shadow
    private int nibble;

    @Unique
    private static final int FISHING_REIMAGINED_LANDING_TICKS = 10;

    @Unique
    private HookedFish fishingReimagined$hookedFish;

    @Unique
    private InteractionHand fishingReimagined$rodHand =
        InteractionHand.MAIN_HAND;

    @Unique
    private int fishingReimagined$landingTicks;

    @Unique
    private Vec3 fishingReimagined$landingStart;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void fishingReimagined$defineFightData(
        SynchedEntityData.Builder builder,
        CallbackInfo ci
    ) {
        builder.define(
            FISHING_REIMAGINED_FIGHT_ACTIVE,
            false
        );
        builder.define(FISHING_REIMAGINED_TENSION, 0.0F);
        builder.define(FISHING_REIMAGINED_PROGRESS, 0.0F);
        builder.define(FISHING_REIMAGINED_STAMINA, 1.0F);
        builder.define(FISHING_REIMAGINED_BREAK_RISK, 0.0F);
        builder.define(FISHING_REIMAGINED_FISH_VELOCITY, 0.0F);
        builder.define(FISHING_REIMAGINED_LINE_VELOCITY, 0.0F);
        builder.define(FISHING_REIMAGINED_DRAG_SLIP, 0.0F);
        builder.define(FISHING_REIMAGINED_DISTANCE, 0.0F);
        builder.define(FISHING_REIMAGINED_FISH_TRACK_POSITION, 0.5F);
        builder.define(FISHING_REIMAGINED_CATCH_ZONE_POSITION, 0.5F);
        builder.define(FISHING_REIMAGINED_CATCH_ZONE_WIDTH, 0.30F);
        builder.define(FISHING_REIMAGINED_FISH_STATE, 0);
        builder.define(FISHING_REIMAGINED_FISH_KIND, 0);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void fishingReimagined$tickFight(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;

        if (hook.level().isClientSide()) {
            return;
        }

        Player owner = hook.getPlayerOwner();
        if (!(hook.level() instanceof ServerLevel serverLevel)
            || owner == null) {
            if (fishingReimagined$hookedFish != null) {
                fishingReimagined$finish(hook, owner);
            }
            return;
        }

        if (fishingReimagined$hookedFish == null) {
            if (
                !biting
                    || !ServerFishingMode.enabledFor(owner)
            ) {
                return;
            }

            InteractionHand hand =
                owner.getMainHandItem().is(Items.FISHING_ROD)
                    ? InteractionHand.MAIN_HAND
                    : InteractionHand.OFF_HAND;

            fishingReimagined$startFight(
                owner,
                hand
            );

            if (fishingReimagined$hookedFish == null) {
                return;
            }

            owner.sendOverlayMessage(
                Component.translatable(
                    "message.fishing_reimagined.fight_started"
                )
            );
        }

        nibble = Math.max(nibble, 2);

        ReelAction action =
            ServerFishingInput.actionFor(owner);
        FightSnapshot snapshot =
            fishingReimagined$hookedFish.tick(action);

        hook.getEntityData().set(
            FISHING_REIMAGINED_TENSION,
            (float) Math.min(
                2.0,
                snapshot.tensionRatio()
            )
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_PROGRESS,
            (float) snapshot.landingProgress()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_STAMINA,
            (float) snapshot.staminaRatio()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_BREAK_RISK,
            (float) snapshot.breakRisk()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_VELOCITY,
            (float) snapshot.fishVelocity()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_LINE_VELOCITY,
            (float) snapshot.lineVelocity()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_DRAG_SLIP,
            (float) snapshot.dragSlip()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_DISTANCE,
            (float) snapshot.distance()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_TRACK_POSITION,
            (float) snapshot.fishTrackPosition()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_CATCH_ZONE_POSITION,
            (float) snapshot.catchZonePosition()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_CATCH_ZONE_WIDTH,
            (float) snapshot.catchZoneWidth()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_STATE,
            fishingReimagined$fishState(snapshot)
        );

        if (snapshot.phase() == FightPhase.CAUGHT) {
            fishingReimagined$handleLanding(
                serverLevel,
                owner,
                hook,
                snapshot
            );
            return;
        }

        fishingReimagined$moveHookAnchor(
            hook,
            owner,
            snapshot
        );

        if (snapshot.phase().isTerminal()) {
            if (
                snapshot.phase() == FightPhase.LINE_BROKEN
            ) {
                serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    hook.getX(),
                    hook.getY() + 0.20,
                    hook.getZ(),
                    12,
                    0.18,
                    0.12,
                    0.18,
                    0.18
                );
                hook.playSound(
                    SoundEvents.TRIPWIRE_DETACH,
                    1.0F,
                    0.62F
                );
                owner.sendOverlayMessage(
                    Component.translatable(
                        "message.fishing_reimagined.line_broken"
                    )
                );
            } else if (
                snapshot.phase() == FightPhase.ESCAPED
            ) {
                hook.playSound(
                    SoundEvents.FISH_SWIM,
                    0.55F,
                    0.72F
                );
                owner.sendOverlayMessage(
                    Component.translatable(
                        "message.fishing_reimagined.fish_escaped"
                    )
                );
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
        return hook.getEntityData().get(
            FISHING_REIMAGINED_FIGHT_ACTIVE
        );
    }

    @Override
    public boolean fishingReimagined$isFishBiting() {
        return biting;
    }

    @Override
    public boolean fishingReimagined$startFight(
        Player player,
        InteractionHand hand
    ) {
        FishingHook hook = (FishingHook) (Object) this;
        if (hook.level().isClientSide()
            || fishingReimagined$hookedFish != null
            || !biting
            || hook.getPlayerOwner() != player) {
            return false;
        }

        fishingReimagined$hookedFish =
            HookedFish.create(
                player,
                hook,
                hook.level().getRandom()
            );
        fishingReimagined$rodHand = hand;
        fishingReimagined$landingTicks = 0;
        fishingReimagined$landingStart = null;

        hook.getEntityData().set(
            FISHING_REIMAGINED_FIGHT_ACTIVE,
            true
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_KIND,
            fishingReimagined$hookedFish
                .kind()
                .networkId()
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_TENSION,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_PROGRESS,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_STAMINA,
            1.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_BREAK_RISK,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_VELOCITY,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_LINE_VELOCITY,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_DRAG_SLIP,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_DISTANCE,
            (float) hook.position()
                .distanceTo(player.position())
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_TRACK_POSITION,
            0.5F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_CATCH_ZONE_POSITION,
            0.5F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_CATCH_ZONE_WIDTH,
            0.30F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_STATE,
            0
        );
        return true;
    }

    @Override
    public float fishingReimagined$tensionRatio() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_TENSION
        );
    }

    @Override
    public float fishingReimagined$catchProgress() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_PROGRESS
        );
    }

    @Override
    public float fishingReimagined$staminaRatio() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_STAMINA
        );
    }

    @Override
    public float fishingReimagined$breakRisk() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_BREAK_RISK
        );
    }

    @Override
    public float fishingReimagined$fishVelocity() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_FISH_VELOCITY
        );
    }

    @Override
    public float fishingReimagined$lineVelocity() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_LINE_VELOCITY
        );
    }

    @Override
    public float fishingReimagined$dragSlip() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_DRAG_SLIP
        );
    }

    @Override
    public float fishingReimagined$distance() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_DISTANCE
        );
    }

    @Override
    public float fishingReimagined$fishTrackPosition() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_FISH_TRACK_POSITION
        );
    }

    @Override
    public float fishingReimagined$catchZonePosition() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_CATCH_ZONE_POSITION
        );
    }

    @Override
    public float fishingReimagined$catchZoneWidth() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_CATCH_ZONE_WIDTH
        );
    }

    @Override
    public int fishingReimagined$fishState() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_FISH_STATE
        );
    }

    @Override
    public int fishingReimagined$fishKindId() {
        FishingHook hook = (FishingHook) (Object) this;
        return hook.getEntityData().get(
            FISHING_REIMAGINED_FISH_KIND
        );
    }

    @Unique
    private int fishingReimagined$fishState(
        FightSnapshot snapshot
    ) {
        if (snapshot.phase() == FightPhase.CAUGHT) {
            return 3;
        }
        if (snapshot.phase() == FightPhase.TIRED) {
            return 2;
        }

        FishFightMode mode =
            snapshot.fishIntent().mode();

        return switch (mode) {
            case BURST -> 1;
            case TIRED -> 2;
            case PULLING -> 4;
            case RECOVERING -> 5;
            case PROBING -> 0;
        };
    }

    @Unique
    private void fishingReimagined$handleLanding(
        ServerLevel level,
        Player owner,
        FishingHook hook,
        FightSnapshot snapshot
    ) {
        if (fishingReimagined$landingStart == null) {
            fishingReimagined$moveHookAnchor(
                hook,
                owner,
                snapshot
            );
            fishingReimagined$landingStart =
                hook.position();

            level.sendParticles(
                ParticleTypes.SPLASH,
                hook.getX(),
                hook.getY() + 0.08,
                hook.getZ(),
                7,
                0.22,
                0.06,
                0.22,
                0.05
            );
        }

        fishingReimagined$landingTicks++;

        Vec3 fromPlayer =
            fishingReimagined$landingStart
                .subtract(owner.position());
        Vec3 horizontal =
            new Vec3(
                fromPlayer.x,
                0.0,
                fromPlayer.z
            );

        if (horizontal.lengthSqr() < 1.0E-6) {
            horizontal =
                new Vec3(0.0, 0.0, 1.0);
        } else {
            horizontal = horizontal.normalize();
        }

        Vec3 target =
            owner.position()
                .add(horizontal.scale(0.90))
                .add(0.0, 0.32, 0.0);

        double t =
            Math.min(
                1.0,
                fishingReimagined$landingTicks
                    / (double) FISHING_REIMAGINED_LANDING_TICKS
            );
        double eased =
            t * t * (3.0 - 2.0 * t);
        Vec3 base =
            fishingReimagined$landingStart
                .lerp(target, eased);
        double arc =
            Math.sin(Math.PI * t) * 0.82;

        hook.setPos(
            base.x,
            base.y + arc,
            base.z
        );
        hook.setDeltaMovement(Vec3.ZERO);

        if (fishingReimagined$landingTicks
            < FISHING_REIMAGINED_LANDING_TICKS) {
            return;
        }

        fishingReimagined$hookedFish.materialize(
            level,
            owner,
            hook
        );
        hook.playSound(
            SoundEvents.FISHING_BOBBER_RETRIEVE,
            0.62F,
            1.04F
        );

        fishingReimagined$damageRod(owner);
        fishingReimagined$finish(hook, owner);
    }

    @Unique
    private void fishingReimagined$moveHookAnchor(
        FishingHook hook,
        Player owner,
        FightSnapshot snapshot
    ) {
        Vec3 desired =
            fishingReimagined$hookedFish.desiredPosition(
                owner,
                snapshot
            );
        BlockPos desiredPos =
            BlockPos.containing(
                desired.x,
                desired.y,
                desired.z
            );

        if (hook.level()
            .getFluidState(desiredPos)
            .is(FluidTags.WATER)) {
            hook.setPos(
                desired.x,
                desired.y,
                desired.z
            );
            hook.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Unique
    private void fishingReimagined$damageRod(
        Player owner
    ) {
        ItemStack rod =
            owner.getItemInHand(
                fishingReimagined$rodHand
            );
        if (rod.is(Items.FISHING_ROD)) {
            rod.hurtAndBreak(
                1,
                owner,
                fishingReimagined$rodHand
                    .asEquipmentSlot()
            );
        }
    }

    @Unique
    private void fishingReimagined$finish(
        FishingHook hook,
        Player owner
    ) {
        if (owner != null) {
            ServerFishingInput.clear(owner);
        }

        fishingReimagined$hookedFish = null;
        fishingReimagined$landingTicks = 0;
        fishingReimagined$landingStart = null;
        hook.getEntityData().set(
            FISHING_REIMAGINED_FIGHT_ACTIVE,
            false
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_TENSION,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_PROGRESS,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_STAMINA,
            1.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_BREAK_RISK,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_VELOCITY,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_LINE_VELOCITY,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_DRAG_SLIP,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_DISTANCE,
            0.0F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_TRACK_POSITION,
            0.5F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_CATCH_ZONE_POSITION,
            0.5F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_CATCH_ZONE_WIDTH,
            0.30F
        );
        hook.getEntityData().set(
            FISHING_REIMAGINED_FISH_STATE,
            0
        );
        hook.discard();
    }
}
