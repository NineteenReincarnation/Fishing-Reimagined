package io.github.nineteenreincarnation.fishingreimagined.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public final class SpecialFishingConfig {
    private static final Gson GSON =
        new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH =
        Paths.get(
            "config",
            "fishing_reimagined.json"
        );

    private static boolean specialFishingEnabled;

    private static String animationCompatibilityMode =
        "auto";

    static {
        load();
    }

    private SpecialFishingConfig() {
    }

    public static boolean specialFishingEnabled() {
        return specialFishingEnabled;
    }

    public static String animationCompatibilityMode() {
        return animationCompatibilityMode;
    }

    public static void setSpecialFishingEnabled(
        boolean enabled
    ) {
        specialFishingEnabled = enabled;
        save();
    }

    public static boolean toggleSpecialFishing() {
        setSpecialFishingEnabled(
            !specialFishingEnabled
        );

        return specialFishingEnabled;
    }

    private static void load() {
        specialFishingEnabled = false;
        animationCompatibilityMode = "auto";

        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try {
            String json =
                Files.readString(
                    CONFIG_PATH,
                    StandardCharsets.UTF_8
                );

            JsonObject object =
                GSON.fromJson(
                    json,
                    JsonObject.class
                );

            if (
                object != null
                    && object.has(
                        "specialFishingEnabled"
                    )
            ) {
                specialFishingEnabled =
                    object.get(
                        "specialFishingEnabled"
                    ).getAsBoolean();
            }

            boolean needsRewrite =
                object == null
                    || !object.has(
                        "animationCompatibility"
                    );

            if (
                object != null
                    && object.has(
                        "animationCompatibility"
                    )
            ) {
                String rawMode =
                    object.get(
                        "animationCompatibility"
                    ).getAsString();

                animationCompatibilityMode =
                    normalizeAnimationMode(
                        rawMode
                    );

                needsRewrite =
                    !animationCompatibilityMode
                        .equals(
                            rawMode
                                .trim()
                                .toLowerCase(
                                    Locale.ROOT
                                )
                        );
            }

            if (needsRewrite) {
                save();
            }
        } catch (
            IOException
                | RuntimeException ignored
        ) {
            specialFishingEnabled = false;
            animationCompatibilityMode = "auto";
        }
    }

    private static String normalizeAnimationMode(
        String value
    ) {
        if (value == null) {
            return "auto";
        }

        String normalized =
            value.trim()
                .toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "builtin",
                "resource_pack",
                "overlay" ->
                normalized;
            default -> "auto";
        };
    }

    private static void save() {
        JsonObject object =
            new JsonObject();

        object.addProperty(
            "specialFishingEnabled",
            specialFishingEnabled
        );

        object.addProperty(
            "animationCompatibility",
            animationCompatibilityMode
        );

        try {
            Files.createDirectories(
                CONFIG_PATH.getParent()
            );

            Files.writeString(
                CONFIG_PATH,
                GSON.toJson(object),
                StandardCharsets.UTF_8
            );
        } catch (IOException ignored) {
        }
    }
}
