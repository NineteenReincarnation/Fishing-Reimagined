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
    private static final Map<Integer, VisualFish> VISUALS = new HashMap<>();
    private static ClientLevel lastLevel;

    private ClientHookedFishVisuals() {
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }

        if (lastLevel != level) {
            clear();
            lastLevel = level;
        }

        List<FishingHook> hooks = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof FishingHook hook) {
                hooks.add(hook);
            }
        }

        Set<Integer> activeHookIds = new HashSet<>();
        for (FishingHook hook : hooks) {
            if (!(hook instanceof FishingHookFightAccess access)
                || !access.fishingReimagined$isFightActive()) {
                continue;
            }

            activeHookIds.add(hook.getId());
            HookedFishKind kind = HookedFishKind.byNetworkId(
                access.fishingReimagined$fishKindId()
            );

            VisualFish visual = VISUALS.get(hook.getId());
            if (visual == null
                || visual.kind != kind
                || visual.fish.isRemoved()) {
                if (visual != null) {
                    removeVisual(level, visual);
                }
                visual = createVisual(level, hook, kind);
                VISUALS.put(hook.getId(), visual);
            }

            updateVisual(hook, access, visual);
        }

        List<Integer> stale = new ArrayList<>();
        for (Map.Entry<Integer, VisualFish> entry : VISUALS.entrySet()) {
            if (!activeHookIds.contains(entry.getKey())) {
                removeVisual(level, entry.getValue());
                stale.add(entry.getKey());
            }
        }
        stale.forEach(VISUALS::remove);
    }

    private static VisualFish createVisual(
        ClientLevel level,
        FishingHook hook,
        HookedFishKind kind
    ) {
        AbstractFish fish = switch (kind) {
            case COD -> new Cod(EntityTypes.COD, level);
            case SALMON -> new Salmon(EntityTypes.SALMON, level);
            case PUFFERFISH -> new Pufferfish(EntityTypes.PUFFERFISH, level);
            case TROPICAL_FISH -> new TropicalFish(EntityTypes.TROPICAL_FISH, level);
        };

        fish.setId(-1_000_000_000 + hook.getId());
        fish.setNoAi(true);
        fish.setNoGravity(true);
        fish.setInvulnerable(true);
        fish.setSilent(true);

        Vec3 start = hook.position().add(0.0, -0.52, 0.0);
        fish.snapTo(start.x, start.y, start.z, hook.getYRot(), 0.0F);
        fish.setOldPosAndRot();
        level.addEntity(fish);
        return new VisualFish(kind, fish, start);
    }

    private static void updateVisual(
        FishingHook hook,
        FishingHookFightAccess access,
        VisualFish visual
    ) {
        int state = access.fishingReimagined$fishState();
        double verticalMotion = switch (state) {
            case 1 -> Math.sin((hook.tickCount + hook.getId()) * 0.9) * 0.12;
            case 2 -> Math.sin((hook.tickCount + hook.getId()) * 0.35) * 0.035;
            default -> Math.sin((hook.tickCount + hook.getId()) * 0.55) * 0.065;
        };

        Vec3 target = hook.position().add(0.0, -0.52 + verticalMotion, 0.0);
        Vec3 movement = target.subtract(visual.lastPosition);

        if (movement.horizontalDistanceSqr() > 1.0E-5) {
            float yaw = (float)(
                Mth.atan2(movement.z, movement.x) * 180.0 / Math.PI
            ) - 90.0F;
            visual.fish.setYRot(yaw);
        }

        visual.fish.setDeltaMovement(movement);
        visual.fish.setPos(target.x, target.y, target.z);
        visual.lastPosition = target;
    }

    private static void removeVisual(ClientLevel level, VisualFish visual) {
        if (!visual.fish.isRemoved()) {
            level.removeEntity(
                visual.fish.getId(),
                Entity.RemovalReason.DISCARDED
            );
        }
    }

    private static void clear() {
        if (lastLevel != null) {
            for (VisualFish visual : VISUALS.values()) {
                removeVisual(lastLevel, visual);
            }
        }
        VISUALS.clear();
        lastLevel = null;
    }

    private static final class VisualFish {
        private final HookedFishKind kind;
        private final AbstractFish fish;
        private Vec3 lastPosition;

        private VisualFish(
            HookedFishKind kind,
            AbstractFish fish,
            Vec3 lastPosition
        ) {
            this.kind = kind;
            this.fish = fish;
            this.lastPosition = lastPosition;
        }
    }
}
