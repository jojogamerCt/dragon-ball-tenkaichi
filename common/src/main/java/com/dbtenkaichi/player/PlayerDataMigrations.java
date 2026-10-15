package com.dbtenkaichi.player;

import net.minecraft.nbt.CompoundTag;

public final class PlayerDataMigrations {
    private PlayerDataMigrations() {
    }

    public static CompoundTag migrate(CompoundTag raw) {
        int schema = raw.getIntOr("schema", 0);
        if (schema >= PlayerData.SCHEMA) {
            return raw;
        }
        CompoundTag tag = raw.copy();
        if (schema < 1) {
            if (!tag.contains("form")) {
                tag.putString("form", PlayerData.BASE_FORM);
            }
            tag.putInt("schema", 1);
        }
        if (tag.getIntOr("schema", 1) < 2) {
            CompoundTag appearance = tag.getCompoundOrEmpty("appearance").copy();
            if (appearance.contains("hair_style")) {
                appearance.putInt("hair_style", HairStyle.fromLegacy(appearance.getIntOr("hair_style", 0)));
                tag.put("appearance", appearance);
            }
            tag.putInt("schema", 2);
        }
        return tag;
    }
}
