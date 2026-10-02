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
    private static final int BAR_WIDTH = 184;
    private static final int BAR_HEIGHT = 7;

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
        float tension = Math.max(0.0F, access.fishingReimagined$tensionRatio());
        float progress = Mth.clamp(access.fishingReimagined$catchProgress(), 0.0F, 1.0F);
        float breakRisk = Mth.clamp(access.fishingReimagined$breakRisk(), 0.0F, 1.0F);

        int centerX = graphics.guiWidth() / 2;
        int x = centerX - BAR_WIDTH / 2;
        int y = graphics.guiHeight() - 102;

        graphics.nextStratum();

        Component tensionLabel = Component.translatable(tensionStateKey(tension));
        graphics.fill(x - 4, y - 13, x + BAR_WIDTH + 4, y + 12, 0x70000000);
        graphics.text(font, tensionLabel, x, y - 11, tensionColor(tension));

        if (breakRisk > 0.0F) {
            Component riskText = Component.translatable(
                "hud.fishing_reimagined.break_risk",
                Math.round(breakRisk * 100.0F)
            );
            graphics.text(
                font,
                riskText,
                x + BAR_WIDTH - font.width(riskText),
                y - 11,
                0xFFFF5555
            );
        }

        drawTensionBar(graphics, x, y, tension, breakRisk);

        int progressY = y + 25;
        graphics.text(
            font,
            Component.translatable(
                "hud.fishing_reimagined.progress",
                Math.round(progress * 100.0F)
            ),
            x,
            progressY - 11,
            0xFFE8E8E8
        );
        drawProgressBar(graphics, x, progressY, progress);

        ReelAction action = ClientFishingInput.currentAction();
        Component controlText = action == ReelAction.REEL_IN
            ? Component.translatable(
                "hud.fishing_reimagined.control.reeling",
                minecraft.options.keyUse.getTranslatedKeyMessage()
            )
            : Component.translatable(
                "hud.fishing_reimagined.control.releasing",
                minecraft.options.keyUse.getTranslatedKeyMessage()
            );

        graphics.text(
            font,
            controlText,
            centerX - font.width(controlText) / 2,
            progressY + 12,
            action == ReelAction.REEL_IN ? 0xFFFFFFFF : 0xFFAAD7FF
        );

        int fishState = access.fishingReimagined$fishState();
        if (fishState == 1) {
            Component hint = Component.translatable(
                "hud.fishing_reimagined.fish.burst_release",
                minecraft.options.keyUse.getTranslatedKeyMessage()
            );
            graphics.text(
                font,
                hint,
                centerX - font.width(hint) / 2,
                y - 25,
                0xFFFFC45A
            );
        } else if (fishState == 2) {
            Component hint = Component.translatable(
                "hud.fishing_reimagined.fish.tired_balance"
            );
            graphics.text(
                font,
                hint,
                centerX - font.width(hint) / 2,
                y - 25,
                0xFF9ED8FF
            );
        }
    }

    private static void drawTensionBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float tension,
        float breakRisk
    ) {
        graphics.fill(x - 2, y - 2, x + BAR_WIDTH + 2, y + BAR_HEIGHT + 2, 0xD0000000);

        int slackEnd = x + Math.round(BAR_WIDTH * 0.18F / 1.30F);
        int sweetStart = x + Math.round(BAR_WIDTH * 0.34F / 1.30F);
        int sweetEnd = x + Math.round(BAR_WIDTH * 0.70F / 1.30F);
        int warningStart = x + Math.round(BAR_WIDTH * 0.82F / 1.30F);
        int breakStart = x + Math.round(BAR_WIDTH * 0.95F / 1.30F);

        graphics.fill(x, y, slackEnd, y + BAR_HEIGHT, 0xFF4E667A);
        graphics.fill(slackEnd, y, sweetStart, y + BAR_HEIGHT, 0xFF66705A);
        graphics.fill(sweetStart, y, sweetEnd, y + BAR_HEIGHT, 0xFF48A95A);
        graphics.fill(sweetEnd, y, warningStart, y + BAR_HEIGHT, 0xFFB39A43);
        graphics.fill(warningStart, y, breakStart, y + BAR_HEIGHT, 0xFFD2763F);
        graphics.fill(
            breakStart,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            breakRisk > 0.0F ? 0xFFFF3D3D : 0xFFC44545
        );

        int ideal = x + Math.round(BAR_WIDTH * 0.52F / 1.30F);
        graphics.fill(ideal, y - 1, ideal + 1, y + BAR_HEIGHT + 1, 0xFFCBFFD1);

        float normalized = Mth.clamp(tension / 1.30F, 0.0F, 1.0F);
        int marker = x + Math.round(normalized * (BAR_WIDTH - 1));

        graphics.fill(
            marker - 1,
            y - 3,
            marker + 2,
            y + BAR_HEIGHT + 3,
            0xFFFFFFFF
        );
    }

    private static void drawProgressBar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float progress
    ) {
        graphics.fill(x - 2, y - 2, x + BAR_WIDTH + 2, y + BAR_HEIGHT + 2, 0xD0000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF18212A);

        int filled = Math.round(BAR_WIDTH * progress);
        if (filled > 0) {
            graphics.fill(x, y, x + filled, y + BAR_HEIGHT, 0xFF42A9E8);
        }
    }

    private static String tensionStateKey(float tension) {
        if (tension < 0.18F) {
            return "hud.fishing_reimagined.tension.slack";
        }
        if (tension < 0.34F) {
            return "hud.fishing_reimagined.tension.low";
        }
        if (tension <= 0.70F) {
            return "hud.fishing_reimagined.tension.good";
        }
        if (tension < 0.95F) {
            return "hud.fishing_reimagined.tension.high";
        }
        return "hud.fishing_reimagined.tension.critical";
    }

    private static int tensionColor(float tension) {
        if (tension < 0.18F) {
            return 0xFFA9C7E2;
        }
        if (tension < 0.34F) {
            return 0xFFD4D5A6;
        }
        if (tension <= 0.70F) {
            return 0xFFB6FFBF;
        }
        if (tension < 0.95F) {
            return 0xFFFFD06A;
        }
        return 0xFFFF5A5A;
    }
}
