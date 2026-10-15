package com.dbtenkaichi.form;

import java.util.Locale;

public enum FormPassive {
    SPEED(true),
    STRIKE(true),
    KI_POWER(true),
    GUARD(true),
    EFFICIENT(true),
    AUTO_DODGE(true),
    GOD_KI(false),
    GIANT(false),
    RAMPAGE(false),
    REGENERATION(true),
    BATTLE_LUST(true),
    LAST_STAND(true),
    REFLECT(true),
    SUPER_ARMOR(false),
    DESTRUCTION(true),
    STAMINA_FREE(false),
    INTIMIDATE(true),
    KI_REGEN(true),
    TAIL_BOUND(false),
    MOON_BOUND(false);

    private static final FormPassive[] VALUES = values();
    private final boolean percent;

    FormPassive(boolean percent) {
        this.percent = percent;
    }

    public boolean percent() {
        return percent;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "form_passive.dbtenkaichi." + id();
    }

    public static FormPassive byId(String id) {
        if (id == null) {
            return null;
        }
        for (FormPassive passive : VALUES) {
            if (passive.id().equals(id)) {
                return passive;
            }
        }
        return null;
    }
}
