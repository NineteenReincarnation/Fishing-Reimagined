package io.github.nineteenreincarnation.fishingreimagined.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.nineteenreincarnation.fishingreimagined.client.FishingHookRenderStateAccess;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    @Unique
    private static final int FISHING_REIMAGINED_SEGMENTS = 16;

    @Unique
    private static final Map<Integer, RopeSimulation>
        FISHING_REIMAGINED_ROPES = new HashMap<>();

    @Unique
    private static final ThreadLocal<RopeFrame>
        FISHING_REIMAGINED_FRAME = new ThreadLocal<>();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fishingReimagined$copyFightState(
        FishingHook entity,
        FishingHookRenderState state,
        float partialTicks,
        CallbackInfo ci
    ) {
        FishingHookRenderStateAccess renderAccess =
            (FishingHookRenderStateAccess) state;

        renderAccess.fishingReimagined$setHookId(
            entity.getId()
        );

        if (entity
            instanceof FishingHookFightAccess fightAccess) {
            renderAccess.fishingReimagined$setFightActive(
                fightAccess
                    .fishingReimagined$isFightActive()
            );
            renderAccess.fishingReimagined$setTensionRatio(
                fightAccess
                    .fishingReimagined$tensionRatio()
            );
        } else {
            renderAccess.fishingReimagined$setFightActive(
                false
            );
            renderAccess.fishingReimagined$setTensionRatio(
                0.0F
            );
        }
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void fishingReimagined$beginPhysicalLine(
        FishingHookRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera,
        CallbackInfo ci
    ) {
        FishingHookRenderStateAccess access =
            (FishingHookRenderStateAccess) state;

        int hookId = access.fishingReimagined$hookId();

        if (!access.fishingReimagined$isFightActive()) {
            FISHING_REIMAGINED_ROPES.remove(hookId);
            FISHING_REIMAGINED_FRAME.remove();
            return;
        }

        ClientLevel level =
            Minecraft.getInstance().level;

        if (level == null) {
            FISHING_REIMAGINED_FRAME.remove();
            return;
        }

        Vec3 start = new Vec3(
            0.0,
            0.25,
            0.0
        );

        Vec3 end = new Vec3(
            state.lineOriginOffset.x,
            state.lineOriginOffset.y + 0.25,
            state.lineOriginOffset.z
        );

        RopeSimulation rope =
            FISHING_REIMAGINED_ROPES
                .computeIfAbsent(
                    hookId,
                    ignored ->
                        new RopeSimulation(
                            start,
                            end
                        )
                );

        Vec3 hookWorld =
            new Vec3(
                state.x,
                state.y,
                state.z
            );

        RopeFrame frame = rope.update(
            start,
            end,
            hookWorld,
            access.fishingReimagined$tensionRatio(),
            state.ageInTicks,
            level
        );

        FISHING_REIMAGINED_FRAME.set(frame);
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void fishingReimagined$endPhysicalLine(
        FishingHookRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera,
        CallbackInfo ci
    ) {
        FISHING_REIMAGINED_FRAME.remove();
    }

    @Inject(
        method = "stringVertex",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void fishingReimagined$renderRopeVertex(
        float xa,
        float ya,
        float za,
        VertexConsumer buffer,
        PoseStack.Pose pose,
        float a,
        float nextA,
        float width,
        CallbackInfo ci
    ) {
        RopeFrame frame =
            FISHING_REIMAGINED_FRAME.get();

        if (frame == null) {
            return;
        }

        int currentIndex =
            Mth.clamp(
                Math.round(
                    a * FISHING_REIMAGINED_SEGMENTS
                ),
                0,
                FISHING_REIMAGINED_SEGMENTS
            );

        int nextIndex =
            Mth.clamp(
                Math.round(
                    nextA
                        * FISHING_REIMAGINED_SEGMENTS
                ),
                0,
                FISHING_REIMAGINED_SEGMENTS
            );

        Vec3 current =
            frame.points[currentIndex];
        Vec3 next =
            frame.points[nextIndex];

        Vec3 normal =
            next.subtract(current);

        double length = normal.length();

        if (length < 1.0E-6) {
            ci.cancel();
            return;
        }

        normal = normal.scale(1.0 / length);

        buffer.addVertex(
            pose,
            (float) current.x,
            (float) current.y,
            (float) current.z
        )
            .setColor(-16777216)
            .setNormal(
                pose,
                (float) normal.x,
                (float) normal.y,
                (float) normal.z
            )
            .setLineWidth(width);

        ci.cancel();
    }

    @Unique
    private static final class RopeSimulation {
        private final Vec3[] points =
            new Vec3[FISHING_REIMAGINED_SEGMENTS + 1];

        private final Vec3[] previous =
            new Vec3[FISHING_REIMAGINED_SEGMENTS + 1];

        private float lastAge = Float.NaN;

        private RopeSimulation(
            Vec3 start,
            Vec3 end
        ) {
            reset(start, end);
        }

        private RopeFrame update(
            Vec3 start,
            Vec3 end,
            Vec3 hookWorld,
            float tensionRatio,
            float ageInTicks,
            ClientLevel level
        ) {
            if (
                points[0].distanceToSqr(start) > 4.0
                    || points[
                        FISHING_REIMAGINED_SEGMENTS
                    ].distanceToSqr(end) > 16.0
            ) {
                reset(start, end);
            }

            float dt =
                Float.isNaN(lastAge)
                    ? 0.25F
                    : Mth.clamp(
                        ageInTicks - lastAge,
                        0.05F,
                        0.50F
                    );

            lastAge = ageInTicks;

            points[0] = start;
            points[FISHING_REIMAGINED_SEGMENTS] =
                end;

            double dtSquared =
                dt * dt;

            for (
                int i = 1;
                i < FISHING_REIMAGINED_SEGMENTS;
                i++
            ) {
                Vec3 current = points[i];
                Vec3 old = previous[i];

                Vec3 velocity =
                    current
                        .subtract(old)
                        .scale(0.94);

                previous[i] = current;

                points[i] =
                    current
                        .add(velocity)
                        .add(
                            0.0,
                            -0.032 * dtSquared,
                            0.0
                        );
            }

            double endpointDistance =
                start.distanceTo(end);

            double slack =
                1.0
                    - Mth.clamp(
                        (tensionRatio - 0.10F)
                            / 0.80F,
                        0.0F,
                        1.0F
                    );

            double ropeLength =
                endpointDistance
                    * (1.0 + slack * 0.18)
                    + slack * 0.28;

            double segmentLength =
                Math.max(
                    0.02,
                    ropeLength
                        / FISHING_REIMAGINED_SEGMENTS
                );

            for (int iteration = 0; iteration < 7; iteration++) {
                points[0] = start;
                points[
                    FISHING_REIMAGINED_SEGMENTS
                ] = end;

                for (
                    int i = 0;
                    i < FISHING_REIMAGINED_SEGMENTS;
                    i++
                ) {
                    satisfy(
                        i,
                        i + 1,
                        segmentLength
                    );
                }

                for (
                    int i = 1;
                    i < FISHING_REIMAGINED_SEGMENTS;
                    i++
                ) {
                    points[i] =
                        resolveCollision(
                            points[i],
                            hookWorld,
                            level
                        );
                }
            }

            if (tensionRatio > 0.82F) {
                double warning =
                    Mth.clamp(
                        (tensionRatio - 0.82F)
                            / 0.30F,
                        0.0F,
                        1.0F
                    );

                for (
                    int i = 1;
                    i < FISHING_REIMAGINED_SEGMENTS;
                    i++
                ) {
                    double envelope =
                        Math.sin(
                            Math.PI
                                * i
                                / FISHING_REIMAGINED_SEGMENTS
                        );

                    double vibration =
                        Math.sin(
                            ageInTicks * 4.2
                                + i * 1.7
                        )
                            * 0.012
                            * warning
                            * envelope;

                    points[i] =
                        points[i].add(
                            0.0,
                            vibration,
                            0.0
                        );
                }
            }

            return new RopeFrame(
                points.clone()
            );
        }

        private void satisfy(
            int first,
            int second,
            double desiredLength
        ) {
            Vec3 a = points[first];
            Vec3 b = points[second];

            Vec3 delta =
                b.subtract(a);

            double distance =
                delta.length();

            if (distance < 1.0E-7) {
                return;
            }

            double difference =
                (distance - desiredLength)
                    / distance;

            boolean firstFixed =
                first == 0;
            boolean secondFixed =
                second
                    == FISHING_REIMAGINED_SEGMENTS;

            if (firstFixed && secondFixed) {
                return;
            }

            if (firstFixed) {
                points[second] =
                    b.subtract(
                        delta.scale(difference)
                    );
                return;
            }

            if (secondFixed) {
                points[first] =
                    a.add(
                        delta.scale(difference)
                    );
                return;
            }

            Vec3 correction =
                delta.scale(
                    difference * 0.5
                );

            points[first] =
                a.add(correction);
            points[second] =
                b.subtract(correction);
        }

        private void reset(
            Vec3 start,
            Vec3 end
        ) {
            for (
                int i = 0;
                i <= FISHING_REIMAGINED_SEGMENTS;
                i++
            ) {
                double fraction =
                    i
                        / (double)
                            FISHING_REIMAGINED_SEGMENTS;

                Vec3 point =
                    start.lerp(
                        end,
                        fraction
                    );

                double sag =
                    Math.sin(
                        Math.PI * fraction
                    ) * 0.20;

                point =
                    point.add(
                        0.0,
                        -sag,
                        0.0
                    );

                points[i] = point;
                previous[i] = point;
            }

            lastAge = Float.NaN;
        }
    }

    @Unique
    private static Vec3 resolveCollision(
        Vec3 localPoint,
        Vec3 hookWorld,
        ClientLevel level
    ) {
        Vec3 worldPoint =
            hookWorld.add(localPoint);

        BlockPos blockPos =
            BlockPos.containing(
                worldPoint.x,
                worldPoint.y,
                worldPoint.z
            );

        BlockState state =
            level.getBlockState(blockPos);

        VoxelShape shape =
            state.getCollisionShape(
                level,
                blockPos
            );

        if (shape.isEmpty()) {
            return localPoint;
        }

        Vec3 blockLocal =
            new Vec3(
                worldPoint.x - blockPos.getX(),
                worldPoint.y - blockPos.getY(),
                worldPoint.z - blockPos.getZ()
            );

        List<AABB> boxes =
            shape.toAabbs();

        for (AABB box : boxes) {
            if (!box.contains(blockLocal)) {
                continue;
            }

            double left =
                blockLocal.x - box.minX;
            double right =
                box.maxX - blockLocal.x;
            double bottom =
                blockLocal.y - box.minY;
            double top =
                box.maxY - blockLocal.y;
            double front =
                blockLocal.z - box.minZ;
            double back =
                box.maxZ - blockLocal.z;

            double minimum =
                Math.min(
                    Math.min(
                        Math.min(left, right),
                        Math.min(bottom, top)
                    ),
                    Math.min(front, back)
                );

            double epsilon = 0.012;

            if (minimum == left) {
                blockLocal =
                    new Vec3(
                        box.minX - epsilon,
                        blockLocal.y,
                        blockLocal.z
                    );
            } else if (minimum == right) {
                blockLocal =
                    new Vec3(
                        box.maxX + epsilon,
                        blockLocal.y,
                        blockLocal.z
                    );
            } else if (minimum == bottom) {
                blockLocal =
                    new Vec3(
                        blockLocal.x,
                        box.minY - epsilon,
                        blockLocal.z
                    );
            } else if (minimum == top) {
                blockLocal =
                    new Vec3(
                        blockLocal.x,
                        box.maxY + epsilon,
                        blockLocal.z
                    );
            } else if (minimum == front) {
                blockLocal =
                    new Vec3(
                        blockLocal.x,
                        blockLocal.y,
                        box.minZ - epsilon
                    );
            } else {
                blockLocal =
                    new Vec3(
                        blockLocal.x,
                        blockLocal.y,
                        box.maxZ + epsilon
                    );
            }

            worldPoint =
                new Vec3(
                    blockPos.getX()
                        + blockLocal.x,
                    blockPos.getY()
                        + blockLocal.y,
                    blockPos.getZ()
                        + blockLocal.z
                );
        }

        return worldPoint.subtract(hookWorld);
    }

    @Unique
    private record RopeFrame(Vec3[] points) {
    }
}
