package io.github.nineteenreincarnation.fishingreimagined.hook;

import io.github.nineteenreincarnation.fishingreimagined.fight.BasicFishBehavior;
import io.github.nineteenreincarnation.fishingreimagined.fight.FightSnapshot;
import io.github.nineteenreincarnation.fishingreimagined.fight.FishProfile;
import io.github.nineteenreincarnation.fishingreimagined.fight.FishingFight;
import io.github.nineteenreincarnation.fishingreimagined.fight.LineProfile;
import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public final class HookedFish {
    private static final double MAX_VISUAL_RADIUS = 31.0;
    private final HookedFishKind kind;
    private final FishingFight fight;
    private final double waterY;
    private double bearing;

    private HookedFish(
        HookedFishKind kind,
        FishingFight fight,
        double waterY,
        double bearing
    ) {
        this.kind = kind;
        this.fight = fight;
        this.waterY = waterY;
        this.bearing = bearing;
    }

    public static HookedFish create(
        Player owner,
        FishingHook hook,
        RandomSource random
    ) {
        double dx = hook.getX() - owner.getX();
        double dz = hook.getZ() - owner.getZ();
        double distance = Math.max(
            2.0,
            Math.min(30.0, Math.sqrt(dx * dx + dz * dz))
        );
        double bearing = Math.atan2(dz, dx);
        HookedFishKind kind = HookedFishKind.random(random);

        FishingFight fight = new FishingFight(
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

    public FightSnapshot tick(ReelAction action) {
        FightSnapshot snapshot = fight.tick(action);
        bearing +=
            snapshot.fishIntent().lateralTurnRadians()
                * kind.turnResponse();
        return snapshot;
    }

    public Vec3 desiredPosition(
        Player owner,
        FightSnapshot snapshot
    ) {
        double radius = Math.min(
            MAX_VISUAL_RADIUS,
            Math.max(0.0, snapshot.distance())
        );

        return new Vec3(
            owner.getX() + Math.cos(bearing) * radius,
            waterY,
            owner.getZ() + Math.sin(bearing) * radius
        );
    }

    public HookedFishKind kind() {
        return kind;
    }

    public void materialize(
        ServerLevel level,
        Player owner,
        FishingHook hook
    ) {
        AbstractFish fish = kind.createEntity(level);
        if (fish == null) {
            return;
        }

        fish.setPersistenceRequired();
        fish.snapTo(
            hook.getX(),
            hook.getY(),
            hook.getZ(),
            hook.getYRot(),
            0.0F
        );

        Vec3 towardOwner =
            owner.position()
                .add(0.0, 0.7, 0.0)
                .subtract(hook.position());

        double horizontal = Math.sqrt(
            towardOwner.x * towardOwner.x
                + towardOwner.z * towardOwner.z
        );
        double yVelocity =
            0.34 + Math.min(0.34, horizontal * 0.018);

        fish.setDeltaMovement(
            towardOwner.x * 0.105,
            yVelocity,
            towardOwner.z * 0.105
        );

        level.addFreshEntity(fish);
        owner.awardStat(Stats.FISH_CAUGHT, 1);
    }
}
