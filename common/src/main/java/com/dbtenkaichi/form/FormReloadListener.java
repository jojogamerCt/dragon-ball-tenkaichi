package com.dbtenkaichi.form;

import com.dbtenkaichi.Tenkaichi;
import com.google.gson.JsonElement;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FormReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Tenkaichi.MOD_ID, "forms");

    public FormReloadListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("tenkaichi_forms"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> preparations, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, JsonElement> raw = new LinkedHashMap<>();
        preparations.forEach((id, json) -> raw.put(id.toString(), json));
        FormRegistry.applyServer(raw);
    }
}
