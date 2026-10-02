package io.github.nineteenreincarnation.fishingreimagined.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    static {
        load();
    }

    private SpecialFishingConfig() {
    }

    public static boolean specialFishingEnabled() {
        return specialFishingEnabled;
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
        } catch (
            IOException
                | RuntimeException ignored
        ) {
            specialFishingEnabled = false;
        }
    }

    private static void save() {
        JsonObject object =
            new JsonObject();

        object.addProperty(
            "specialFishingEnabled",
            specialFishingEnabled
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
