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
    private static final int BAR_WIDTH = 172;
    private static final int BAR_HEIGHT = 7;
    private static final int PANEL_WIDTH = 188;
    private static final int PANEL_HEIGHT = 74;

    private FishingHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, Minecraft minecraft) {
        if (minecraft.player == null) {
            return;
        }

        FishingHook hook = minecraft.player.fishing;
        if (!(hook instanceof FishingHookFightAccess access)
            || !access.fishingReimagined$isFightActive()) {
            return;
        }

        Font font = minecraft.font;
        int x = (graphics.guiWidth() - PANEL_WIDTH) / 2;
        int y = graphics.guiHeight() - 124;

        float tension = Math.max(0.0F, access.fishingReimagined$tensionRatio());
        float progress = Mth.clamp(
            access.fishingReimagined$catchProgress(),
            0.0F,
            1.0F
        );

        graphics.nextStratum();
        graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + PANEL_HEIGHT,
            0x98000000
        );
        graphics.outline(
            x,
            y,
            PANEL_WIDTH,
            PANEL_HEIGHT,
            0xCC555555
        );

        int contentX = x + 8;

        Component tensionState = Component.translatable(
            tensionStateKey(tension)
        );
        Component tensionLabel = Component.translatable(
            "hud.fishing_reimagined.tension",
            tensionState
        );
        graphics.text(font, tensionLabel, contentX, y + 5, 0xFFFFFFFF);
        drawTensionBar(graphics, contentX, y + 16, tension);

        Component progressLabel = Component.translatable(
            "hud.fishing_reimagined.progress",
            Math.round(progress * 100.0F)
        );
        graphics.text(font, progressLabel, contentX, y + 29, 0xFFFFFFFF);
        drawProgressBar(graphics, contentX, y + 40, progress);

        ReelAction action = ClientFishingInput.currentAction();
        Component left = Component.translatable(
            "hud.fishing_reimagined.reel_in"
        );
        Component right = Component.translatable(
            "hud.fishing_reimagined.pay_out"
        );

        int controlsY = y + 53;
        int leftColor = action == ReelAction.REEL_IN
            ? 0xFFFFFFFF
            : 0xFF8A8A8A;
        int rightColor = action == ReelAction.PAY_OUT
            ? 0xFFFFFFFF
            : 0xFF8A8A8A;

        graphics.text(font, left, contentX, controlsY, leftColor);
        graphics.text(
            font,
            right,
            x + PANEL_WIDTH - 8 - font.width(right),
            controlsY,
            rightColor
        );

        Component fishState = Component.translatable(
            fishStateKey(access.fishingReimagined$fishState())
        );
        int stateWidth = font.width(fishState);
        graphics.text(
            font,
            fishState,
            x + (PANEL_WIDTH - stateWidth) / 2,
            y + 64,
            fishStateColor(access.fishingReimagined$fishState())
        );
    }

    private static void drawTensionBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float tension
    ) {
        int width = BAR_WIDTH;
        graphics.fill(x, y, x + width, y + BAR_HEIGHT, 0xFF1D1D1D);

        int slackEnd = x + Math.round(width * 0.08F / 1.25F);
        int safeEnd = x + Math.round(width * 0.72F / 1.25F);
        int highEnd = x + Math.round(width * 1.00F / 1.25F);

        graphics.fill(x, y, slackEnd, y + BAR_HEIGHT, 0xFF515151);
        graphics.fill(slackEnd, y, safeEnd, y + BAR_HEIGHT, 0xFF3D8C4A);
        graphics.fill(safeEnd, y, highEnd, y + BAR_HEIGHT, 0xFFB99737);
        graphics.fill(highEnd, y, x + width, y + BAR_HEIGHT, 0xFFA94848);

        float normalized = Mth.clamp(tension / 1.25F, 0.0F, 1.0F);
        int marker = x + Math.round(normalized * (width - 1));
        graphics.fill(
            marker - 1,
            y - 2,
            marker + 2,
            y + BAR_HEIGHT + 2,
            0xFFFFFFFF
        );
        graphics.outline(
            x - 1,
            y - 1,
            width + 2,
            BAR_HEIGHT + 2,
            0xFF000000
        );
    }

    private static void drawProgressBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float progress
    ) {
        graphics.fill(
            x,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            0xFF1D1D1D
        );
        int filled = Math.round(BAR_WIDTH * progress);
        if (filled > 0) {
            graphics.fill(
                x,
                y,
                x + filled,
                y + BAR_HEIGHT,
                0xFF4A8FCB
            );
        }
        graphics.outline(
            x - 1,
            y - 1,
            BAR_WIDTH + 2,
            BAR_HEIGHT + 2,
            0xFF000000
        );
    }

    private static String tensionStateKey(float tension) {
        if (tension < 0.08F) {
            return "hud.fishing_reimagined.tension.slack";
        }
        if (tension < 0.72F) {
            return "hud.fishing_reimagined.tension.good";
        }
        if (tension <= 1.0F) {
            return "hud.fishing_reimagined.tension.high";
        }
        return "hud.fishing_reimagined.tension.critical";
    }

    private static String fishStateKey(int state) {
        return switch (state) {
            case 1 -> "hud.fishing_reimagined.fish.burst";
            case 2 -> "hud.fishing_reimagined.fish.tired";
            default -> "hud.fishing_reimagined.fish.fighting";
        };
    }

    private static int fishStateColor(int state) {
        return switch (state) {
            case 1 -> 0xFFFFC75A;
            case 2 -> 0xFF8FD0FF;
            default -> 0xFFD8D8D8;
        };
    }
}
