package io.github.nineteenreincarnation.fishingreimagined.client;

import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import io.github.nineteenreincarnation.fishingreimagined.hook.HookedFishKind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public final class ClientHookedFishVisuals {
    private static final Map<Integer, VisualFish>
        VISUALS = new HashMap<>();

    private static ClientLevel lastLevel;

    private ClientHookedFishVisuals() {
    }

    public static void tick(
        Minecraft minecraft
    ) {
        ClientLevel level = minecraft.level;

        if (level == null) {
            clear();
            return;
        }

        if (lastLevel != level) {
            clear();
            lastLevel = level;
        }

        List<FishingHook> hooks =
            new ArrayList<>();

        for (
            Entity entity
                : level.entitiesForRendering()
        ) {
            if (entity
                instanceof FishingHook hook) {
                hooks.add(hook);
            }
        }

        Set<Integer> activeHookIds =
            new HashSet<>();

        for (FishingHook hook : hooks) {
            if (!(hook
                instanceof FishingHookFightAccess access)
                || !access
                    .fishingReimagined$isFightActive()) {
                continue;
            }

            activeHookIds.add(
                hook.getId()
            );

            HookedFishKind kind =
                HookedFishKind.byNetworkId(
                    access
                        .fishingReimagined$fishKindId()
                );

            VisualFish visual =
                VISUALS.get(hook.getId());

            if (visual == null
                || visual.kind != kind
                || visual.fish.isRemoved()) {
                if (visual != null) {
                    removeVisual(
                        level,
                        visual
                    );
                }

                visual =
                    createVisual(
                        level,
                        hook,
                        kind
                    );

                VISUALS.put(
                    hook.getId(),
                    visual
                );
            }

            updateVisual(
                level,
                hook,
                access,
                visual
            );
        }

        List<Integer> stale =
            new ArrayList<>();

        for (
            Map.Entry<Integer, VisualFish> entry
                : VISUALS.entrySet()
        ) {
            if (!activeHookIds.contains(
                entry.getKey()
            )) {
                removeVisual(
                    level,
                    entry.getValue()
                );
                stale.add(
                    entry.getKey()
                );
            }
        }

        stale.forEach(VISUALS::remove);
    }

    private static VisualFish createVisual(
        ClientLevel level,
        FishingHook hook,
        HookedFishKind kind
    ) {
        AbstractFish fish =
            switch (kind) {
                case COD ->
                    new Cod(
                        EntityTypes.COD,
                        level
                    );
                case SALMON ->
                    new Salmon(
                        EntityTypes.SALMON,
                        level
                    );
                case PUFFERFISH ->
                    new Pufferfish(
                        EntityTypes.PUFFERFISH,
                        level
                    );
                case TROPICAL_FISH ->
                    new TropicalFish(
                        EntityTypes.TROPICAL_FISH,
                        level
                    );
            };

        fish.setId(
            -1_000_000_000
                + hook.getId()
        );
        fish.setNoAi(true);
        fish.setNoGravity(true);
        fish.setInvulnerable(true);
        fish.setSilent(true);

        Vec3 start =
            hook.position()
                .add(
                    0.0,
                    -0.72,
                    0.0
                );

        fish.snapTo(
            start.x,
            start.y,
            start.z,
            hook.getYRot(),
            0.0F
        );
        fish.setOldPosAndRot();
        level.addEntity(fish);

        return new VisualFish(
            kind,
            fish,
            start,
            hook.position()
        );
    }

    private static void updateVisual(
        ClientLevel level,
        FishingHook hook,
        FishingHookFightAccess access,
        VisualFish visual
    ) {
        int state =
            access.fishingReimagined$fishState();

        Player owner =
            hook.getPlayerOwner();

        Vec3 radial =
            owner == null
                ? new Vec3(
                    1.0,
                    0.0,
                    0.0
                )
                : hook.position()
                    .subtract(
                        owner.position()
                    );

        Vec3 horizontal =
            new Vec3(
                radial.x,
                0.0,
                radial.z
            );

        if (horizontal.lengthSqr()
            < 1.0E-6) {
            horizontal =
                new Vec3(
                    1.0,
                    0.0,
                    0.0
                );
        } else {
            horizontal =
                horizontal.normalize();
        }

        Vec3 lateral =
            new Vec3(
                -horizontal.z,
                0.0,
                horizontal.x
            );

        Vec3 hookVelocity =
            hook.position()
                .subtract(
                    visual.lastHookPosition
                );

        double speciesScale =
            switch (visual.kind) {
                case COD -> 1.00;
                case SALMON -> 1.18;
                case PUFFERFISH -> 0.72;
                case TROPICAL_FISH -> 1.32;
            };

        double time =
            hook.tickCount
                + hook.getId() * 0.37;

        double sideAmplitude;
        double sideSpeed;
        double baseDepth;
        double verticalAmplitude;
        double radialOffset;
        float pitchAmplitude;
        double trailFactor;

        switch (state) {
            case 3 -> {
                sideAmplitude = 0.05;
                sideSpeed = 0.30;
                baseDepth = -0.10;
                verticalAmplitude = 0.02;
                radialOffset = 0.02;
                pitchAmplitude = 5.0F;
                trailFactor = 0.0;
            }
            case 1 -> {
                sideAmplitude =
                    1.25 * speciesScale;
                sideSpeed = 1.18;
                baseDepth = -0.34;
                verticalAmplitude =
                    0.28 * speciesScale;
                radialOffset =
                    0.78 * speciesScale;
                pitchAmplitude = 24.0F;
                trailFactor = 1.8;
            }
            case 2 -> {
                sideAmplitude =
                    0.18 * speciesScale;
                sideSpeed = 0.16;
                baseDepth = -0.96;
                verticalAmplitude = 0.03;
                radialOffset = 0.10;
                pitchAmplitude = 3.0F;
                trailFactor = 0.15;
            }
            case 4 -> {
                sideAmplitude =
                    0.88 * speciesScale;
                sideSpeed = 0.72;
                baseDepth = -0.52;
                verticalAmplitude =
                    0.16 * speciesScale;
                radialOffset =
                    0.56 * speciesScale;
                pitchAmplitude = 17.0F;
                trailFactor = 1.10;
            }
            case 5 -> {
                sideAmplitude =
                    0.30 * speciesScale;
                sideSpeed = 0.28;
                baseDepth = -0.82;
                verticalAmplitude = 0.05;
                radialOffset = 0.16;
                pitchAmplitude = 5.0F;
                trailFactor = 0.30;
            }
            default -> {
                sideAmplitude =
                    0.58 * speciesScale;
                sideSpeed = 0.52;
                baseDepth = -0.66;
                verticalAmplitude =
                    0.11 * speciesScale;
                radialOffset =
                    0.34 * speciesScale;
                pitchAmplitude = 11.0F;
                trailFactor = 0.55;
            }
        }

        double physicalOutwardSpeed =
            Mth.clamp(
                access
                    .fishingReimagined$fishVelocity()
                    / 0.14F,
                -1.0F,
                1.0F
            );

        double landingProgress =
            Mth.clamp(
                access
                    .fishingReimagined$catchProgress(),
                0.0F,
                1.0F
            );

        double depth =
            baseDepth
                + landingProgress * 0.24;

        double sideOffset =
            Math.sin(
                time * sideSpeed
            )
                * sideAmplitude;

        double verticalOffset =
            Math.sin(
                time
                    * sideSpeed
                    * 0.78
            )
                * verticalAmplitude;

        double stateSurge =
            state == 1
                ? 0.42
                : state == 4
                    ? 0.20
                    : 0.0;

        double surge =
            Math.sin(
                time
                    * (
                        state == 1
                            ? 0.63
                            : 0.44
                    )
            )
                * stateSurge
                * speciesScale
                + physicalOutwardSpeed
                    * (
                        state == 1
                            ? 0.46
                            : 0.22
                    );

        Vec3 target =
            hook.position()
                .add(
                    horizontal.scale(
                        radialOffset + surge
                    )
                )
                .add(
                    lateral.scale(
                        sideOffset
                    )
                )
                .add(
                    0.0,
                    depth
                        + verticalOffset,
                    0.0
                );

        Vec3 movement =
            target.subtract(
                visual.lastPosition
            );

        Vec3 facingMovement =
            new Vec3(
                movement.x
                    + hookVelocity.x
                        * trailFactor,
                0.0,
                movement.z
                    + hookVelocity.z
                        * trailFactor
            );

        if (facingMovement.lengthSqr()
            > 1.0E-5) {
            float yaw =
                (float) (
                    Mth.atan2(
                        facingMovement.z,
                        facingMovement.x
                    )
                        * 180.0
                        / Math.PI
                )
                    - 90.0F;

            visual.fish.setYRot(yaw);
            visual.fish.yBodyRot = yaw;
            visual.fish.yHeadRot = yaw;
        }

        visual.fish.setXRot(
            (float) Math.sin(
                time
                    * sideSpeed
                    * 1.35
            )
                * pitchAmplitude
        );

        visual.fish.setDeltaMovement(
            movement
        );

        visual.fish.setPos(
            target.x,
            target.y,
            target.z
        );

        visual.lastPosition = target;
        visual.lastHookPosition =
            hook.position();

        if (visual.lastState != state) {
            emitStateTransitionParticles(
                level,
                hook,
                target,
                state
            );
            visual.lastState = state;
        }

        emitStateParticles(
            level,
            hook,
            target,
            state,
            lateral,
            movement,
            time
        );
    }

    private static void emitStateTransitionParticles(
        ClientLevel level,
        FishingHook hook,
        Vec3 position,
        int state
    ) {
        if (state != 1) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            double spread =
                (i - 4) * 0.065;

            level.addParticle(
                ParticleTypes.SPLASH,
                position.x + spread,
                hook.getY() + 0.10,
                position.z - spread,
                spread * 0.14,
                0.08
                    + Math.abs(spread)
                        * 0.16,
                -spread * 0.14
            );
        }

        for (int i = 0; i < 10; i++) {
            level.addParticle(
                ParticleTypes.BUBBLE,
                position.x,
                position.y + 0.12,
                position.z,
                (i - 4.5) * 0.014,
                0.035,
                (4.5 - i) * 0.014
            );
        }
    }

    private static void emitStateParticles(
        ClientLevel level,
        FishingHook hook,
        Vec3 position,
        int state,
        Vec3 lateral,
        Vec3 movement,
        double time
    ) {
        if (state == 3) {
            return;
        }

        if (state == 1) {
            double speed =
                Math.sqrt(
                    movement.x * movement.x
                        + movement.z
                            * movement.z
                );

            if (hook.tickCount % 2 == 0) {
                double drift =
                    Math.sin(
                        time * 0.8
                    ) * 0.08;

                level.addParticle(
                    ParticleTypes.BUBBLE,
                    position.x
                        - movement.x * 2.4,
                    position.y + 0.10,
                    position.z
                        - movement.z * 2.4,
                    lateral.x * drift,
                    0.03,
                    lateral.z * drift
                );
            }

            if (
                hook.tickCount % 2 == 0
                    && speed > 0.10
            ) {
                level.addParticle(
                    ParticleTypes.SPLASH,
                    position.x,
                    hook.getY() + 0.08,
                    position.z,
                    -movement.x * 0.18,
                    0.055,
                    -movement.z * 0.18
                );
            }

            return;
        }

        if (
            (state == 0 || state == 4)
                && hook.tickCount
                    % (state == 4 ? 4 : 6)
                    == 0
        ) {
            level.addParticle(
                ParticleTypes.BUBBLE,
                position.x
                    - movement.x,
                position.y + 0.08,
                position.z
                    - movement.z,
                0.0,
                0.018,
                0.0
            );
        }
    }

    private static void removeVisual(
        ClientLevel level,
        VisualFish visual
    ) {
        if (!visual.fish.isRemoved()) {
            level.removeEntity(
                visual.fish.getId(),
                Entity.RemovalReason.DISCARDED
            );
        }
    }

    private static void clear() {
        if (lastLevel != null) {
            for (
                VisualFish visual
                    : VISUALS.values()
            ) {
                removeVisual(
                    lastLevel,
                    visual
                );
            }
        }

        VISUALS.clear();
        lastLevel = null;
    }

    private static final class VisualFish {
        private final HookedFishKind kind;
        private final AbstractFish fish;

        private Vec3 lastPosition;
        private Vec3 lastHookPosition;
        private int lastState = -1;

        private VisualFish(
            HookedFishKind kind,
            AbstractFish fish,
            Vec3 lastPosition,
            Vec3 lastHookPosition
        ) {
            this.kind = kind;
            this.fish = fish;
            this.lastPosition =
                lastPosition;
            this.lastHookPosition =
                lastHookPosition;
        }
    }
}
