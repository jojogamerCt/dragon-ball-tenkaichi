package com.dbtenkaichi.form;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.race.Race;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FormRegistry {
    public static final String BASE = "dbtenkaichi:base";

    private static volatile Map<String, FormDefinition> server = Map.of();
    private static volatile Map<String, FormDefinition> client = Map.of();

    private FormRegistry() {
    }

    public static FormDefinition get(String id, boolean clientSide) {
        return (clientSide ? client : server).get(id);
    }

    public static Collection<FormDefinition> all(boolean clientSide) {
        return (clientSide ? client : server).values();
    }

    public static List<FormDefinition> chainFor(Race race, boolean clientSide) {
        List<FormDefinition> list = new ArrayList<>();
        for (FormDefinition def : all(clientSide)) {
            if (def.allows(race)) {
                list.add(def);
            }
        }
        list.sort(Comparator.comparingInt((FormDefinition d) -> d.order).thenComparing(d -> d.id));
        return list;
    }

    public static FormDefinition next(Race race, String current, boolean clientSide) {
        for (FormDefinition def : chainFor(race, clientSide)) {
            if (def.parent.equals(current)) {
                return def;
            }
        }
        return null;
    }

    public static void applyServer(Map<String, JsonElement> raw) {
        Map<String, FormDefinition> parsed = parse(raw);
        server = parsed;
        Tenkaichi.LOG.info("Loaded {} transformation definitions", parsed.size());
    }

    public static void applyClient(List<String> json) {
        Map<String, JsonElement> raw = new LinkedHashMap<>();
        for (String entry : json) {
            try {
                JsonElement element = com.google.gson.JsonParser.parseString(entry);
                if (element.isJsonObject() && element.getAsJsonObject().has("id")) {
                    raw.put(element.getAsJsonObject().get("id").getAsString(), element);
                }
            } catch (RuntimeException e) {
                Tenkaichi.LOG.warn("Ignoring malformed transformation sent by server", e);
            }
        }
        client = parse(raw);
    }

    public static void clearClient() {
        client = Map.of();
    }

    public static List<String> serializeServer() {
        List<String> out = new ArrayList<>();
        for (FormDefinition def : server.values()) {
            out.add(ConfigManager.GSON.toJson(def));
        }
        return out;
    }

    private static Map<String, FormDefinition> parse(Map<String, JsonElement> raw) {
        Map<String, FormDefinition> parsed = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : raw.entrySet()) {
            try {
                FormDefinition def = ConfigManager.GSON.fromJson(entry.getValue(), FormDefinition.class);
                if (def == null) {
                    continue;
                }
                def.id = entry.getKey();
                def.validate();
                if (BASE.equals(def.id)) {
                    Tenkaichi.LOG.warn("Transformation id {} is reserved", def.id);
                    continue;
                }
                parsed.put(def.id, def);
            } catch (RuntimeException e) {
                Tenkaichi.LOG.error("Invalid transformation definition {}", entry.getKey(), e);
            }
        }
        List<String> broken = new ArrayList<>();
        for (FormDefinition def : parsed.values()) {
            if (!BASE.equals(def.parent) && !parsed.containsKey(def.parent)) {
                broken.add(def.id);
            }
        }
        for (String id : broken) {
            Tenkaichi.LOG.error("Transformation {} references a missing parent and was disabled", id);
            parsed.remove(id);
        }
        return Map.copyOf(parsed);
    }
}
