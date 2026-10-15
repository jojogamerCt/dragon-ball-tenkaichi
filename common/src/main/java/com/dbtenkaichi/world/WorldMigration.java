package com.dbtenkaichi.world;

import com.dbtenkaichi.Tenkaichi;
import net.minecraft.server.level.ServerLevel;

import java.util.Properties;

public final class WorldMigration {
    private WorldMigration() {
    }

    static void migrate(ServerLevel level, Properties props, int fromVersion) {
        Tenkaichi.LOG.info("Migrating Dragon Block: Tenkaichi world data from version {} to {}", fromVersion, Landmarks.DATA_VERSION);
        if (fromVersion < 2) {
            int rebuilt = Landmarks.rebuild(level, props);
            Tenkaichi.LOG.info("World migration: rebuilt {} landmarks and registered {} safe zones", rebuilt, SafeZones.zones().size());
        }
    }
}
