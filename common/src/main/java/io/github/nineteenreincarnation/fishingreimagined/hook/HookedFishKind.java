package io.github.nineteenreincarnation.fishingreimagined.hook;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.AbstractFish;

public enum HookedFishKind {
    COD(EntityType.COD, 60),
    SALMON(EntityType.SALMON, 30),
    PUFFERFISH(EntityType.PUFFERFISH, 5),
    TROPICAL_FISH(EntityType.TROPICAL_FISH, 5);

    private final EntityType<? extends AbstractFish> entityType;
    private final int weight;

    HookedFishKind(EntityType<? extends AbstractFish> entityType, int weight) {
        this.entityType = entityType;
        this.weight = weight;
    }

    public int networkId() {
        return ordinal();
    }

    public AbstractFish createEntity(ServerLevel level) {
        return entityType.create(level, EntitySpawnReason.TRIGGERED);
    }

    public static HookedFishKind byNetworkId(int id) {
        HookedFishKind[] kinds = values();
        return id >= 0 && id < kinds.length ? kinds[id] : COD;
    }

    public static HookedFishKind random(RandomSource random) {
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
