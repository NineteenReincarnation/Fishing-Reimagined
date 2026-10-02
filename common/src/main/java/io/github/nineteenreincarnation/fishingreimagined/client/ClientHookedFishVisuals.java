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
                    -0.30,
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

        double yaw =
            hook.getYRot();

        return new VisualFish(
            kind,
            fish,
            start,
            hook.position(),
            yaw,
            yaw,
            -0.30,
            forwardFromYaw(yaw)
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

        Vec3 hookVelocity =
            hook.position()
                .subtract(
                    visual.lastHookPosition
                );

        Vec3 horizontalVelocity =
            new Vec3(
                hookVelocity.x,
                0.0,
                hookVelocity.z
            );

        double horizontalSpeed =
            horizontalVelocity.length();

        if (horizontalSpeed > 0.002) {
            Vec3 movementDirection =
                horizontalVelocity.scale(
                    1.0 / horizontalSpeed
                );

            double targetYaw =
                Math.toDegrees(
                    Math.atan2(
                        movementDirection.z,
                        movementDirection.x
                    )
                )
                    - 90.0;

            visual.yaw =
                approachDegrees(
                    visual.yaw,
                    targetYaw,
                    state == 1
                        ? 0.42
                        : 0.28
                );

            visual.bodyYaw =
                approachDegrees(
                    visual.bodyYaw,
                    visual.yaw,
                    state == 1
                        ? 0.30
                        : 0.18
                );

            visual.forward =
                visual.forward
                    .lerp(
                        movementDirection,
                        state == 1
                            ? 0.34
                            : 0.22
                    )
                    .normalize();
        } else {
            visual.bodyYaw =
                approachDegrees(
                    visual.bodyYaw,
                    visual.yaw,
                    0.12
                );
        }

        double targetDepth =
            switch (state) {
                case 3 -> -0.08;
                case 1 -> -0.18;
                case 2 -> -0.38;
                case 4 -> -0.24;
                case 5 -> -0.32;
                default -> -0.28;
            };

        visual.depth +=
            (
                targetDepth
                    - visual.depth
            ) * (
                state == 1
                    ? 0.24
                    : 0.14
            );

        double headOffset =
            switch (visual.kind) {
                case SALMON -> 0.36;
                case COD -> 0.28;
                case PUFFERFISH -> 0.18;
                case TROPICAL_FISH -> 0.24;
            };

        Vec3 target =
            hook.position()
                .subtract(
                    visual.forward.scale(
                        headOffset
                    )
                )
                .add(
                    0.0,
                    visual.depth,
                    0.0
                );

        Vec3 movement =
            target.subtract(
                visual.lastPosition
            );

        float verticalPitch =
            (float) Mth.clamp(
                -hookVelocity.y * 72.0,
                -16.0,
                16.0
            );

        float motionPitch =
            (float) (
                Math.sin(
                    hook.tickCount
                        * (
                            0.28
                                + Math.min(
                                    0.36,
                                    horizontalSpeed
                                        * 8.0
                                )
                        )
                )
                    * Math.min(
                        5.0,
                        1.5
                            + horizontalSpeed
                                * 32.0
                    )
            );

        visual.fish.setYRot(
            (float) visual.yaw
        );

        visual.fish.yBodyRot =
            (float) visual.bodyYaw;

        visual.fish.yHeadRot =
            (float) visual.yaw;

        visual.fish.setXRot(
            verticalPitch
                + motionPitch
        );

        visual.fish.setDeltaMovement(
            movement
        );

        visual.fish.setPos(
            target.x,
            target.y,
            target.z
        );

        if (visual.lastState != state) {
            emitStateTransitionParticles(
                level,
                hook,
                target,
                state,
                visual.forward
            );
            visual.lastState = state;
        }

        emitMotionParticles(
            level,
            hook,
            target,
            state,
            visual.forward,
            horizontalSpeed
        );

        visual.lastPosition = target;
        visual.lastHookPosition =
            hook.position();
    }

    private static void emitStateTransitionParticles(
        ClientLevel level,
        FishingHook hook,
        Vec3 position,
        int state,
        Vec3 forward
    ) {
        if (state != 1) {
            return;
        }

        Vec3 wakeDirection =
            forward.scale(-1.0);

        for (int i = 0; i < 4; i++) {
            double spread =
                (i - 1.5) * 0.07;

            level.addParticle(
                ParticleTypes.SPLASH,
                position.x
                    + wakeDirection.x
                        * 0.20,
                hook.getY() + 0.05,
                position.z
                    + wakeDirection.z
                        * 0.20,
                wakeDirection.x * 0.05
                    + spread,
                0.05,
                wakeDirection.z * 0.05
                    - spread
            );
        }

        for (int i = 0; i < 5; i++) {
            level.addParticle(
                ParticleTypes.BUBBLE,
                position.x
                    + wakeDirection.x
                        * (
                            0.10
                                + i * 0.05
                        ),
                position.y + 0.08,
                position.z
                    + wakeDirection.z
                        * (
                            0.10
                                + i * 0.05
                        ),
                wakeDirection.x * 0.015,
                0.02,
                wakeDirection.z * 0.015
            );
        }
    }

    private static void emitMotionParticles(
        ClientLevel level,
        FishingHook hook,
        Vec3 position,
        int state,
        Vec3 forward,
        double speed
    ) {
        Vec3 wakeDirection =
            forward.scale(-1.0);

        if (
            state == 1
                && hook.tickCount % 4 == 0
        ) {
            level.addParticle(
                ParticleTypes.BUBBLE,
                position.x
                    + wakeDirection.x
                        * 0.28,
                position.y + 0.08,
                position.z
                    + wakeDirection.z
                        * 0.28,
                wakeDirection.x * 0.02,
                0.018,
                wakeDirection.z * 0.02
            );

            if (
                speed > 0.08
                    && hook.tickCount % 8 == 0
            ) {
                level.addParticle(
                    ParticleTypes.SPLASH,
                    position.x,
                    hook.getY() + 0.04,
                    position.z,
                    wakeDirection.x * 0.05,
                    0.035,
                    wakeDirection.z * 0.05
                );
            }

            return;
        }

        if (
            state != 3
                && speed > 0.025
                && hook.tickCount % 10 == 0
        ) {
            level.addParticle(
                ParticleTypes.BUBBLE,
                position.x
                    + wakeDirection.x
                        * 0.20,
                position.y + 0.06,
                position.z
                    + wakeDirection.z
                        * 0.20,
                0.0,
                0.012,
                0.0
            );
        }
    }

    private static double approachDegrees(
        double current,
        double target,
        double response
    ) {
        double delta =
            Mth.wrapDegrees(
                target - current
            );

        return current
            + delta * response;
    }

    private static Vec3 forwardFromYaw(
        double yaw
    ) {
        double radians =
            Math.toRadians(yaw);

        return new Vec3(
            -Math.sin(radians),
            0.0,
            Math.cos(radians)
        );
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
        private double yaw;
        private double bodyYaw;
        private double depth;
        private Vec3 forward;
        private int lastState = -1;

        private VisualFish(
            HookedFishKind kind,
            AbstractFish fish,
            Vec3 lastPosition,
            Vec3 lastHookPosition,
            double yaw,
            double bodyYaw,
            double depth,
            Vec3 forward
        ) {
            this.kind = kind;
            this.fish = fish;
            this.lastPosition = lastPosition;
            this.lastHookPosition =
                lastHookPosition;
            this.yaw = yaw;
            this.bodyYaw = bodyYaw;
            this.depth = depth;
            this.forward = forward;
        }
    }
}
