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
    private static final int PANEL_WIDTH = 222;
    private static final int PANEL_HEIGHT = 62;
    private static final int BAR_WIDTH = 194;
    private static final int BAR_HEIGHT = 7;

    private static final float LOW_END = 0.28F;
    private static final float IDEAL_END = 0.68F;
    private static final float RED_START = 0.90F;
    private static final float DISPLAY_MAX = 1.20F;

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

        float tension = Math.max(
            0.0F,
            access.fishingReimagined$tensionRatio()
        );

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

        int centerX = graphics.guiWidth() / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = graphics.guiHeight() - 108;
        int contentX = panelX + 14;

        graphics.nextStratum();

        drawPanel(
            graphics,
            panelX,
            panelY
        );

        ReelAction action =
            ClientFishingInput.currentAction();

        Component tensionLabel =
            Component.translatable(
                "hud.fishing_reimagined.tension_label"
            );

        Component actionLabel =
            action == ReelAction.REEL_IN
                ? Component.translatable(
                    "hud.fishing_reimagined.reeling"
                )
                : Component.translatable(
                    "hud.fishing_reimagined.release"
                );

        graphics.text(
            font,
            tensionLabel,
            contentX,
            panelY + 8,
            0xFFF3F3F3
        );

        graphics.text(
            font,
            actionLabel,
            panelX + PANEL_WIDTH - 14
                - font.width(actionLabel),
            panelY + 8,
            action == ReelAction.REEL_IN
                ? 0xFFA8E8A8
                : 0xFFB7D5F3
        );

        drawTensionBar(
            graphics,
            contentX,
            panelY + 21,
            tension
        );

        Component progressLabel =
            Component.translatable(
                "hud.fishing_reimagined.progress_label"
            );

        String progressPercent =
            Math.round(progress * 100.0F)
                + "%";

        graphics.text(
            font,
            progressLabel,
            contentX,
            panelY + 35,
            0xFFF3F3F3
        );

        graphics.text(
            font,
            progressPercent,
            panelX + PANEL_WIDTH - 14
                - font.width(progressPercent),
            panelY + 35,
            0xFF8FD5FF
        );

        drawProgressBar(
            graphics,
            contentX,
            panelY + 48,
            progress
        );

        drawDangerBanner(
            graphics,
            font,
            minecraft,
            centerX,
            panelY,
            tension,
            breakRisk
        );
    }

    private static void drawPanel(
        GuiGraphicsExtractor graphics,
        int x,
        int y
    ) {
        graphics.fill(
            x + 2,
            y + 2,
            x + PANEL_WIDTH + 2,
            y + PANEL_HEIGHT + 2,
            0x65000000
        );

        graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + PANEL_HEIGHT,
            0xB9181818
        );

        graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + 1,
            0xA0FFFFFF
        );

        graphics.fill(
            x,
            y + PANEL_HEIGHT - 1,
            x + PANEL_WIDTH,
            y + PANEL_HEIGHT,
            0x50000000
        );
    }

    private static void drawTensionBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float tension
    ) {
        drawBarFrame(
            graphics,
            x,
            y
        );

        int innerX = x + 1;
        int innerY = y + 1;
        int innerWidth = BAR_WIDTH - 2;
        int innerHeight = BAR_HEIGHT - 2;

        int lowEnd =
            innerX + Math.round(
                innerWidth
                    * (LOW_END / DISPLAY_MAX)
            );

        int idealEnd =
            innerX + Math.round(
                innerWidth
                    * (IDEAL_END / DISPLAY_MAX)
            );

        int redStart =
            innerX + Math.round(
                innerWidth
                    * (RED_START / DISPLAY_MAX)
            );

        graphics.fill(
            innerX,
            innerY,
            lowEnd,
            innerY + innerHeight,
            0xFF26323B
        );

        graphics.fill(
            lowEnd,
            innerY,
            idealEnd,
            innerY + innerHeight,
            0xFF1C4A28
        );

        graphics.fill(
            idealEnd,
            innerY,
            redStart,
            innerY + innerHeight,
            0xFF5A461B
        );

        graphics.fill(
            redStart,
            innerY,
            innerX + innerWidth,
            innerY + innerHeight,
            0xFF5A1B1B
        );

        float normalized =
            Mth.clamp(
                tension / DISPLAY_MAX,
                0.0F,
                1.0F
            );

        int marker =
            innerX + Math.round(
                normalized
                    * (innerWidth - 1)
            );

        graphics.fill(
            marker - 1,
            y - 2,
            marker + 2,
            y + BAR_HEIGHT + 2,
            0xFFFFFFFF
        );
    }

    private static void drawProgressBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float progress
    ) {
        drawBarFrame(
            graphics,
            x,
            y
        );

        int innerX = x + 1;
        int innerY = y + 1;
        int innerWidth = BAR_WIDTH - 2;
        int innerHeight = BAR_HEIGHT - 2;

        graphics.fill(
            innerX,
            innerY,
            innerX + innerWidth,
            innerY + innerHeight,
            0xFF172832
        );

        int filled =
            Math.round(
                innerWidth * progress
            );

        if (filled > 0) {
            graphics.fill(
                innerX,
                innerY,
                innerX + filled,
                innerY + innerHeight,
                0xFF4AB6F0
            );

            if (filled > 2) {
                graphics.fill(
                    innerX,
                    innerY,
                    innerX + filled,
                    innerY + 1,
                    0xFF90D8FF
                );
            }
        }
    }

    private static void drawBarFrame(
        GuiGraphicsExtractor graphics,
        int x,
        int y
    ) {
        graphics.fill(
            x - 1,
            y - 1,
            x + BAR_WIDTH + 1,
            y + BAR_HEIGHT + 1,
            0xD0000000
        );

        graphics.fill(
            x,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            0xFF232323
        );
    }

    private static void drawDangerBanner(
        GuiGraphicsExtractor graphics,
        Font font,
        Minecraft minecraft,
        int centerX,
        int panelY,
        float tension,
        float breakRisk
    ) {
        if (tension < RED_START
            && breakRisk <= 0.0F) {
            return;
        }

        Component banner =
            Component.translatable(
                "hud.fishing_reimagined.snap_warning"
            );

        int width =
            font.width(banner);

        int x =
            centerX - width / 2;

        int y =
            panelY - 15;

        int pulse =
            (minecraft.player.tickCount / 3)
                    % 2
                    == 0
                ? 0xFFFF6666
                : 0xFFFFFFFF;

        graphics.fill(
            x - 6,
            y - 2,
            x + width + 6,
            y + 10,
            0xB0501717
        );

        graphics.text(
            font,
            banner,
            x,
            y,
            pulse
        );
    }
}
