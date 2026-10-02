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

        drawPanel(graphics, panelX, panelY);

        ReelAction action = ClientFishingInput.currentAction();
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
            Math.round(progress * 100.0F) + "%";

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

        drawContextBanner(
            graphics,
            font,
            minecraft,
            access,
            centerX,
            panelY,
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
        drawBarFrame(graphics, x, y);

        int innerX = x + 1;
        int innerY = y + 1;
        int innerWidth = BAR_WIDTH - 2;
        int innerHeight = BAR_HEIGHT - 2;

        int safeEnd =
            innerX + Math.round(
                innerWidth * (0.80F / 1.20F)
            );

        int warningEnd =
            innerX + Math.round(
                innerWidth * (1.00F / 1.20F)
            );

        graphics.fill(
            innerX,
            innerY,
            safeEnd,
            innerY + innerHeight,
            0xFF183421
        );

        graphics.fill(
            safeEnd,
            innerY,
            warningEnd,
            innerY + innerHeight,
            0xFF4A3718
        );

        graphics.fill(
            warningEnd,
            innerY,
            innerX + innerWidth,
            innerY + innerHeight,
            0xFF4B1919
        );

        float normalized =
            Mth.clamp(
                tension / 1.20F,
                0.0F,
                1.0F
            );

        int filled =
            Math.round(
                innerWidth * normalized
            );

        int fillColor;
        if (tension < 0.80F) {
            fillColor = 0xFF55C96E;
        } else if (tension < 1.00F) {
            fillColor = 0xFFF0B548;
        } else {
            fillColor = 0xFFF05B5B;
        }

        if (filled > 0) {
            graphics.fill(
                innerX,
                innerY,
                innerX + filled,
                innerY + innerHeight,
                fillColor
            );
        }

        int marker =
            innerX + Mth.clamp(
                filled,
                0,
                innerWidth - 1
            );

        graphics.fill(
            marker,
            y - 1,
            marker + 1,
            y + BAR_HEIGHT + 1,
            0xFFFFFFFF
        );
    }

    private static void drawProgressBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float progress
    ) {
        drawBarFrame(graphics, x, y);

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

    private static void drawContextBanner(
        GuiGraphicsExtractor graphics,
        Font font,
        Minecraft minecraft,
        FishingHookFightAccess access,
        int centerX,
        int panelY,
        float breakRisk
    ) {
        Component banner = null;
        int color = 0xFFFFFFFF;
        int background = 0xA0202020;

        if (breakRisk > 0.0F) {
            banner = Component.translatable(
                "hud.fishing_reimagined.snap_warning"
            );
            color =
                (minecraft.player.tickCount / 3) % 2 == 0
                    ? 0xFFFF6666
                    : 0xFFFFFFFF;
            background = 0xB0501717;
        } else if (
            access.fishingReimagined$fishState() == 1
        ) {
            banner = Component.translatable(
                "hud.fishing_reimagined.burst_warning"
            );
            color = 0xFFFFD269;
            background = 0xA04C3414;
        }

        if (banner == null) {
            return;
        }

        int width = font.width(banner);
        int x = centerX - width / 2;
        int y = panelY - 15;

        graphics.fill(
            x - 6,
            y - 2,
            x + width + 6,
            y + 10,
            background
        );

        graphics.text(
            font,
            banner,
            x,
            y,
            color
        );
    }
}
