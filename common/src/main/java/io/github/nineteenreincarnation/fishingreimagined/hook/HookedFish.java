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
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public final class HookedFish {
    private static final double MAX_VISUAL_RADIUS = 31.0;

    private final HookedFishKind kind;
    private final FishingFight fight;
    private final double waterY;
    private final double motionPhase;

    private double bearing;
    private double angularVelocity;

    private HookedFish(
        HookedFishKind kind,
        FishingFight fight,
        double waterY,
        double bearing,
        double motionPhase
    ) {
        this.kind = kind;
        this.fight = fight;
        this.waterY = waterY;
        this.bearing = bearing;
        this.motionPhase = motionPhase;
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
            Math.min(
                30.0,
                Math.sqrt(dx * dx + dz * dz)
            )
        );
        double bearing = Math.atan2(dz, dx);
        HookedFishKind kind =
            HookedFishKind.random(random);

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
            bearing,
            random.nextDouble() * Math.PI * 2.0
        );
    }

    public FightSnapshot tick(ReelAction action) {
        FightSnapshot snapshot =
            fight.tick(action);

        updateWorldMotion(snapshot);

        return snapshot;
    }

    private void updateWorldMotion(
        FightSnapshot snapshot
    ) {
        double tick = snapshot.tick();
        double intentTurn =
            snapshot.fishIntent()
                .lateralTurnRadians();

        boolean burst =
            snapshot.fishIntent().burst();
        boolean tired =
            snapshot.phase() == FightPhase.TIRED;

        double weave = Math.sin(
            tick * (
                burst
                    ? 0.34
                    : tired
                        ? 0.09
                        : 0.17
            ) + motionPhase
        );

        double targetAngularVelocity;
        double response;

        if (burst) {
            targetAngularVelocity =
                clamp(
                    intentTurn * 2.7
                        + weave * 0.045,
                    -0.095,
                    0.095
                );
            response = 0.34;
        } else if (tired) {
            targetAngularVelocity =
                clamp(
                    intentTurn * 0.65
                        + weave * 0.006,
                    -0.018,
                    0.018
                );
            response = 0.11;
        } else {
            targetAngularVelocity =
                clamp(
                    intentTurn * 1.65
                        + weave * 0.022,
                    -0.052,
                    0.052
                );
            response = 0.19;
        }

        angularVelocity +=
            (targetAngularVelocity
                - angularVelocity)
                * response;

        bearing += angularVelocity;
    }

    public Vec3 desiredPosition(
        Player owner,
        FightSnapshot snapshot
    ) {
        double radius = Math.min(
            MAX_VISUAL_RADIUS,
            Math.max(
                0.0,
                snapshot.distance()
            )
        );

        boolean burst =
            snapshot.fishIntent().burst();
        boolean tired =
            snapshot.phase() == FightPhase.TIRED;

        double tick = snapshot.tick();

        double sweepAngle =
            Math.sin(
                tick
                    * (
                        burst
                            ? 0.30
                            : tired
                                ? 0.08
                                : 0.14
                    )
                    + motionPhase
            )
                * (
                    burst
                        ? 0.22
                        : tired
                            ? 0.045
                            : 0.095
                );

        double radialPulse =
            Math.sin(
                tick
                    * (
                        burst
                            ? 0.42
                            : tired
                                ? 0.10
                                : 0.18
                    )
                    + motionPhase * 0.73
            )
                * (
                    burst
                        ? 0.95
                        : tired
                            ? 0.10
                            : 0.34
                );

        double animatedRadius = Math.min(
            MAX_VISUAL_RADIUS,
            Math.max(
                1.2,
                radius + radialPulse
            )
        );

        double animatedBearing =
            bearing + sweepAngle;

        return new Vec3(
            owner.getX()
                + Math.cos(animatedBearing)
                    * animatedRadius,
            waterY,
            owner.getZ()
                + Math.sin(animatedBearing)
                    * animatedRadius
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
        AbstractFish fish =
            kind.createEntity(level);

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
                + towardOwner.z
                    * towardOwner.z
        );

        double yVelocity =
            0.34
                + Math.min(
                    0.34,
                    horizontal * 0.018
                );

        fish.setDeltaMovement(
            towardOwner.x * 0.105,
            yVelocity,
            towardOwner.z * 0.105
        );

        level.addFreshEntity(fish);
        owner.awardStat(
            Stats.FISH_CAUGHT,
            1
        );
    }

    private static double clamp(
        double value,
        double min,
        double max
    ) {
        return Math.max(
            min,
            Math.min(max, value)
        );
    }
}
