package io.github.nineteenreincarnation.fishingreimagined.client;

import io.github.nineteenreincarnation.fishingreimagined.hook.FishingHookFightAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;

public final class FishingHud {
    private static final int PANEL_WIDTH = 252;
    private static final int PANEL_HEIGHT = 61;
    private static final int TRACK_WIDTH = 224;
    private static final int TRACK_HEIGHT = 15;
    private static final int PROGRESS_HEIGHT = 6;

    private FishingHud() {
    }

    public static void render(
        GuiGraphicsExtractor graphics,
        Minecraft minecraft
    ) {
        if (minecraft.player == null) {
            return;
        }

        FishingHook hook =
            minecraft.player.fishing;

        if (!(hook
            instanceof FishingHookFightAccess access)
            || !access.fishingReimagined$isFightActive()) {
            return;
        }

        Font font = minecraft.font;

        float fishPosition = Mth.clamp(
            access.fishingReimagined$fishTrackPosition(),
            0.0F,
            1.0F
        );

        float zonePosition = Mth.clamp(
            access.fishingReimagined$catchZonePosition(),
            0.0F,
            1.0F
        );

        float zoneWidth = Mth.clamp(
            access.fishingReimagined$catchZoneWidth(),
            0.05F,
            0.80F
        );

        float progress = Mth.clamp(
            access.fishingReimagined$catchProgress(),
            0.0F,
            1.0F
        );

        int centerX = graphics.guiWidth() / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = graphics.guiHeight() - 108;
        int trackX = panelX + 14;
        int trackY = panelY + 22;

        graphics.nextStratum();

        drawPanel(
            graphics,
            panelX,
            panelY
        );

        Component label =
            Component.translatable(
                "hud.fishing_reimagined.track_label"
            );

        Component hint =
            Component.translatable(
                "hud.fishing_reimagined.track_hint"
            );

        graphics.text(
            font,
            label,
            trackX,
            panelY + 8,
            0xFFF2F2F2
        );

        graphics.text(
            font,
            hint,
            panelX + PANEL_WIDTH - 14
                - font.width(hint),
            panelY + 8,
            0xFFB9CDE0
        );

        drawCatchTrack(
            graphics,
            trackX,
            trackY,
            fishPosition,
            zonePosition,
            zoneWidth
        );

        Component progressLabel =
            Component.translatable(
                "hud.fishing_reimagined.progress_label"
            );

        String percentage =
            Math.round(
                progress * 100.0F
            )
                + "%";

        graphics.text(
            font,
            progressLabel,
            trackX,
            panelY + 43,
            0xFFF2F2F2
        );

        graphics.text(
            font,
            percentage,
            panelX + PANEL_WIDTH - 14
                - font.width(percentage),
            panelY + 43,
            0xFF8FD5FF
        );

        drawProgress(
            graphics,
            trackX,
            panelY + 54,
            progress
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
            0x60000000
        );

        graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + PANEL_HEIGHT,
            0xB616181A
        );

        graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + 1,
            0x80FFFFFF
        );

        graphics.fill(
            x,
            y + PANEL_HEIGHT - 1,
            x + PANEL_WIDTH,
            y + PANEL_HEIGHT,
            0x50000000
        );
    }

    private static void drawCatchTrack(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float fishPosition,
        float zonePosition,
        float zoneWidth
    ) {
        graphics.fill(
            x - 1,
            y - 1,
            x + TRACK_WIDTH + 1,
            y + TRACK_HEIGHT + 1,
            0xD0000000
        );

        graphics.fill(
            x,
            y,
            x + TRACK_WIDTH,
            y + TRACK_HEIGHT,
            0xFF20262B
        );

        for (int i = 1; i < 4; i++) {
            int tickX =
                x + TRACK_WIDTH * i / 4;

            graphics.fill(
                tickX,
                y + 2,
                tickX + 1,
                y + TRACK_HEIGHT - 2,
                0xFF30383E
            );
        }

        int zoneCenter =
            x + Math.round(
                zonePosition * TRACK_WIDTH
            );

        int halfZone =
            Math.max(
                8,
                Math.round(
                    zoneWidth
                        * TRACK_WIDTH
                        * 0.5F
                )
            );

        int zoneLeft =
            Mth.clamp(
                zoneCenter - halfZone,
                x,
                x + TRACK_WIDTH
            );

        int zoneRight =
            Mth.clamp(
                zoneCenter + halfZone,
                x,
                x + TRACK_WIDTH
            );

        graphics.fill(
            zoneLeft,
            y + 1,
            zoneRight,
            y + TRACK_HEIGHT - 1,
            0xAA2F7C4A
        );

        graphics.fill(
            zoneLeft,
            y,
            zoneLeft + 1,
            y + TRACK_HEIGHT,
            0xFF69D889
        );

        graphics.fill(
            zoneRight - 1,
            y,
            zoneRight,
            y + TRACK_HEIGHT,
            0xFF69D889
        );

        int fishX =
            x + Math.round(
                fishPosition * TRACK_WIDTH
            );

        boolean inside =
            fishX >= zoneLeft
                && fishX <= zoneRight;

        int fishColor =
            inside
                ? 0xFFFFE28A
                : 0xFFFFA85A;

        graphics.fill(
            fishX - 2,
            y + 3,
            fishX + 3,
            y + TRACK_HEIGHT - 3,
            fishColor
        );

        graphics.fill(
            fishX - 4,
            y + 6,
            fishX + 5,
            y + TRACK_HEIGHT - 6,
            fishColor
        );

        graphics.fill(
            fishX - 1,
            y + 1,
            fishX + 2,
            y + 3,
            0xFFFFFFFF
        );
    }

    private static void drawProgress(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float progress
    ) {
        graphics.fill(
            x - 1,
            y - 1,
            x + TRACK_WIDTH + 1,
            y + PROGRESS_HEIGHT + 1,
            0xD0000000
        );

        graphics.fill(
            x,
            y,
            x + TRACK_WIDTH,
            y + PROGRESS_HEIGHT,
            0xFF172832
        );

        int filled =
            Math.round(
                TRACK_WIDTH * progress
            );

        if (filled > 0) {
            graphics.fill(
                x,
                y,
                x + filled,
                y + PROGRESS_HEIGHT,
                0xFF4AB6F0
            );

            graphics.fill(
                x,
                y,
                x + filled,
                y + 1,
                0xFF91D9FF
            );
        }
    }
}
