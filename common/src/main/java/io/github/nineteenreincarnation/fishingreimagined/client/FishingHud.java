package io.github.nineteenreincarnation.fishingreimagined.client;

import io.github.nineteenreincarnation.fishingreimagined.fight.ReelAction;
import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;

public final class FishingHud {
    private static final int BAR_WIDTH = 176;
    private static final int BAR_HEIGHT = 8;

    private FishingHud() {
    }

    public static void render(
        GuiGraphicsExtractor graphics,
        Minecraft minecraft
    ) {
        if (minecraft.player == null) {
            return;
        }

        FishingHook hook = minecraft.player.fishing;
        if (!(hook instanceof FishingHookFightAccess access)
            || !access.fishingReimagined$isFightActive()) {
            return;
        }

        Font font = minecraft.font;
        float tension = Math.max(0.0F, access.fishingReimagined$tensionRatio());
        float progress = Mth.clamp(
            access.fishingReimagined$catchProgress(),
            0.0F,
            1.0F
        );
        float breakRisk = Mth.clamp(
            access.fishingReimagined$breakRisk(),
            0.0F,
            1.0F
        );

        int x = 18;
        int y = graphics.guiHeight() - 94;

        graphics.nextStratum();

        graphics.text(
            font,
            Component.translatable("hud.fishing_reimagined.tension_label"),
            x,
            y,
            0xFFFFFFFF
        );
        drawTensionBar(graphics, x, y + 11, tension);

        graphics.text(
            font,
            Component.translatable("hud.fishing_reimagined.progress_label"),
            x,
            y + 26,
            0xFFFFFFFF
        );
        drawProgressBar(graphics, x, y + 37, progress);

        if (breakRisk > 0.0F) {
            Component warning = Component.translatable(
                "hud.fishing_reimagined.snap_warning"
            );
            int pulse = (minecraft.player.tickCount / 3) % 2 == 0
                ? 0xFFFF5555
                : 0xFFFFFFFF;

            graphics.text(
                font,
                warning,
                x,
                y - 13,
                pulse
            );
        } else {
            ReelAction action = ClientFishingInput.currentAction();
            Component hint = action == ReelAction.REEL_IN
                ? Component.translatable(
                    "hud.fishing_reimagined.reeling"
                )
                : Component.translatable(
                    "hud.fishing_reimagined.release"
                );

            graphics.text(
                font,
                hint,
                x + BAR_WIDTH - font.width(hint),
                y,
                action == ReelAction.REEL_IN
                    ? 0xFFA8E8A8
                    : 0xFFAACBEE
            );
        }

        if (access.fishingReimagined$fishState() == 1) {
            Component burst = Component.translatable(
                "hud.fishing_reimagined.burst_warning"
            );
            graphics.text(
                font,
                burst,
                x,
                y - 13,
                0xFFFFC45A
            );
        }
    }

    private static void drawTensionBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float tension
    ) {
        graphics.fill(
            x - 1,
            y - 1,
            x + BAR_WIDTH + 1,
            y + BAR_HEIGHT + 1,
            0xE0000000
        );
        graphics.fill(
            x,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            0xFF252525
        );

        int redStart = x + Math.round(BAR_WIDTH * 0.80F);
        graphics.fill(
            redStart,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            0xFF5C2020
        );

        float normalized = Mth.clamp(tension / 1.20F, 0.0F, 1.0F);
        int filled = Math.round(BAR_WIDTH * normalized);

        int fillColor;
        if (tension < 0.80F) {
            fillColor = 0xFF43B65C;
        } else if (tension < 1.00F) {
            fillColor = 0xFFE0A33B;
        } else {
            fillColor = 0xFFE54D4D;
        }

        if (filled > 0) {
            graphics.fill(
                x,
                y,
                x + filled,
                y + BAR_HEIGHT,
                fillColor
            );
        }
    }

    private static void drawProgressBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float progress
    ) {
        graphics.fill(
            x - 1,
            y - 1,
            x + BAR_WIDTH + 1,
            y + BAR_HEIGHT + 1,
            0xE0000000
        );
        graphics.fill(
            x,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            0xFF252525
        );

        int filled = Math.round(BAR_WIDTH * progress);
        if (filled > 0) {
            graphics.fill(
                x,
                y,
                x + filled,
                y + BAR_HEIGHT,
                0xFF3FA9E8
            );
        }
    }
}
