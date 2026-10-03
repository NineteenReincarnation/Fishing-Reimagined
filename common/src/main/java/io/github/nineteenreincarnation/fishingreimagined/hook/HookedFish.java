package io.github.nineteenreincarnation.fishingreimagined.hook;

import io.github.nineteenreincarnation.fishingreimagined.fight.BasicFishBehavior;
import io.github.nineteenreincarnation.fishingreimagined.fight.FightPhase;
import io.github.nineteenreincarnation.fishingreimagined.fight.FightSnapshot;
import io.github.nineteenreincarnation.fishingreimagined.fight.FishingFight;
import io.github.nineteenreincarnation.fishingreimagined.fight.LineProfile;
import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public final class HookedFish {
    private static final double MAX_VISUAL_RADIUS = 31.0;

    private final HookedFishKind kind;
    private final FishingFight fight;
    private final double waterY;
    private final double baseBearing;

    private double lateralAngle;

    private HookedFish(
        HookedFishKind kind,
        FishingFight fight,
        double waterY,
        double baseBearing
    ) {
        this.kind = kind;
        this.fight = fight;
        this.waterY = waterY;
        this.baseBearing = baseBearing;
    }

    public static HookedFish create(
        Player owner,
        FishingHook hook,
        RandomSource random
    ) {
        double dx =
            hook.getX() - owner.getX();

        double dz =
            hook.getZ() - owner.getZ();

        double distance =
            Math.max(
                2.0,
                Math.min(
                    30.0,
                    Math.sqrt(dx * dx + dz * dz)
                )
            );

        double bearing =
            Math.atan2(dz, dx);

        HookedFishKind kind =
            HookedFishKind.random(random);

        FishingFight fight =
            new FishingFight(
                kind.profile(),
                LineProfile.PROTOTYPE,
                BasicFishBehavior.INSTANCE,
                new Random(random.nextLong()),
                distance,
                distance
            );

        return new HookedFish(
            kind,
            fight,
            hook.getY(),
            bearing
        );
    }

    public FightSnapshot tick(
        ReelAction action
    ) {
        FightSnapshot snapshot =
            fight.tick(action);

        updateWorldMotion(snapshot);

        return snapshot;
    }

    private void updateWorldMotion(
        FightSnapshot snapshot
    ) {
        double targetAngle =
            (
                snapshot.fishTrackPosition()
                    - 0.5
            ) * 1.05;

        double speed =
            Math.abs(
                snapshot.fishVelocity()
            );

        double response =
            Math.min(
                0.38,
                0.16 + speed * 7.0
            );

        if (snapshot.phase() == FightPhase.TIRED) {
            response *= 0.75;
        }

        lateralAngle +=
            (targetAngle - lateralAngle)
                * response;
    }

    public Vec3 desiredPosition(
        Player owner,
        FightSnapshot snapshot
    ) {
        double radius =
            Math.min(
                MAX_VISUAL_RADIUS,
                Math.max(
                    1.2,
                    snapshot.distance()
                )
            );

        double animatedBearing =
            baseBearing + lateralAngle;

        return new Vec3(
            owner.getX()
                + Math.cos(animatedBearing)
                    * radius,
            waterY,
            owner.getZ()
                + Math.sin(animatedBearing)
                    * radius
        );
    }

    public HookedFishKind kind() {
        return kind;
    }

    public void rewardCatch(
        ServerLevel level,
        Player owner
    ) {
        ExperienceOrb.award(
            level,
            owner.position()
                .add(0.0, 0.5, 0.0),
            1 + level.getRandom().nextInt(6)
        );

        owner.awardStat(
            Stats.FISH_CAUGHT,
            1
        );
    }

    public void materialize(
        ServerLevel level,
        FishingHook hook
    ) {
        AbstractFish fish =
            kind.createEntity(level);

        if (fish == null) {
            return;
        }

        fish.setPersistenceRequired();

        fish.snapTo(
            hook.getX(),
            hook.getY() - 0.48,
            hook.getZ(),
            hook.getYRot(),
            0.0F
        );

        fish.setDeltaMovement(
            0.0,
            -0.04,
            0.0
        );

        level.addFreshEntity(fish);
    }
}
