package com.dbtenkaichi;

import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.registry.ModItems;
import com.dbtenkaichi.registry.ModSounds;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Tenkaichi {
    public static final String MOD_ID = "dbtenkaichi";
    public static final Logger LOG = LoggerFactory.getLogger("DBTenkaichi");

    private Tenkaichi() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        ConfigManager.init(Services.PLATFORM.configDirectory());
        ModSounds.init();
        ModEntities.init();
        ModItems.init();
        LOG.info("Dragon Ball: Tenkaichi initialised on {}", Services.PLATFORM.loaderName());
    }
}
