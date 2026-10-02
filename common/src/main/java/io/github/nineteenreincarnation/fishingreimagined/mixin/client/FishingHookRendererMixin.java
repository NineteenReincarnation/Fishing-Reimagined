package io.github.nineteenreincarnation.fishingreimagined.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.nineteenreincarnation.fishingreimagined.client.FishingHookRenderStateAccess;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
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
    private static final ThreadLocal<LineVisual> FISHING_REIMAGINED_LINE =
        new ThreadLocal<>();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fishingReimagined$copyFightState(
        FishingHook entity,
        FishingHookRenderState state,
        float partialTicks,
        CallbackInfo ci
    ) {
        FishingHookRenderStateAccess renderAccess =
            (FishingHookRenderStateAccess) state;

        if (entity instanceof FishingHookFightAccess fightAccess) {
            renderAccess.fishingReimagined$setFightActive(
                fightAccess.fishingReimagined$isFightActive()
            );
            renderAccess.fishingReimagined$setTensionRatio(
                fightAccess.fishingReimagined$tensionRatio()
            );
        } else {
            renderAccess.fishingReimagined$setFightActive(false);
            renderAccess.fishingReimagined$setTensionRatio(0.0F);
        }
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void fishingReimagined$beginDynamicLine(
        FishingHookRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera,
        CallbackInfo ci
    ) {
        FishingHookRenderStateAccess access =
            (FishingHookRenderStateAccess) state;

        if (access.fishingReimagined$isFightActive()) {
            FISHING_REIMAGINED_LINE.set(
                new LineVisual(
                    access.fishingReimagined$tensionRatio(),
                    state.x,
                    state.y,
                    state.z,
                    state.ageInTicks,
                    Minecraft.getInstance().level
                )
            );
        } else {
            FISHING_REIMAGINED_LINE.remove();
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void fishingReimagined$endDynamicLine(
        FishingHookRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera,
        CallbackInfo ci
    ) {
        FISHING_REIMAGINED_LINE.remove();
    }

    @Inject(method = "stringVertex", at = @At("HEAD"), cancellable = true)
    private static void fishingReimagined$renderDynamicLineVertex(
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
        LineVisual visual = FISHING_REIMAGINED_LINE.get();
        if (visual == null) {
            return;
        }

        float lineDistance = Mth.sqrt(
            xa * xa + ya * ya + za * za
        );
        float tension = Math.max(
            0.0F,
            visual.tensionRatio
        );
        float normalizedTension =
            Mth.clamp(tension, 0.0F, 1.0F);
        float slack = 1.0F - normalizedTension;

        float sagStrength =
            Math.min(1.85F, lineDistance * 0.09F)
                * slack
                * slack;

        LocalPoint current = fishingReimagined$linePoint(
            xa,
            ya,
            za,
            a,
            sagStrength,
            visual
        );
        LocalPoint next = fishingReimagined$linePoint(
            xa,
            ya,
            za,
            nextA,
            sagStrength,
            visual
        );

        float nx = next.x - current.x;
        float ny = next.y - current.y;
        float nz = next.z - current.z;
        float length = Mth.sqrt(
            nx * nx + ny * ny + nz * nz
        );
        if (length < 1.0E-5F) {
            ci.cancel();
            return;
        }

        nx /= length;
        ny /= length;
        nz /= length;

        buffer.addVertex(
            pose,
            current.x,
            current.y,
            current.z
        )
            .setColor(-16777216)
            .setNormal(pose, nx, ny, nz)
            .setLineWidth(width);

        ci.cancel();
    }

    @Unique
    private static LocalPoint fishingReimagined$linePoint(
        float xa,
        float ya,
        float za,
        float fraction,
        float sagStrength,
        LineVisual visual
    ) {
        float x = xa * fraction;
        float y =
            fishingReimagined$baseY(ya, fraction)
                - sagStrength
                    * Mth.sin((float) Math.PI * fraction);
        float z = za * fraction;

        float highLoad =
            Mth.clamp(
                (visual.tensionRatio - 0.72F) / 0.53F,
                0.0F,
                1.0F
            );
        if (highLoad > 0.0F
            && fraction > 0.08F
            && fraction < 0.92F) {
            float vibration =
                Mth.sin(
                    visual.ageInTicks * 2.6F
                        + fraction * 34.0F
                )
                    * 0.022F
                    * highLoad;

            y += vibration
                * Mth.sin((float) Math.PI * fraction);
        }

        return fishingReimagined$resolveCollision(
            new LocalPoint(x, y, z),
            fraction,
            visual
        );
    }

    @Unique
    private static LocalPoint fishingReimagined$resolveCollision(
        LocalPoint point,
        float fraction,
        LineVisual visual
    ) {
        if (visual.level == null
            || fraction <= 0.04F
            || fraction >= 0.96F) {
            return point;
        }

        double worldX = visual.hookX + point.x;
        double worldY = visual.hookY + point.y;
        double worldZ = visual.hookZ + point.z;

        BlockPos pos = BlockPos.containing(
            worldX,
            worldY,
            worldZ
        );
        BlockState blockState =
            visual.level.getBlockState(pos);
        VoxelShape shape =
            blockState.getCollisionShape(
                visual.level,
                pos
            );

        if (shape.isEmpty()) {
            return point;
        }

        AABB bounds = shape.bounds();
        Vec3 local = new Vec3(
            worldX - pos.getX(),
            worldY - pos.getY(),
            worldZ - pos.getZ()
        );

        if (!bounds.contains(local)) {
            return point;
        }

        float topY =
            (float) (
                pos.getY()
                    + bounds.maxY
                    + 0.025
                    - visual.hookY
            );

        return new LocalPoint(
            point.x,
            Math.max(point.y, topY),
            point.z
        );
    }

    @Unique
    private static float fishingReimagined$baseY(
        float ya,
        float fraction
    ) {
        return ya
            * (fraction * fraction + fraction)
            * 0.5F
            + 0.25F;
    }

    @Unique
    private record LocalPoint(float x, float y, float z) {
    }

    @Unique
    private record LineVisual(
        float tensionRatio,
        double hookX,
        double hookY,
        double hookZ,
        float ageInTicks,
        ClientLevel level
    ) {
    }
}
