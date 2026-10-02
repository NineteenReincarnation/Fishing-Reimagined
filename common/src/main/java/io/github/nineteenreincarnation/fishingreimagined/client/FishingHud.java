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
    private static final int BAR_WIDTH = 148;
    private static final int BAR_HEIGHT = 5;

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

        int centerX = graphics.guiWidth() / 2;
        int x = centerX - BAR_WIDTH / 2;
        int y = graphics.guiHeight() - 91;

        graphics.nextStratum();

        Component tensionText = Component.translatable(
            tensionStateKey(tension)
        );
        int tensionTextWidth = font.width(tensionText);
        graphics.fill(
            centerX - tensionTextWidth / 2 - 4,
            y - 10,
            centerX + tensionTextWidth / 2 + 4,
            y,
            0x76000000
        );
        graphics.text(
            font,
            tensionText,
            centerX - tensionTextWidth / 2,
            y - 9,
            tensionColor(tension)
        );
        drawTensionBar(graphics, x, y + 3, tension);

        int progressY = y + 15;
        drawProgressBar(
            graphics,
            x,
            progressY,
            progress
        );

        String percent = Math.round(progress * 100.0F) + "%";
        graphics.text(
            font,
            percent,
            centerX - font.width(percent) / 2,
            progressY - 9,
            0xFFE8E8E8
        );

        ReelAction action = ClientFishingInput.currentAction();
        Component actionText = switch (action) {
            case REEL_IN ->
                Component.translatable(
                    "hud.fishing_reimagined.action.reel_key",
                    minecraft.options.keyAttack.getTranslatedKeyMessage()
                );
            case PAY_OUT ->
                Component.translatable(
                    "hud.fishing_reimagined.action.release_key",
                    minecraft.options.keyUse.getTranslatedKeyMessage()
                );
            case HOLD ->
                Component.translatable(
                    "hud.fishing_reimagined.action.hold"
                );
        };

        int actionY = progressY + 11;
        int actionWidth = font.width(actionText);
        graphics.fill(
            centerX - actionWidth / 2 - 4,
            actionY - 1,
            centerX + actionWidth / 2 + 4,
            actionY + 10,
            0x62000000
        );
        graphics.text(
            font,
            actionText,
            centerX - actionWidth / 2,
            actionY,
            actionColor(action)
        );

        int fishState = access.fishingReimagined$fishState();
        if (fishState != 0) {
            Component stateText = switch (fishState) {
                case 1 -> Component.translatable(
                    "hud.fishing_reimagined.fish.burst_hint_key",
                    minecraft.options.keyUse.getTranslatedKeyMessage()
                );
                case 2 -> Component.translatable(
                    "hud.fishing_reimagined.fish.tired_hint_key",
                    minecraft.options.keyAttack.getTranslatedKeyMessage()
                );
                case 3 -> Component.translatable(
                    "hud.fishing_reimagined.fish.landing_hint"
                );
                default -> Component.translatable(
                    "hud.fishing_reimagined.fish.fighting"
                );
            };
            int stateWidth = font.width(stateText);
            int stateY = y - 24;
            graphics.fill(
                centerX - stateWidth / 2 - 5,
                stateY - 1,
                centerX + stateWidth / 2 + 5,
                stateY + 10,
                fishState == 1
                    ? 0x8A3B2400
                    : fishState == 3
                        ? 0x70332018
                        : 0x70202A33
            );
            graphics.text(
                font,
                stateText,
                centerX - stateWidth / 2,
                stateY,
                fishState == 1
                    ? 0xFFFFC45A
                    : fishState == 3
                        ? 0xFFFFE0A0
                        : 0xFF9ED8FF
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
            0xA0000000
        );

        int slackEnd =
            x + Math.round(BAR_WIDTH * 0.08F / 1.25F);
        int safeEnd =
            x + Math.round(BAR_WIDTH * 0.72F / 1.25F);
        int highEnd =
            x + Math.round(BAR_WIDTH * 1.00F / 1.25F);

        graphics.fill(
            x,
            y,
            slackEnd,
            y + BAR_HEIGHT,
            0xFF555555
        );
        graphics.fill(
            slackEnd,
            y,
            safeEnd,
            y + BAR_HEIGHT,
            0xFF3F9751
        );
        graphics.fill(
            safeEnd,
            y,
            highEnd,
            y + BAR_HEIGHT,
            0xFFC4A13F
        );
        graphics.fill(
            highEnd,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            0xFFC94D4D
        );

        float normalized = Mth.clamp(
            tension / 1.25F,
            0.0F,
            1.0F
        );
        int marker =
            x + Math.round(normalized * (BAR_WIDTH - 1));

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
        graphics.fill(
            x - 1,
            y - 1,
            x + BAR_WIDTH + 1,
            y + BAR_HEIGHT + 1,
            0xA0000000
        );
        graphics.fill(
            x,
            y,
            x + BAR_WIDTH,
            y + BAR_HEIGHT,
            0xFF1F2730
        );

        int filled = Math.round(BAR_WIDTH * progress);
        if (filled > 0) {
            graphics.fill(
                x,
                y,
                x + filled,
                y + BAR_HEIGHT,
                0xFF4D9AD7
            );
        }
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

    private static int tensionColor(float tension) {
        if (tension < 0.08F) {
            return 0xFFB7B7B7;
        }
        if (tension < 0.72F) {
            return 0xFFA7F1AF;
        }
        if (tension <= 1.0F) {
            return 0xFFFFDA74;
        }
        return 0xFFFF7373;
    }

    private static int actionColor(ReelAction action) {
        return switch (action) {
            case REEL_IN -> 0xFFFFFFFF;
            case PAY_OUT -> 0xFFA8D9FF;
            case HOLD -> 0xFFC0C0C0;
        };
    }
}
