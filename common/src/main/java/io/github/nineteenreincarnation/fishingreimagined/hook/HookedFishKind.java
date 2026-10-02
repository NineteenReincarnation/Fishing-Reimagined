package io.github.nineteenreincarnation.fishingreimagined.hook;

import io.github.nineteenreincarnation.fishingreimagined.fight.FishProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.fish.AbstractFish;

public enum HookedFishKind {
    COD(
        EntityTypes.COD,
        60,
        new FishProfile(
            0.020,
            0.90,
            85.0,
            0.012,
            3.3,
            7,
            15,
            1.05,
            0.10,
            48,
            0.28
        ),
        0.16
    ),
    SALMON(
        EntityTypes.SALMON,
        30,
        new FishProfile(
            0.026,
            1.18,
            125.0,
            0.020,
            4.4,
            10,
            24,
            0.78,
            0.07,
            58,
            0.22
        ),
        0.21
    ),
    PUFFERFISH(
        EntityTypes.PUFFERFISH,
        5,
        new FishProfile(
            0.013,
            0.72,
            70.0,
            0.010,
            2.4,
            6,
            12,
            1.15,
            0.12,
            45,
            0.30
        ),
        0.12
    ),
    TROPICAL_FISH(
        EntityTypes.TROPICAL_FISH,
        5,
        new FishProfile(
            0.030,
            0.68,
            60.0,
            0.032,
            3.6,
            4,
            10,
            1.30,
            0.14,
            42,
            0.32
        ),
        0.30
    );

    private final EntityType<? extends AbstractFish> entityType;
    private final int weight;
    private final FishProfile profile;
    private final double turnResponse;

    HookedFishKind(
        EntityType<? extends AbstractFish> entityType,
        int weight,
        FishProfile profile,
        double turnResponse
    ) {
        this.entityType = entityType;
        this.weight = weight;
        this.profile = profile;
        this.turnResponse = turnResponse;
    }

    public int networkId() {
        return ordinal();
    }

    public FishProfile profile() {
        return profile;
    }

    public double turnResponse() {
        return turnResponse;
    }

    public AbstractFish createEntity(ServerLevel level) {
        return entityType.create(
            level,
            EntitySpawnReason.TRIGGERED
        );
    }

    public static HookedFishKind byNetworkId(int id) {
        HookedFishKind[] kinds = values();
        return id >= 0 && id < kinds.length
            ? kinds[id]
            : COD;
    }

    public static HookedFishKind random(
        RandomSource random
    ) {
        int total = 0;
        for (HookedFishKind kind : values()) {
            total += kind.weight;
        }

        int roll = random.nextInt(total);
        for (HookedFishKind kind : values()) {
            roll -= kind.weight;
            if (roll < 0) {
                return kind;
            }
        }

        return COD;
    }
}
