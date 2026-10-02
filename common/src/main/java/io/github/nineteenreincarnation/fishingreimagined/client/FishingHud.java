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
            || !access
                .fishingReimagined$isFightActive()) {
            return;
        }

        Font font = minecraft.font;

        float tension =
            Math.max(
                0.0F,
                access
                    .fishingReimagined$tensionRatio()
            );

        float progress =
            Mth.clamp(
                access
                    .fishingReimagined$catchProgress(),
                0.0F,
                1.0F
            );

        float breakRisk =
            Mth.clamp(
                access
                    .fishingReimagined$breakRisk(),
                0.0F,
                1.0F
            );

        float distance =
            Math.max(
                0.0F,
                access
                    .fishingReimagined$distance()
            );

        float lineVelocity =
            access
                .fishingReimagined$lineVelocity();

        float dragSlip =
            access
                .fishingReimagined$dragSlip();

        int centerX =
            graphics.guiWidth() / 2;

        int x =
            centerX
                - BAR_WIDTH / 2;

        int y =
            graphics.guiHeight()
                - 104;

        graphics.nextStratum();

        Component tensionLabel =
            Component.translatable(
                tensionStateKey(
                    tension
                )
            );

        graphics.fill(
            x - 4,
            y - 14,
            x + BAR_WIDTH + 4,
            y + 12,
            0x76000000
        );

        graphics.text(
            font,
            tensionLabel,
            x,
            y - 11,
            tensionColor(
                tension
            )
        );

        if (breakRisk > 0.0F) {
            Component riskText =
                Component.translatable(
                    "hud.fishing_reimagined.break_risk",
                    Math.round(
                        breakRisk * 100.0F
                    )
                );

            graphics.text(
                font,
                riskText,
                x + BAR_WIDTH
                    - font.width(
                        riskText
                    ),
                y - 11,
                0xFFFF5555
            );
        }

        drawTensionBar(
            graphics,
            x,
            y,
            tension,
            breakRisk
        );

        int distanceY =
            y + 16;

        Component distanceText =
            Component.translatable(
                "hud.fishing_reimagined.distance",
                String.format(
                    java.util.Locale.ROOT,
                    "%.1f",
                    distance
                )
            );

        graphics.text(
            font,
            distanceText,
            x,
            distanceY,
            0xFFE8E8E8
        );

        Component progressText =
            Component.translatable(
                "hud.fishing_reimagined.progress",
                Math.round(
                    progress * 100.0F
                )
            );

        graphics.text(
            font,
            progressText,
            x + BAR_WIDTH
                - font.width(
                    progressText
                ),
            distanceY,
            0xFFB8DFFF
        );

        int progressY =
            distanceY + 12;

        drawProgressBar(
            graphics,
            x,
            progressY,
            progress
        );

        ReelAction action =
            ClientFishingInput
                .currentAction();

        Component controlText;

        if (dragSlip > 0.008F) {
            controlText =
                Component.translatable(
                    "hud.fishing_reimagined.drag_slipping"
                );
        } else if (
            action
                == ReelAction.REEL_IN
                && lineVelocity > -0.018F
                && tension > 0.72F
        ) {
            controlText =
                Component.translatable(
                    "hud.fishing_reimagined.reel_stalled"
                );
        } else if (
            action
                == ReelAction.REEL_IN
        ) {
            controlText =
                Component.translatable(
                    "hud.fishing_reimagined.control.reeling",
                    minecraft.options
                        .keyUse
                        .getTranslatedKeyMessage()
                );
        } else {
            controlText =
                Component.translatable(
                    "hud.fishing_reimagined.control.releasing",
                    minecraft.options
                        .keyUse
                        .getTranslatedKeyMessage()
                );
        }

        int controlColor =
            dragSlip > 0.008F
                ? 0xFFFFC15A
                : action
                    == ReelAction.REEL_IN
                    ? 0xFFFFFFFF
                    : 0xFFAAD7FF;

        graphics.text(
            font,
            controlText,
            centerX
                - font.width(
                    controlText
                ) / 2,
            progressY + 12,
            controlColor
        );

        int fishState =
            access
                .fishingReimagined$fishState();

        Component hint = switch (fishState) {
            case 1 ->
                Component.translatable(
                    "hud.fishing_reimagined.fish.burst_release",
                    minecraft.options
                        .keyUse
                        .getTranslatedKeyMessage()
                );

            case 2 ->
                Component.translatable(
                    "hud.fishing_reimagined.fish.tired_balance"
                );

            case 4 ->
                Component.translatable(
                    "hud.fishing_reimagined.fish.pull_hint"
                );

            case 5 ->
                Component.translatable(
                    "hud.fishing_reimagined.fish.recover_hint"
                );

            default -> null;
        };

        if (hint != null) {
            graphics.text(
                font,
                hint,
                centerX
                    - font.width(
                        hint
                    ) / 2,
                y - 26,
                fishState == 1
                    ? 0xFFFFC45A
                    : fishState == 5
                        ? 0xFF9ED8FF
                        : 0xFFE8E8E8
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
        graphics.fill(
            x - 2,
            y - 2,
            x + BAR_WIDTH + 2,
            y + BAR_HEIGHT + 2,
            0xD0000000
        );

        int slackEnd =
            x + Math.round(
                BAR_WIDTH
                    * 0.08F
                    / 1.35F
            );

        int dragStart =
            x + Math.round(
                BAR_WIDTH
                    * 0.78F
                    / 1.35F
            );

        int breakStart =
            x + Math.round(
                BAR_WIDTH
                    * 0.96F
                    / 1.35F
            );

        graphics.fill(
            x,
            y,
            slackEnd,
            y + BAR_HEIGHT,
            0xFF50677A
        );

        graphics.fill(
            slackEnd,
            y,
            dragStart,
            y + BAR_HEIGHT,
            0xFF48A95A
        );

        graphics.fill(
            dragStart,
            y,
            breakStart,
            y + BAR_HEIGHT,
            0xFFD29A3F
        );

        graphics.fill(
            breakStart,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            breakRisk > 0.0F
                ? 0xFFFF3D3D
                : 0xFFC44545
        );

        float normalized =
            Mth.clamp(
                tension / 1.35F,
                0.0F,
                1.0F
            );

        int marker =
            x + Math.round(
                normalized
                    * (
                        BAR_WIDTH - 1
                    )
            );

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
        graphics.fill(
            x - 2,
            y - 2,
            x + BAR_WIDTH + 2,
            y + BAR_HEIGHT + 2,
            0xD0000000
        );

        graphics.fill(
            x,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            0xFF18212A
        );

        int filled =
            Math.round(
                BAR_WIDTH * progress
            );

        if (filled > 0) {
            graphics.fill(
                x,
                y,
                x + filled,
                y + BAR_HEIGHT,
                0xFF42A9E8
            );
        }
    }

    private static String tensionStateKey(
        float tension
    ) {
        if (tension < 0.08F) {
            return "hud.fishing_reimagined.tension.slack";
        }

        if (tension < 0.60F) {
            return "hud.fishing_reimagined.tension.good";
        }

        if (tension < 0.78F) {
            return "hud.fishing_reimagined.tension.high";
        }

        if (tension < 0.96F) {
            return "hud.fishing_reimagined.tension.drag";
        }

        return "hud.fishing_reimagined.tension.critical";
    }

    private static int tensionColor(
        float tension
    ) {
        if (tension < 0.08F) {
            return 0xFFA9C7E2;
        }

        if (tension < 0.60F) {
            return 0xFFB6FFBF;
        }

        if (tension < 0.78F) {
            return 0xFFFFD76A;
        }

        if (tension < 0.96F) {
            return 0xFFFFA455;
        }

        return 0xFFFF5555;
    }
}
