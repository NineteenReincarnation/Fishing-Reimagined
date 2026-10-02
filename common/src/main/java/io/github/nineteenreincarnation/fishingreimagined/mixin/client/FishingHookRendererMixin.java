package io.github.nineteenreincarnation.fishingreimagined.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.nineteenreincarnation.fishingreimagined.client.FishingHookRenderStateAccess;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
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
                new LineVisual(access.fishingReimagined$tensionRatio())
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

        float lineDistance = Mth.sqrt(xa * xa + ya * ya + za * za);
        float tension = Math.max(0.0F, visual.tensionRatio);
        float slack = 1.0F - Mth.clamp(tension, 0.0F, 1.0F);
        float sagStrength =
            Math.min(1.65F, lineDistance * 0.075F)
                * slack
                * slack;

        float x = xa * a;
        float y = baseY(ya, a)
            - sagStrength * Mth.sin((float) Math.PI * a);
        float z = za * a;

        float nextX = xa * nextA;
        float nextY = baseY(ya, nextA)
            - sagStrength * Mth.sin((float) Math.PI * nextA);
        float nextZ = za * nextA;

        float nx = nextX - x;
        float ny = nextY - y;
        float nz = nextZ - z;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < 1.0E-5F) {
            return;
        }

        nx /= length;
        ny /= length;
        nz /= length;

        buffer.addVertex(pose, x, y, z)
            .setColor(-16777216)
            .setNormal(pose, nx, ny, nz)
            .setLineWidth(width);
        ci.cancel();
    }

    @Unique
    private static float baseY(float ya, float a) {
        return ya * (a * a + a) * 0.5F + 0.25F;
    }

    @Unique
    private record LineVisual(float tensionRatio) {
    }
}
