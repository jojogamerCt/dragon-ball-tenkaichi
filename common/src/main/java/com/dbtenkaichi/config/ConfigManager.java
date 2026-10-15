package com.dbtenkaichi.config;

import com.dbtenkaichi.Tenkaichi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {
    public static final int COMMON_SCHEMA = 4;
    public static final int CLIENT_SCHEMA = 1;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

    private static volatile TenkaichiConfig common = new TenkaichiConfig();
    private static volatile TenkaichiConfig serverOverride;
    private static volatile ClientConfig client = new ClientConfig();
    private static Path directory;

    private ConfigManager() {
    }

    public static void init(Path configDirectory) {
        directory = configDirectory;
        reloadCommon();
    }

    public static TenkaichiConfig get() {
        TenkaichiConfig override = serverOverride;
        return override != null ? override : common;
    }

    public static TenkaichiConfig local() {
        return common;
    }

    public static BalanceConfig balance() {
        return get().balance;
    }

    public static GameplayConfig gameplay() {
        return get().gameplay;
    }

    public static ClientConfig client() {
        return client;
    }

    public static void applyServerConfig(String json) {
        try {
            TenkaichiConfig parsed = GSON.fromJson(json, TenkaichiConfig.class);
            if (parsed != null) {
                parsed.validate();
                serverOverride = parsed;
            }
        } catch (RuntimeException e) {
            Tenkaichi.LOG.warn("Ignoring malformed server configuration", e);
        }
    }

    public static void clearServerConfig() {
        serverOverride = null;
    }

    public static String serializeCommon() {
        return GSON.toJson(common);
    }

    public static synchronized void reloadCommon() {
        if (directory == null) {
            return;
        }
        Path file = directory.resolve(Tenkaichi.MOD_ID + "-common.json");
        TenkaichiConfig loaded = read(file, TenkaichiConfig.class, COMMON_SCHEMA, ConfigManager::migrateCommon);
        if (loaded == null) {
            loaded = new TenkaichiConfig();
        }
        loaded.validate();
        loaded.schemaVersion = COMMON_SCHEMA;
        common = loaded;
        write(file, loaded);
    }

    public static synchronized void reloadClient() {
        if (directory == null) {
            return;
        }
        Path file = directory.resolve(Tenkaichi.MOD_ID + "-client.json");
        ClientConfig loaded = read(file, ClientConfig.class, CLIENT_SCHEMA, (obj, version) -> obj);
        if (loaded == null) {
            loaded = new ClientConfig();
        }
        loaded.schemaVersion = CLIENT_SCHEMA;
        client = loaded;
        write(file, loaded);
    }

    public static synchronized void saveClient() {
        if (directory != null) {
            write(directory.resolve(Tenkaichi.MOD_ID + "-client.json"), client);
        }
    }

    static JsonObject migrateCommon(JsonObject obj, int fromVersion) {
        if (fromVersion < 2 && obj.has("gameplay") && obj.get("gameplay").isJsonObject()) {
            JsonObject gameplay = obj.getAsJsonObject("gameplay");
            if (gameplay.has("enabledRaces") && gameplay.get("enabledRaces").isJsonArray()) {
                com.google.gson.JsonArray races = gameplay.getAsJsonArray("enabledRaces");
                boolean oldDefault = races.size() == 2 && races.contains(new com.google.gson.JsonPrimitive("saiyan")) && races.contains(new com.google.gson.JsonPrimitive("human"));
                if (oldDefault) {
                    com.google.gson.JsonArray all = new com.google.gson.JsonArray();
                    GameplayConfig.ALL_RACES.forEach(all::add);
                    gameplay.add("enabledRaces", all);
                }
            }
        }
        if (fromVersion < 3 && obj.has("gameplay") && obj.get("gameplay").isJsonObject()) {
            JsonObject gameplay = obj.getAsJsonObject("gameplay");
            if (gameplay.has("cinematicTicks") && gameplay.get("cinematicTicks").getAsInt() == 70) {
                gameplay.addProperty("cinematicTicks", 200);
            }
        }
        if (fromVersion < 4 && obj.has("gameplay") && obj.get("gameplay").isJsonObject()) {
            JsonObject gameplay = obj.getAsJsonObject("gameplay");
            if (gameplay.has("maxExplosionRadius") && gameplay.get("maxExplosionRadius").getAsInt() == 6) {
                gameplay.addProperty("maxExplosionRadius", 14);
            }
            if (gameplay.has("maxBlocksPerExplosion") && gameplay.get("maxBlocksPerExplosion").getAsInt() == 220) {
                gameplay.addProperty("maxBlocksPerExplosion", 3000);
            }
        }
        obj.addProperty("schemaVersion", COMMON_SCHEMA);
        return obj;
    }

    private static <T> T read(Path file, Class<T> type, int currentSchema, Migration migration) {
        if (!Files.exists(file)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) {
                throw new IOException("Root is not an object");
            }
            JsonObject obj = element.getAsJsonObject();
            int version = obj.has("schemaVersion") ? obj.get("schemaVersion").getAsInt() : 0;
            if (version > currentSchema) {
                Tenkaichi.LOG.warn("{} was written by a newer version (schema {}), loading what is understood", file.getFileName(), version);
            }
            if (version < currentSchema) {
                backup(file, version);
                obj = migration.migrate(obj, version);
            }
            return GSON.fromJson(obj, type);
        } catch (IOException | RuntimeException e) {
            Tenkaichi.LOG.error("Failed to read {}, keeping a backup and using defaults", file, e);
            backup(file, -1);
            return null;
        }
    }

    private static void backup(Path file, int version) {
        try {
            Path target = file.resolveSibling(file.getFileName() + (version < 0 ? ".broken" : ".v" + version) + ".bak");
            Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Tenkaichi.LOG.warn("Could not back up {}", file, e);
        }
    }

    private static void write(Path file, Object value) {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(value, writer);
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Tenkaichi.LOG.error("Failed to write {}", file, e);
        }
    }

    @FunctionalInterface
    interface Migration {
        JsonObject migrate(JsonObject obj, int fromVersion);
    }
}
